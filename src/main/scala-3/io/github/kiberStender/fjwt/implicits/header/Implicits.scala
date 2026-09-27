package io.github.kiberStender
package fjwt
package implicits
package header

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, toFunctorOps}
import io.github.kiberStender.fjwt.crypto.hmac.Hmac
import io.github.kiberStender.fjwt.models.Header
import org.apache.commons.codec.digest.HmacUtils

/** A utility container object providing out-of-the-box implementations of the [[Hmac]] typeclass
  * backed by Apache Commons Codec.
  */
object Implicits:

  /** Provides an implicit `Hmac` typeclass instance powered by Apache Commons Codec's `HmacUtils`.
    *
    * This implementation handles cryptographic signing by mapping standard JWT algorithm strings
    * (such as `"HS256"` or `"HS512"`) to their corresponding JVM/Commons algorithm identifiers
    * (such as `"HmacSHA256"` or `"HmacSHA512"`). It uses `HmacUtils` to compute the HMAC hash using
    * the provided `privateKey` and input string.
    *
    * Any cryptographic or initialization errors encountered during hashing are safely caught and
    * lifted into the effect type `F` as a suspended error using `ApplicativeError`.
    *
    * @tparam F
    *   The effect type constructor, requiring an instance of `cats.ApplicativeError[F, Throwable]`.
    * @return
    *   An instance of `Hmac[F]` implemented using Apache Commons Codec.
    */
  given hmacEncoderApacheCommons[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: Hmac[F] with
    def hash(header: Header)(privateKey: String)(str: String): F[Array[Byte]] =
      try {
        extractAlg(header).map(new HmacUtils(_, privateKey).hmac(str))
      } catch {
        case error: Throwable => error.raiseError[F, Array[Byte]]
      }

    def extractAlg(header: Header): F[String] = header.alg match
      case "HS1"   => "HmacSHA1".pure[F]
      case "HS224" => "HmacSHA224".pure[F]
      case "HS256" => "HmacSHA256".pure[F]
      case "HS384" => "HmacSHA384".pure[F]
      case "HS512" => "HmacSHA512".pure[F]
