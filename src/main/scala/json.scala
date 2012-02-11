package pollcat

trait Json {
  import sjson.json._
  import DefaultProtocol._
  import JsonSerialization._
  import dispatch.json.JsValue.{ toJson => jsonStr }

  def json(props: Map[String, String]) = jsonStr(tojson(props))
}
