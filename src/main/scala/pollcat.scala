package pollcat

import unfiltered.Cycle
import unfiltered.request._
import unfiltered.response._

object Pollcat {
  def browser: Cycle.Intent[Any, Any] = {
    case Path("/") => Templates.index
  }
}

object Templates {
  import xml._

  def css(path: String) =
    <link rel="stylesheet" type="text/css" href={ path } />

  def script(path: String) =
    <script  type="text/javascript" src={ path }></script>

  def apply(title: String)(
    contents: NodeSeq)(styles: String*)(scripts: String*) =
    Html(
      <html>
        <head>
          <title>{ title }</title>
          { css("http://fonts.googleapis.com/css?family=Ubuntu+Mono") }
          { css("/css/pollcat.css") }          
          { styles map(css) }
          { script("https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js") }
        </head>
        <body>
          <div id="container">{ contents }</div>
          { script("/js/pollcat.js") }
          { scripts map(script) }
        </body>
      </html>
    )

  val index = apply("pollcat")(<div>pollcat</div>)()()
}
