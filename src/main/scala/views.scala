package pollcat

object Views {
  import unfiltered.response.Html
  import xml._

  val jQuery =
    "https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"

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
          { css("/css/pollcat.css") }          
          { styles map(css) }
          { script(jQuery) }
        </head>
        <body>
          <div id="head">
            <div class="contained">
              <h1>POLLCAT</h1>
            </div>
          </div>
          <div id="contents" class="contained">{ contents }</div>
          { script("/js/pollcat.js") }
          { scripts map(script) }
        </body>
      </html>
    )

  val index = apply("pollcat")(
    <div id="auth"><a class="btn" href="/logout">Log out</a></div>
    <form id="ask" action="/polls" method="POST">
      <input name="name" type="hidden" value="talk"/>
      <textarea name="q" id="q" maxlength="255"/>
      <input type="submit" value="Ask a Question" class="btn"/>
    </form>
    <div class="clearfix">
      <h2>Questions</h2>
      <ul id="questions"></ul>
    </div>
  )()()

  val alien = apply("pollcat")(
    <div id="auth">
      <a class="btn" id="login" href="/login">Login with Meetup</a>
    </div>
  )()()
}
