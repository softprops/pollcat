package pollcat

import unfiltered.netty.Http
import unfiltered.netty.cycle.Planify
import unfiltered.util.Browser
import java.net.URL
import System.{ getenv => Env }

object Server {
  def main(a: Array[String]) {
    Http(Option(Env("PORT")).map(_.toInt).getOrElse(8080))
      .resources(new URL(getClass().getResource("/www/robots.txt"), "."))
      .handler(Cat.websockets onPass(_.sendUpstream(_)))
      .handler(Planify(Poll.questions orElse Pollcat.browser))
      .run({ s => if(a.contains("-b")) Browser.open(s.url) })
  }
}
