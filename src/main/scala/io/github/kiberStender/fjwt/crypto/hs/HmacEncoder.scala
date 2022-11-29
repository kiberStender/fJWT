package io.github.kiberStender
package fjwt
package crypto
package hs

import cats.syntax.all.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import cats.Applicative

import io.github.kiberStender.fjwt.error.JWTError
import org.apache.commons.codec.digest.HmacUtils

/** A trait that defines an {@link HmacEncoder} typeclass
  * @tparam F
  *   The container type to wrap the return value
  */
trait HmacEncoder[F[*]]:
  /** The algorithm to be used when encoding
    * @return
    */
  def alg: HmacEncoderAlgorithms

  /** A method to encode a given {@link String}
    * @param privateKey
    *   The key to be used when encoding the payload
    * @param payload
    *   The String to be encoded
    * @return
    *   The encoded String wrapped in a container of type F
    */
  def encode(privateKey: String)(payload: String): F[String]

/** Instance factory for {@link HmacEncoder}
  */
object HmacEncoder:
  /** An HmacEncoder instance that uses Sha1 algorithm
    * @tparam F
    *   An {@link Applicative}[F] instance
    * @return
    *   An {@link HmacEncoder}[F] implemented with SHA1
    */
  def hs1Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacSHA1
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  /** An HmacEncoder instance that uses Sha224 algorithm
    *
    * @tparam F
    *   An {@link Applicative}[F] instance
    * @return
    *   An {@link HmacEncoder}[F] implemented with SHA224
    */
  def hs224Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacSHA224
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  /** An HmacEncoder instance that uses Sha256 algorithm
    *
    * @tparam F
    *   An {@link Applicative}[F] instance
    * @return
    *   An {@link HmacEncoder}[F] implemented with SHA256
    */
  def hs256Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacSHA256
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  /** An HmacEncoder instance that uses Sha384 algorithm
    *
    * @tparam F
    *   An {@link Applicative}[F] instance
    * @return
    *   An {@link HmacEncoder}[F] implemented with SHA384
    */
  def hs384Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacSHA384
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  /** An HmacEncoder instance that uses Sha512 algorithm
    *
    * @tparam F
    *   An {@link Applicative}[F] instance
    * @return
    *   An {@link HmacEncoder}[F] implemented with SHA512
    */
  def hs512Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacSHA512
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  /** An HmacEncoder instance that uses MD5 algorithm
    *
    * @tparam F
    *   An {@link Applicative}[F] instance
    * @return
    *   An {@link HmacEncoder}[F] implemented with MD5
    */
  def hMD5Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacMD5
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  /** A generic way to instantiate an {@link HmacEncoder}[F] instance by providing the Hmac
    * algorithm
    * @param algorithms
    *   The algorithm to encode the data
    * @tparam F
    *   An {@link Applicative}[F] instance
    * @return
    *   An {@link HmacEncoder}[F] implemented with the given algorithm
    */
  def hsEncoder[F[*]: Applicative](
      algorithms: HmacEncoderAlgorithms
  ): HmacEncoder[F] =
    new HmacEncoder[F]:
      val alg: HmacEncoderAlgorithms = algorithms
      def encode(privateKey: String)(payload: String): F[String] =
        hmacEncoder(alg)(privateKey)(payload)

  private def hmacEncoder[F[*]: Applicative](
      alg: HmacEncoderAlgorithms
  )(privateKey: String)(str: String): F[String] =
    new HmacUtils(alg.alg, privateKey).hmacHex(str).pure[F]
