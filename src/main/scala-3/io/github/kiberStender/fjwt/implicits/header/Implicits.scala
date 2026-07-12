package io.github.kiberStender
package fjwt
package implicits
package header

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.crypto.hmac.Hmac
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import org.apache.commons.codec.digest.HmacUtils

object Implicits:

  given hmacEncoderApacheCommons[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: Hmac[F] with
    def hash(hmac: HmacAlgorithm)(privateKey: String)(str: String): F[Array[Byte]] =
      try {
        new HmacUtils(hmac.fullName, privateKey).hmac(str).pure[F]
      } catch {
        case error: Throwable => error.raiseError[F, Array[Byte]]
      }
