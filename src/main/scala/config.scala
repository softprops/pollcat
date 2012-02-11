package pollcat

object Config {
  private lazy val props = {
    val file = getClass.getResourceAsStream("/pc.properties")
    val props = new java.util.Properties
    props.load(file)
    file.close()
    props
  }

  def apply(name: String) = 
    get(name) match {
      case None => sys.error("missing property %s" format name)
      case Some(value) => value
    }

  def get(name: String) =
    Option(System.getenv(name)).orElse(Option(props.getProperty(name)))
    
  def int(name: String) =
    try {
      apply(name).toInt
    } catch { case nfe: NumberFormatException =>
      sys.error("%s was not an int" format get(name))
    }
}
