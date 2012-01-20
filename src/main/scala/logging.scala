package pollcat

trait Log {
  def info(m: => String): Unit
}

trait Logged {
  def log: Log
}

trait DefaultLogging extends Logged {
  val log = ConsoleLogger
}

object ConsoleLogger extends Log {
  def info(m: => String) = println(m)
}
