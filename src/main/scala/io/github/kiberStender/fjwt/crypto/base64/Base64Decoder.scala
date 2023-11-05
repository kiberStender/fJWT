package io.github.kiberStender
package fjwt
package crypto
package base64

import io.github.kiberStender.fjwt.error.JWTError.NotMappedError

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}

import org.apache.commons.codec.binary.Base64

/** A trait describing A {@link Base64Decoder}
  * @tparam F
  *   A given container that wraps the return type
  */
trait Base64Decoder[F[*]]:
  /** Method to decode a string previously encoded in Base64
    * @param str
    *   The String to be decoded using Base64 algorithm
    * @return
    *   A String that was decoded from Base64 algorithm wrapped in F
    */
  def decode(str: String): F[String]

/** Instance factor for {@link Base64Decoder}
  */
object Base64Decoder:

  /** Generates an instance of {@link Base64Decoder}
    * @tparam F
    *   An instance of {@link ApplicativeError}[F, Throwable]
    * @return
    *   The instance of {@link Base64Decoder}[F]
    */
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: Base64Decoder[F] = (str: String) =>
    try {
      new String(Base64 decodeBase64 str).pure[F]
    } catch {
      case iae: IllegalArgumentException => NotMappedError(iae.getMessage).raiseError[F, String]
    }
