package pollcat

trait ManagedHttp {
  import dispatch._
  def http[T](h: Handler[T]) = {
    val exec = new Http
    try { exec(h) }
    finally { exec.shutdown() }
  }
}
