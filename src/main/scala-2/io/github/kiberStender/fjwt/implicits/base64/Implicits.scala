package io.github.kiberStender
package fjwt
package implicits
package base64

import cats.{Applicative, ApplicativeError}
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.exception.JWTException.NotMappedException
import org.apache.commons.codec.binary.Base64

/** A utility object containing out-of-the-box production-ready implementations of core FJWT
  * typeclasses backed by Apache Commons Codec.
  *
  * This object provides standard implicit/given instances for `Base64Encoder` and `Base64Decoder`,
  * allowing you to seamlessly integrate Apache Commons Base64 operations into your effect-agnostic
  * (`F[_]`) workflows without having to write custom adapter boilerplate.
  *
  * @example
  *   {{{
  * // Importing the provided Apache Commons implementations into scope
  * import com.example.fjwt.Implicits._
  * import cats.effect.IO
  *
  * // Base64Encoder and Base64Decoder are now implicitly available for IO
  *   }}}
  */
object Implicits {

  /** Provides an implicit `Base64Encoder` instance backed by Apache Commons Codec.
    *
    * This encoder uses Apache's `Base64.encodeBase64String` for standard encoding and
    * `Base64.encodeBase64URLSafeString` for URL-safe JWT-compliant encoding, lifting the results
    * into the effect type `F` using its `Applicative` instance.
    *
    * @tparam F
    *   The effect type constructor, which must have an instance of `cats.Applicative`.
    * @return
    *   An instance of `Base64Encoder[F]` powered by Apache Commons Codec.
    */
  implicit def apacheCommonEncoder[F[*]: Applicative]: Base64Encoder[F] = new Base64Encoder[F] {
    def encode(data: Array[Byte]): F[String] = (Base64 encodeBase64String data).pure[F]

    def encodeURLSafe(data: Array[Byte]): F[String] =
      (Base64 encodeBase64URLSafeString data).pure[F]
  }

  /** Provides an implicit `Base64Decoder` instance backed by Apache Commons Codec.
    *
    * This decoder uses Apache's `Base64.decodeBase64` to parse base64-encoded strings. Any
    * `IllegalArgumentException` thrown during decoding (such as malformed input or invalid padding)
    * is safely caught and mapped into a domain-specific [[JWTException.NotMappedException]] raised
    * natively within the effect system `F`.
    *
    * @tparam F
    *   The effect type constructor, which must have an instance of `cats.ApplicativeError[F,
    *   Throwable]`.
    * @return
    *   An instance of `Base64Decoder[F]` powered by Apache Commons Codec with error handling.
    */
  implicit def apacheCommonDecoder[F[*]: ApplicativeError[*[*], Throwable]]: Base64Decoder[F] =
    (str: String) =>
      try {
        new String(Base64 decodeBase64 str).pure[F]
      } catch {
        case iae: IllegalArgumentException =>
          NotMappedException(iae.getMessage).raiseError[F, String]
      }
}
