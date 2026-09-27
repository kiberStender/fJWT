package io.github.kiberStender
package fjwt
package crypto
package base64

/** A typeclass defining effectful operations for Base64 and Base64Url encoding.
  *
  * Within the FJWT library, this trait is responsible for encoding the JWT
  * Header, Claim (Payload), and cryptographic Signature into string segments.
  * Because JWTs are often transmitted in HTTP headers and URLs, they strictly
  * require **URL-safe** Base64 encoding (typically without padding), which is
  * handled by the `encodeURLSafe` methods.
  *
  * The trait provides default implementations for `String` inputs, which
  * automatically convert the strings to UTF-8 byte arrays (via library
  * extensions) before delegating to the `Array[Byte]` abstract methods.
  * Implementers only need to provide the logic for the `Array[Byte]` variants.
  *
  * By wrapping the result in the effect type `F`, encoding can be seamlessly
  * chained within your monadic comprehensions alongside JSON parsing and
  * hashing.
  *
  * @tparam F
  *   The effect type constructor (e.g., `cats.effect.IO`, `scala.util.Try`).
  *
  * @example
  *   {{{
  * // Scala 3 example using standard java.util.Base64 and Applicative
  * import java.util.Base64
  * import cats.Applicative
  * import cats.syntax.all.*
  *
  * given [F[_]](using a: Applicative[F]): Base64Encoder[F] with
  *   def encode(data: Array[Byte]): F[String] =
  *     Base64.getEncoder.encodeToString(data).pure[F]
  *
  *   def encodeURLSafe(data: Array[Byte]): F[String] =
  *     // JWT strictly requires URL-safe encoding without padding
  *     Base64.getUrlEncoder.withoutPadding().encodeToString(data).pure[F]
  *   }}}
  */
trait Base64Encoder[F[*]] {

  /** Encodes a UTF-8 string into a standard Base64 string.
    *
    * This method provides a default implementation that converts the string to
    * a UTF-8 byte array and delegates to `encode(data: Array[Byte])`.
    *
    * @param str
    *   The raw string to encode.
    * @return
    *   The standard Base64 encoded string suspended in the effect `F`.
    */
  def encode(str: String): F[String] = encode(str.toBytesUTF8)

  /** Encodes a byte array into a standard Base64 string.
    *
    * @param data
    *   The raw byte array to encode.
    * @return
    *   The standard Base64 encoded string suspended in the effect `F`.
    */
  def encode(data: Array[Byte]): F[String]

  /** Encodes a UTF-8 string into a URL-safe Base64 string.
    *
    * In the context of JWTs, this encoding is specifically used to format token
    * segments (Header, Payload, Signature) and should omit padding characters
    * (`=`). This method provides a default implementation that converts the
    * string to a UTF-8 byte array and delegates to `encodeURLSafe(data:
    * Array[Byte])`.
    *
    * @param str
    *   The raw string to encode.
    * @return
    *   The URL-safe Base64 encoded string suspended in the effect `F`.
    */
  def encodeURLSafe(str: String): F[String] = encodeURLSafe(str.toBytesUTF8)

  /** Encodes a byte array into a URL-safe Base64 string.
    *
    * In the context of JWTs, this encoding is crucial for generating valid
    * token segments and should omit padding characters (`=`).
    *
    * @param data
    *   The raw byte array to encode.
    * @return
    *   The URL-safe Base64 encoded string suspended in the effect `F`.
    */
  def encodeURLSafe(data: Array[Byte]): F[String]
}
