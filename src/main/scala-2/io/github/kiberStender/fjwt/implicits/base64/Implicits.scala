package io.github.kiberStender
package fjwt
package implicits
package base64

import cats.{Applicative, ApplicativeError}
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.exception.JWTException.NotMappedException
import org.apache.commons.codec.binary.Base64

object Implicits {

  /** A [[Base64Encoder]] instance implemented with Apache Common Codec library
    * @tparam F
    *   The effect tupe
    * @return
    *   An instance of [[Base64Encoder]]
    */
  implicit def apacheCommonEncoder[F[*]: Applicative]: Base64Encoder[F] = new Base64Encoder[F] {
    def encode(data: Array[Byte]): F[String] = (Base64 encodeBase64String data).pure[F]

    def encodeURLSafe(data: Array[Byte]): F[String] =
      (Base64 encodeBase64URLSafeString data).pure[F]
  }

  /** A [[Base64Decoder]] instance implemented with Apache Common Codec library
    * @tparam F
    *   The effect type
    * @return
    *   An instance of [[Base64Decoder]]
    */
  implicit def apacheCommonDecoder[F[*]: ApplicativeError[*[_], Throwable]]: Base64Decoder[F] =
    (str: String) =>
      try {
        new String(Base64 decodeBase64 str).pure[F]
      } catch {
        case iae: IllegalArgumentException =>
          NotMappedException(iae.getMessage).raiseError[F, String]
      }
}
