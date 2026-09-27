package io.github.kiberStender
package fjwt
package crypto
package hmac

import io.github.kiberStender.fjwt.models.Header

/** A typeclass defining the cryptographic hashing operations required to sign
  * and verify JWTs.
  *
  * Within the FJWT library, this trait abstracts the creation of the signature
  * portion of the JSON Web Token. It is responsible for taking the encoded
  * Header and Payload, along with a secret key, and producing a cryptographic
  * hash (typically an HMAC, as the name implies) based on the algorithm
  * specified in the token's Header.
  *
  * By suspending these operations in the effect type `F`, implementations can
  * safely handle cryptographic failures (such as unsupported algorithms or
  * invalid key formats) natively within the chosen effect system (e.g., via
  * `MonadError`) rather than throwing runtime exceptions.
  *
  * @tparam F
  *   The effect type constructor (e.g., `cats.effect.IO`, `scala.util.Try`).
  *
  * @example
  *   {{{
  * // Scala 3 example using standard javax.crypto.Mac and ApplicativeError
  * import javax.crypto.Mac
  * import javax.crypto.spec.SecretKeySpec
  * import cats.ApplicativeError
  * import cats.syntax.all.*
  *
  * given [F[_]](using ae: ApplicativeError[F, Throwable]): Hmac[F] with
  *   def extractAlg(header: Header): F[String] = header.alg match
  *     case "HS256" => "HmacSHA256".pure[F]
  *     case "HS384" => "HmacSHA384".pure[F]
  *     case "HS512" => "HmacSHA512".pure[F]
  *     case other   => new IllegalArgumentException(s"Unsupported alg: $$other").raiseError[F, String]
  *
  *   def hash(header: Header)(privateKey: String)(str: String): F[Array[Byte]] =
  *     extractAlg(header).flatMap { alg =>
  *       try
  *         val mac = Mac.getInstance(alg)
  *         val secretKey = new SecretKeySpec(privateKey.getBytes("UTF-8"), alg)
  *         mac.init(secretKey)
  *         mac.doFinal(str.getBytes("UTF-8")).pure[F]
  *       catch
  *         case ex: Throwable => ex.raiseError[F, Array[Byte]]
  *     }
  *   }}}
  */
trait Hmac[F[*]] {

  /** Generates a cryptographic hash for the given string using the specified
    * secret key and the algorithm defined in the token's header.
    *
    * In a JWT context, `str` is typically the concatenated Base64Url-encoded
    * Header and Payload (e.g., `"encodedHeader.encodedPayload"`).
    *
    * @param header
    *   The decoded JWT Header, which contains the algorithm (`alg`)
    *   specification.
    * @param privateKey
    *   The secret key used to sign the token.
    * @param str
    *   The raw string payload to be hashed.
    * @return
    *   The resulting cryptographic hash as a byte array, suspended in the
    *   effect `F`.
    */
  def hash(header: Header)(privateKey: String)(str: String): F[Array[Byte]]
}
