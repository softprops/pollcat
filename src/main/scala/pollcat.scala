package pollcat

object Pollcat extends DefaultLogging with ManagedHttp {
  import unfiltered.Cookie
  import unfiltered.Cycle.Intent
  import unfiltered.request._
  import unfiltered.response._
  import QParams._
  import dispatch.meetup.Auth
  import dispatch.oauth.Token

  def authentication: Intent[Any, Any] = {

    case GET(Path("/login")) =>
      val callback = "%s/authenticated" format Config("host")
      val token = http(Auth.request_token(Meetup.consumer, callback))
      val cookie = Cookie("token", ClientToken(token.value, token.secret, None, None).toCookieString)
      ResponseCookies(cookie) ~>
        ResponseCookies(SignedCookies.sign(cookie)) ~>
          Redirect(Auth.authenticate_url(token).to_uri.toString)

    case req @ GET(Path("/authenticated")) & Params(params) =>
      val expected = for {
        verifier <- lookup("oauth_verifier") is required("required")
        token <- lookup("oauth_token") is required("required")
      } yield {
        CookieToken(req) match {
          case Some(rt) =>
            val at = http(Auth.access_token(
              Meetup.consumer, Token(rt.value, rt.sec), verifier.get))
            val tokenCookie = Cookie(
              "token",
              ClientToken(
                at.value, at.secret,
                verifier, Some(Meetup.member_id(at).toString)).toCookieString)
            ResponseCookies(tokenCookie) ~>
              ResponseCookies(SignedCookies.sign(tokenCookie)) ~>  Redirect("/")
          case _ => sys.error("could not extract request token")
        }
      }
      expected(params) orFail { errors =>
        BadRequest ~> ResponseString(errors.map { _.error } mkString(". "))
      }

    case Path("/logout") =>
      ResponseCookies(Cookie("token", "", maxAge = Some(0))) 
        ResponseCookies(Cookie("token.sig", "", maxAge = Some(0))) ~> Redirect("/")
  }

  def browser: Intent[Any, Any] = {
    case req @ Path("/") =>
      req match {
        case CookieToken(ClientToken(tok, sec, Some(_), Some(mid))) =>
          val eventId = Config("mu.event_id")
          if(Meetup.rsvped(eventId, Token(tok,sec))) Views.index(Meetup.hosting(mid, eventId))
          else Views.sry
        case _ => Views.alien
      }
  }
}
