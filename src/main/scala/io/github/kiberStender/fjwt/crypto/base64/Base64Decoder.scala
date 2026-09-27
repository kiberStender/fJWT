package io.github.kiberStender
package fjwt
package crypto
package base64

/** A typeclass defining an effectful operation for decoding Base64-encoded
  * strings.
  *
  * Within the FJWT library, this trait abstracts the Base64 decoding process
  * required to read a JWT's Header and Payload. Because JWTs utilize Base64Url
  * encoding without padding, implementations of this trait should safely handle
  * URL-safe decoding.
  *
  * By returning the decoded string wrapped in the effect type `F`,
  * implementations can gracefully handle decoding failures (such as malformed
  * input, invalid characters, or incorrect padding) by lifting exceptions into
  * the effect system (e.g., via `MonadError` or `ApplicativeError`) rather than
  * throwing runtime exceptions.
  *
  * @tparam F
  *   The effect type constructor (e.g., `cats.effect.IO`, `scala.util.Try`).
  *
  * @example
  *   {{{
  * // Scala 3 example using java.util.Base64 and ApplicativeError
  * import java.util.Base64
  * import cats.ApplicativeError
  * import cats.syntax.all.*
  *
  * given [F[_]](using ae: ApplicativeError[F, Throwable]): Base64Decoder[F] with
  *   def decode(str: String): F[String] =
  *     try
  *       // JWTs typically use URL-safe Base64
  *       val decodedBytes = Base64.getUrlDecoder.decode(str)
  *       new String(decodedBytes).pure[F]
  *     catch
  *       case ex: IllegalArgumentException =>
  *         new RuntimeException(s"Invalid Base64 payload: $${ex.getMessage}").raiseError[F, String]
  *   }}}
  */
trait Base64Decoder[F[*]] {

  /** Decodes a Base64 string back into its original UTF-8 String
    * representation.
    *
    * @param str
    *   The Base64 (or Base64Url) encoded string.
    * @return
    *   The decoded string suspended in the effect `F`.
    */
  def decode(str: String): F[String]
}
