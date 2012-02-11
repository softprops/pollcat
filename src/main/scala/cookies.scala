package pollcat

object SignedCookies {
  import org.apache.commons.codec.binary.Base64.{ encodeBase64URLSafe => b64encode }
  import java.security.MessageDigest
  import unfiltered.Cookie
  private val SHA1 = "HmacSHA1"

  private val secret = Config("cookie_secret").getBytes("utf8")

  private def hash(clear: String) = {
    import javax.crypto
    val mac = crypto.Mac.getInstance(SHA1)
    mac.init(new crypto.spec.SecretKeySpec(secret, SHA1))
    new String(b64encode(mac.doFinal(clear.getBytes("utf8"))))
  }

  def sign(c: Cookie) =
    c.copy(name = "%s.sig" format(c.name), value = hash(c.value))

  def valid(clear: Cookie, signed: Cookie) =
    sign(clear).value == signed.value
}
