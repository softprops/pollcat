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

  def apply(title: String, authed: Boolean = false)(
    contents: NodeSeq)(styles: Seq[String] = Nil)(scripts: Seq[String] = Nil) =
    Html(
      <html>
        <head>
          <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/> 
          <title>{ title }</title>
          { css("/css/pollcat.css") }          
          { styles map(css) }
          { script(jQuery) }
        </head>
        <body>
          <div id="head">
            <div class="contained">
              <h1>Option[Scala]</h1>
              <p class="subtle-in-dark">Adding Scala to yr companies stack</p>
            </div>
          </div>
          <div id="sub-head">
            <div id="auth" class="contained">{
              if(authed) <a class="btn" href="/logout">Log out</a>
              else <a class="btn" id="login" href="/login">Login with Meetup</a>
            }</div>
          </div>
          <div id="contents" class="contained">{ contents }</div>
          { scripts map(script) }
        </body>
      </html>
    )

  def index(admin: Boolean) = apply("option scala", authed = true)(
    <div id="currently">
      <h2>Currently asking</h2>
      <div id="asking">nothing</div>
    </div>
    <form id="ask" action="/polls" method="POST">      
      <input name="name" type="hidden" value="talk"/>
      <textarea name="q" id="q" maxlength="255"/>
      <input type="submit" value="Ask a Question" class="btn"/>
    </form>
    <div class="clearfix">
      <h2>Questions</h2>
      <ul id="questions"></ul>
    </div>
  )()(if(admin) Seq("/js/pollcat.js", "/js/admin.js") else Seq("/js/pollcat.js"))

  val alien = apply("option scala")(<div id="what-is-this">curious? login.</div>)()()

  val sry = apply("option scala")(<div id="what-is-this">sorry. looks like you're not RSVP'd :/</div>)()()
}
