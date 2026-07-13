package io.github.kiberStender
package fjwt
package implicits
package header

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.crypto.hmac.Hmac
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import org.apache.commons.codec.digest.HmacUtils

object Implicits {

  /** A convenience instance of [[Hmac]] implemented using Apache Commons Codec
    * @tparam F
    *   The effect type
    * @return
    *   The instance of [[Hmac[F]]]
    */
  implicit def hmacEncoderApacheCommons[F[*]: ApplicativeError[*[_], Throwable]]: Hmac[F] =
    new Hmac[F] {
      def hash(hmac: HmacAlgorithm)(privateKey: String)(str: String): F[Array[Byte]] =
        try {
          new HmacUtils(hmac.fullName, privateKey).hmac(str).pure[F]
        } catch {
          case error: Throwable => error.raiseError[F, Array[Byte]]
        }
    }
}
