package pollcat

object Meetup {
  import dispatch._
  import meetup._
  import dispatch.liftjson.Js._
  import oauth._
  import Http._

  import net.liftweb.json.JsonAST._
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.JsonParser._

  val DefaultImage = "http://img1.meetupstatic.com/39194172310009655/img/noPhoto_50.gif"

  lazy val consumer = Consumer(
    Config("mu.consumer_key"), Config("mu.consumer_secret"))

  val client: Client = APIKeyClient(Config("mu.api_key"))

  def http = Http

  def rsvped(eventId: String, tok: oauth.Token) = {
    val mu = OAuthClient(consumer, tok)
    val (res, _) = http(mu.handle(Events.id(eventId)))
    res.flatMap(Event.myrsvp).contains("yes")
  }

  case class SimpleMember(id: String, name: String, photo: String)

  def members(ids: Traversable[String]) = {
    val (res, _) = http(client.handle(Members.member_id(ids.mkString(","))))
    for {
      r <- res
      id <- Member.id(r)
      name <- Member.name(r)
      photo <- Member.photo_url(r)
    } yield {
      id -> SimpleMember(id, name, if(photo.isEmpty) DefaultImage else photo)
    }
  }

  def member_id(tok: oauth.Token) = {
    val mu = OAuthClient(consumer, tok)
    val (res, _) = http(mu.handle(Members.self))
    res.flatMap(Member.id).apply(0).toInt
  }

  def hosting(memberId: String, eventId: String) =
    hosts(eventId).contains(memberId.toInt)

  def hosts(eventId: String) =  {
    val (res, _) = http(client.handle(Events.id(eventId)))
    for {
      e <- res
      JArray(hosts) <- e \ "event_hosts"
      h <- hosts
      JInt(id) <- h \ "member_id"
    } yield id
  }

}
