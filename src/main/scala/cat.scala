package pollcat

object Cat extends DefaultLogging {
  import unfiltered.netty.websockets._
  import unfiltered.request.{ GET, Path, Seg }
  import scala.collection.mutable.ListBuffer
  import scala.collection.JavaConversions._
  import java.util.concurrent._

  private lazy val listeners = new JConcurrentMapWrapper(
    new ConcurrentHashMap[String, List[WebSocket]]) {
      override def default(k: String) = List.empty[WebSocket]
    }

  private def write(in: String, msg: String) =
    listeners(in).foreach(_.send(msg))

  def publish(chan: String, msg: String) =
    msg.split(':') match {
      case Array("up" | "down" | "ask", _*) => write(chan, msg)
      case mm => log.info("discarding malformed msg %s" format mm)
    }

  /** Exposes a websocket plan that subscribes clients
   *  to poll change notifications */
  val websockets = Planify {
    case GET(Path(Seg("cat" :: poll :: Nil)))
      if(poll.equalsIgnoreCase(Poll.DefaultPoll)) => {
      case Open(s) => listeners += (
        poll -> (s :: listeners(poll))
      )
      case Close(s) => listeners += (
        poll -> listeners(poll).filterNot(_ == s)
      )
    }
  }
}
