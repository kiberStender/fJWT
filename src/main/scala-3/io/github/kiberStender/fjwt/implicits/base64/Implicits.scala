package io.github.kiberStender
package fjwt
package implicits
package base64

import cats.{Applicative, ApplicativeError}
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.exception.JWTError.NotMappedError
import org.apache.commons.codec.binary.Base64

object Implicits:

  /** A [[Base64Encoder]] instance implemented with Apache Common Codec library
    * @tparam F
    *   The effect tupe
    * @return
    *   An instance of [[Base64Encoder]]
    */
  given apacheCommonEncoder[F[*]: Applicative]: Base64Encoder[F] with

    def encode(data: Array[Byte]): F[String] = (Base64 encodeBase64String data).pure[F]

    def encodeURLSafe(data: Array[Byte]): F[String] =
      (Base64 encodeBase64URLSafeString data).pure[F]

  /** A [[Base64Decoder]] instance implemented with Apache Common Codec library
    * @tparam F
    *   The effect type
    * @return
    *   An instance of [[Base64Decoder]]
    */
  given apacheCommonDecoder[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: Base64Decoder[F] with
    def decode(str: String): F[String] = try {
      new String(Base64.decodeBase64(str)).pure[F]
    } catch {
      case iae: IllegalArgumentException => NotMappedError(iae.getMessage).raiseError[F, String]
    }
