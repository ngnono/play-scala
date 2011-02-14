package play {

    package object test {
        
        // Helper to deal with Yaml fixtures in a type safe way
        def Yaml[T](name: String)(implicit m: ClassManifest[T]) = {  
            
            val yamlParser = new org.yaml.snakeyaml.Yaml(
                new org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor(classOf[Object], play.Play.classloader) {

                import org.yaml.snakeyaml.nodes._

                override def constructObject(node: Node) = {
                    node match {
                        case n: ScalarNode if n.getTag.getClassName == "None" => None
                        case n: ScalarNode if n.getTag.getClassName == "Some[String]" => Some(n.getValue)
                        case n: ScalarNode if n.getTag.getClassName == "Some[Long]" => Some(java.lang.Long.parseLong(n.getValue, 10))
                        case n: ScalarNode if n.getTag.getClassName == "Some[Int]" => Some(java.lang.Integer.parseInt(n.getValue, 10))
                        case n: ScalarNode if n.getTag.getClassName == "TODO" => play.db.sql.TODO
                        case n: ScalarNode if n.getTag.getClassName == "Id[String]" => play.db.sql.Id(n.getValue)
                        case n: ScalarNode if n.getTag.getClassName == "Id[Long]" => play.db.sql.Id(java.lang.Long.parseLong(n.getValue, 10))
                        case n: ScalarNode if n.getTag.getClassName == "Id[Int]" => play.db.sql.Id(java.lang.Integer.parseInt(n.getValue, 10))
                        case _ => super.constructObject(node)
                    }                
                }

            })
            yamlParser.setBeanAccess(org.yaml.snakeyaml.introspector.BeanAccess.FIELD)
                  
            import scala.collection.JavaConversions._    
                
            m.erasure.getName match {
                case "scala.collection.immutable.List" => play.test.Fixtures.loadYaml(name, yamlParser).asInstanceOf[java.util.List[Any]].toList.asInstanceOf[T]
                case "scala.collection.immutable.Map"  => play.test.Fixtures.loadYaml(name, yamlParser).asInstanceOf[java.util.Map[Any,Any]].toMap[Any,Any].asInstanceOf[T]
                case _                                 => play.test.Fixtures.loadYaml(name, yamlParser).asInstanceOf[T]
            }
        } 

    }

}