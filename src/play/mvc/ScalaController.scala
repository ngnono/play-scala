package play.mvc

import results._
import scala.xml.NodeSeq
import scala.io.Source
import scala.collection.JavaConversions._

import java.io.InputStream
import java.util.concurrent.Future

import play.mvc.Http._
import play.mvc.Scope._
import play.data.validation.Validation
import play.classloading.enhancers.LocalvariablesNamesEnhancer.{LocalVariablesSupport, LocalVariablesNamesTracer}
import play.classloading.enhancers.ControllersEnhancer.ControllerSupport
import play.WithEscape


/**
 *
 * Represents a Scala based Controller
 */
private[mvc] abstract class ScalaController extends ControllerDelegate with LocalVariablesSupport with ControllerSupport {

  
  /**
   * implicit def to provider an easier way to render arguments 
   */
  implicit def richRenderArgs(x: RenderArgs) = new RichRenderArgs(x)

  /**
   * implicit def to provide some extra syntatic sugar while dealing with Response objects 
   */
  implicit def richResponse(x: Response) = new RichResponse(x)

  /**
   * implicit def to to provide some extra syntatic sugar while dealing with a sessions 
   */
  implicit def richSession(x: Session) = new RichSession(x)

  /**
   * implicit def to wrap a String as template name 
   */
  implicit def stringAsTemplateName(x: String) = new StringAsTemplate(x)

  /**
   * implicit def to wrap response into an Option
   */
  implicit def optionToResults[T](x: Option[T]) = new OptionWithResults[T](x)

  // -- Responses

  def Ok                                          = new Ok()
  def Created                                     = new Status(201)
  def Accepted                                    = new Status(202)
  def NoContent                                   = new Status(204)
  def NotModified                                 = new NotModified()
  def NotModified(etag: String)                   = new NotModified(etag)
  def Forbidden                                   = new Forbidden("Forbidden")
  def Forbidden(why: String)                      = new Forbidden(why)
  def NotFound                                    = new NotFound("Not found")
  def NotFound(why: String)                       = new NotFound(why)
  def NotFound(method: String, path: String)      = new NotFound(method, path)
  def Error                                       = new Error("Internal server error")
  def Error(why: String)                          = new Error(why)
  def Error(status: Int, why: String)             = new Error(status, why)
  def BadRequest                                  = new BadRequest()
  def Unauthorized                                = new Unauthorized("Secure")
  def Unauthorized(area: String)                  = new Unauthorized(area)
  def Html(html: Any)                             = new RenderHtml( if(html != null) html.toString else "" )
  def Xml(document: org.w3c.dom.Document)         = new RenderXml(document)
  def Xml(xml: Any)                               = new RenderXml( if(xml != null) xml.toString else "<empty/>" )
  def Json(json: String)                          = new RenderJson(json)
  def Json(o: Any)                                = new RenderJson(new com.google.gson.Gson().toJson(o))
  def Text(content: Any)                          = new RenderText(if(content != null) content.toString else "")
  def Redirect(url: String)                       = new Redirect(url)
  def Redirect(url: String, permanent: Boolean)   = new Redirect(url, permanent)
  def Template                                    = new ScalaRenderTemplate()
  def Template(args: Any*)                        = new ScalaRenderTemplate(args =  ScalaController.argsToParams(args: _*))
  def Action(action: => Any)                      = new ScalaAction(action)
  def Continue                                    = new NoResult()
  def Suspend(s: String)                          = new ScalaSuspend(s)
  def Suspend(t: Int)                             = new ScalaSuspend(t)

  // -- Shortcuts
  def @@(action: => Any)                          = Action(action)
  def ^                                           = new ScalaRenderTemplate()
  def ^(args: Any*)                               = new ScalaRenderTemplate(args = ScalaController.argsToParams(args: _*))

  /**
   * @returns a play request object
   */
  def request = Request.current()

  /**
   * @returns a play response object
   */
  def response = Response.current()

  /**
   * @returns a session object
   */
  def session = Session.current()

  /**
   * @returns a flash object
   */
  def flash = Flash.current()

  /**
   * @returns parameters
   */
  def params = Params.current()

  /**
   * @returns render argument object
   */
  def renderArgs = RenderArgs.current()

  /**
   * @returns Validation
   */
  def validation = Validation.current()

  def reverse(action: => Any): play.mvc.Router.ActionDefinition = {
      val actionDefinition = reverse()
      action
      actionDefinition
  }
  
}

object ScalaController {

    def argsToParams(args: Any*) = {
        val params = new java.util.HashMap[String,AnyRef]
        for(o <- args) {
            o match {
                  case (name: String, value: Any) => params.put(name, value.asInstanceOf[AnyRef])
                  case _ => val names = LocalVariablesNamesTracer.getAllLocalVariableNames(o)
                            for (name <- names) {
                                params.put(name, o.asInstanceOf[AnyRef])
                            }
              }
        }
        params
    }
    
}
