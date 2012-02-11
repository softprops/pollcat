package pollcat

object Store extends DefaultLogging {
  import com.redis._
  private val URI = """^redis://(\w+):(\w+)@(.*):(\d{4}).*""".r

  private lazy val (auth, clients) = {
    
    val prop = Config("redis_url")
    prop match {
      case URI(_, pass, host, port) =>
        (pass, new RedisClientPool(host, port.toInt))
      case mf =>
        sys.error("malformed redis uri: %s" format mf)
    }
  }


  def one = {
    val prop = Config("redis_url")
    val(pass, c) = prop match {
      case URI(_, pass, host, port) =>
        (pass, new RedisClient(host, port.toInt))
      case mf =>
        sys.error("malformed redis uri: %s" format mf)
    }
    def attempt: RedisClient = try {  
      c.auth(auth)
      c
    } catch {
      case e: com.redis.RedisConnectionException =>
        log.error("redis fail. attempting reconnect")
        if(c.reconnect) attempt
        else throw e
    }
    attempt
  }
    

  def apply[T](f: RedisClient => T): T =
    clients.withClient { c =>
      def attempt: T = try {
        c.auth(auth)
        f(c)
      } catch {
        case e: com.redis.RedisConnectionException =>
          log.error("redis fail. attempting reconnect")
          if(c.reconnect) attempt
          else throw e
      }
      attempt
    }
}
