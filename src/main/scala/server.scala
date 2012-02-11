package pollcat

import unfiltered.netty.Http
import unfiltered.netty.cycle.Planify
import unfiltered.util.Browser
import java.net.URL
import System.{ getenv => Env }

object Server {
  def main(a: Array[String]) {
    val intents =  Seq(
      Poll.questions, Poll.ask, Poll.votes,
      Pollcat.authentication, Pollcat.browser)
    Http(Option(Env("PORT")).map(_.toInt).getOrElse(8080))
      .resources(new URL(getClass().getResource("/www/robots.txt"), "."))
      .handler(Cat.websockets onPass(_.sendUpstream(_)))
      .handler(Planify(
        (intents.head /: intents.tail)(_ orElse _)
      ))
      .run({ s => if(a.contains("-b")) Browser.open(s.url) }, afterStop = {
        _ => Cat.shutdown()
      })
  }
}