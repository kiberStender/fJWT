package io.github.kiberStender
package fjwt
package crypto
package hs

import cats.{Applicative, ApplicativeError}
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.circe.{Decoder, DecodingFailure, HCursor}
import org.apache.commons.codec.digest.HmacUtils

import java.nio.charset.StandardCharsets
import javax.crypto.{Mac, SecretKey}
import javax.crypto.spec.SecretKeySpec

/** Enum to narrow down the possible hmac algorithms
  */
enum HmacEncoderAlgorithms(val alg: String, val fullName: String):
  case HmacSHA1 extends HmacEncoderAlgorithms("HS1", "HmacSHA1")
  case HmacSHA224 extends HmacEncoderAlgorithms("HS224", "HmacSHA224")
  case HmacSHA256 extends HmacEncoderAlgorithms("HS256", "HmacSHA256")
  case HmacSHA384 extends HmacEncoderAlgorithms("HS384", "HmacSHA384")
  case HmacSHA512 extends HmacEncoderAlgorithms("HS512", "HmacSHA512")

object HmacEncoderAlgorithms:
  extension [F[*]: [F[*]] =>> ApplicativeError[F, Throwable]](alg: HmacEncoderAlgorithms) {
    private[fjwt] def encode(privateKey: String)(str: String): F[Array[Byte]] =
      try {
        new HmacUtils(alg.fullName, privateKey).hmac(str).pure[F]
      } catch {
        case error: Throwable => error.raiseError[F, Array[Byte]]
      }
  }
