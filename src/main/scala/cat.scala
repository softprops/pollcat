package pollcat

import unfiltered.netty.websockets._
import unfiltered.request.{ GET, Path, Seg }
import scala.collection.mutable.ListBuffer

object Cat extends DefaultLogging {
  import scala.collection.JavaConversions._
  import java.util.concurrent._
  import com.redis._

  private lazy val redis = new RedisClient("localhost", 6379)

  private lazy val listeners: ConcurrentMap[String, List[WebSocket]] =
    new ConcurrentHashMap[String, List[WebSocket]]

  private def write(in: String, msg: String) =
    listeners.getOrElse(in, Nil).foreach(_.send(msg))

  redis.subscribe("talk") {
    _ match {
      case S(_,_) | U(_,_) => ()
      case M(chan, msg) =>
        msg match {
          case y if y startsWith "+" => write(chan, y)
          case n if n startsWith "-" => write(chan, n)
          case uk => log.info("discarding msg %s" format uk)
        }
    }
  }

  val websockets = Planify {
    case GET(Path(Seg("cat" :: poll :: Nil))) => poll match {
      case "talk" => {
        case Open(s) => listeners += (
          poll -> (s :: listeners.getOrElse(poll, Nil))
        )
        case Close(s) => listeners += (
          poll -> listeners.getOrElse(poll, Nil).filterNot(_ == s)
        )
      }
      case _ => {
        case Open(s) =>
          log.info("rec connection for unsupported poll %s" format poll)
          s.channel.close()
      }
    }
  }
}
