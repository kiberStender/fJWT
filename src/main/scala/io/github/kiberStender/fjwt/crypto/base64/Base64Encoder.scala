package io.github.kiberStender
package fjwt
package crypto
package base64

import cats.Applicative
import cats.syntax.all.{catsSyntaxApplicativeId, toFunctorOps}
import org.apache.commons.codec.binary.Base64

import java.nio.charset.StandardCharsets

/** A trait describing a [[Base64Encoder]]
  * @tparam F
  *   A given container that wraps the return type
  */
trait Base64Encoder[F[*]]:
  /** A method to encode a given string using Base64 algorithm
    *
    * @param str
    *   The String to be encoded in Base64 algorithm
    * @return
    *   A String encoded in Base64 algorithm wrapped in F
    */
  def encode(str: String): F[String]

  /** A method to encode a given string using Base64 algorithm
    *
    * @param data`
    *   The [[Array]][[Byte]] to be encoded in Base64 algorithm
    * @return
    *   A String encoded in Base64 algorithm wrapped in F
    */
  def encode(data: Array[Byte]): F[String]

  /** A method to encode a given string using Base64 URL safe algorithm
    * @param str
    *   The String to be encoded in Base64 algorithm
    * @return
    *   A String encoded in Base64 algorithm wrapped in F
    */
  def encodeURLSafe(str: String): F[String]

  /** A method to encode a given string using Base64 URL safe algorithm
    *
    * @param data
    *   The [[Array]][[Byte]] to be encoded in Base64 algorithm
    * @return
    *   A String encoded in Base64 algorithm wrapped in F
    */
  def encodeURLSafe(data: Array[Byte]): F[String]

/** Instance factory for [[Base64Encoder]]
  */
object Base64Encoder:

  /** A method to instantiate a Base64Encoder
    * @tparam F
    *   An instance of [[Applicative}[F]]
    * @return
    *   The instance of [[Base64Encoder]][F]
    */
  def dsl[F[*]: Applicative]: Base64Encoder[F] = new Base64Encoder[F]:

    def encode(data: Array[Byte]): F[String] =
      (Base64 encodeBase64String data).pure[F]

    def encode(str: String): F[String] = encode(str.toBytesUTF8)

    def encodeURLSafe(data: Array[Byte]): F[String] =
      (Base64 encodeBase64URLSafeString data).pure[F]

    def encodeURLSafe(str: String): F[String] = encodeURLSafe(str.toBytesUTF8)
