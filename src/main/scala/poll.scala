package pollcat

import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle

object Poll {
  val questions: Cycle.Intent[Any, Any] = {
    case GET(Path("/poll")) => JsonContent ~> ResponseString("{}")
  }
}
