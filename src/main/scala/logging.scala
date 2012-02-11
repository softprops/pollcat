package pollcat

trait Log {
  def info(m: => String): Unit
  def warn(m: => String): Unit
  def error(m: => String, t: Option[Throwable]): Unit
}

trait Logged {
  def log: Log
}

trait DefaultLogging extends Logged {
  val log = ConsoleLogger
}

object ConsoleLogger extends Log {
  def info(m: => String) = println("[INFO]: %s" format m)
  def warn(m: => String) = println("[WARN]: %s" format m)
  def error(m: => String, t: Option[Throwable] = None) =
    Console.err.println("[ERROR]: %s%s" format(
      m, t.map(" " + _).getOrElse("")
    ))
}
