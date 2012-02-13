package pollcat

import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle.Intent
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST
/**
 * This module provides read/write access to poll info
 * Poll questions are stored in redis in the following format
 * pollcat:{poll-name}:qs:{id} -> { q -> question, votes -> count }
 * The id is generated from the incremented counter key count:pollcat:qs
 */
object Poll extends DefaultLogging {
  import unfiltered.request.QParams._

  val DefaultPoll = "talk"
  
  type QMap = Map[String, String]

  private val OkStatus = Json("status" -> 200)

  private val NotFoundStatus = Json(("status" -> 404))

  private val EventId = Config("mu.event_id")

  val questions: Intent[Any, Any] = {
    case GET(Path("/polls")) & Params(p) &
      CookieToken(ClientToken(tok, sec, Some(_), Some(mid)))  =>
      Store { s =>
        val keys = s.keys(
          "pollcat:%s:qs:*" format DefaultPoll).flatten.flatten
        questionResponse(((List.empty[QMap] /: keys) {
            (a,e) => s.hgetall(e).getOrElse(
              Map.empty[String, String]) + ("id" -> e) :: a
          }))
      }

      
    case POST(Path("/current")) & Params(p) &
      CookieToken(ClientToken(tok, sec, Some(_), Some(mid))) =>
      val expecting = for {
        name <- lookup("name") is required("missing name")
        q <- lookup("q") is required("missing q")
        host <- external("host", Some(Meetup.hosting(mid, EventId))) is
            pred({ h => h /* valid only when true */}, { h => "must be a host to perform this action" })
      } yield {
        Store { s =>
          val key = "pollcat:%s:qs:%s" format(DefaultPoll, q.get)
          if(s.exists(key)) {
            val value = "curr:%s:%s" format(q.get, "") 
            log.info("publishing %s on chan (key) %s" format(value, DefaultPoll))
            Cat.publish(DefaultPoll, value)
          } else {
            log.info("%s attempted to set invalid question %s as the current question" format(mid, key))
          }
          OkStatus
        }
      }
      expecting(p) orFail withErrors


    case POST(Path("/questions")) & Params(p) & 
      CookieToken(ClientToken(tok, sec, Some(_), Some(mid))) =>
        val expecting = for {
          poll <- lookup("poll") is required("missing poll") is
            nonempty("missing poll") is
            pred(DefaultPoll.equalsIgnoreCase,
                 _ + " must be %s".format(DefaultPoll))
          q <- lookup("q") is required("missing q") is
            nonempty("missing q")
          host <- external("host", Some(Meetup.hosting(mid, EventId))) is
            pred({ h => h /* valid only when true */}, { h => "must be a host to perform this action" })
        } yield {
          Store { s =>
            val key = "pollcat:%s:qs:%s" format(DefaultPoll, q.get)
            if(s.exists(key)) {
              log.info("deleting question %s" format key)
              s.del(key)
              OkStatus
            } else NotFoundStatus
          }
        }
        expecting(p) orFail withErrors
  }

  val votes: Intent[Any, Any] = {
    case POST(Path("/votes")) & Params(p) &
      CookieToken(ClientToken(tok, sec, Some(_), Some(mid))) =>
      val expecting = for {
        name <- lookup("poll") is
          required("missing poll") is
          nonempty("missing poll") is
          pred(DefaultPoll.equalsIgnoreCase,
               _ + " must be %s".format(DefaultPoll))
        q <- lookup("q") is required("missing q") is
          nonempty("missing q")
        v <- lookup("v") is required("missing v") is
          pred(Seq("up", "down").contains, _ + " must be one of + or _")
      } yield {
        Store { s =>
          val key = "pollcat:%s:qs:%s" format(DefaultPoll, q.get)
          if(s.exists(key)) {
            s.hincrby(key, "votes", if("up".equals(v.get)) 1 else -1)
            OkStatus
          } else NotFoundStatus
        }
      }
      expecting(p) orFail withErrors
  }

  val ask: Intent[Any, Any] = {
    case POST(Path("/polls")) & Params(p) &
      CookieToken(ClientToken(tok, sec, Some(_), Some(mid))) =>
      val expecting = for {
        name <- lookup("name") is required("missing name")
        q <- lookup("q") is required("missing q")
      } yield {
        Store { s =>
          val id = s.incr("count:pollcat:qs").getOrElse(1)
          val key = "pollcat:%s:qs:%s" format(DefaultPoll, id)
          s.hmset(key, Map(
            "q" -> q.get.trim, "votes" -> 0
          ))
          val value = "ask:%s:%s" format(id, q.get) 
          log.info("publishing %s on chan (key) %s" format(value, DefaultPoll))
          Cat.publish(DefaultPoll, value)
          OkStatus
        }
      }
      expecting(p) orFail withErrors
  }

  private def withErrors(errs: Seq[Fail[_]]) =
    Json((("status" -> 400) ~ ("f" -> "u") /*:)*/ /: errs)(
      (a:JsonAST.JObject, e: Fail[_]) => (a ~ (e.name -> e.error.toString))))

  private def questionResponse(qs: Seq[QMap]) = {
    var js = qs.map { m =>
      ("id"-> m("id")) ~
      ("text" -> m("q")) ~
      ("votes" -> m("votes"))
    }
    Json(js)
  }
}
