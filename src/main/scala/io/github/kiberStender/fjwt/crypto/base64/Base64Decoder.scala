package io.github.kiberStender 
package fjwt
package crypto
package base64

import io.github.kiberStender.fjwt.error.JWTError.NotMappedError

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}

import java.util.Base64

trait Base64Decoder[F[*]]:
  def decode(str: String): F[String]

object Base64Decoder:
  private lazy val decoder: Base64.Decoder = Base64.getDecoder

  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: Base64Decoder[F] = (str: String) =>
    try {
      new String(decoder decode str).pure[F]
    } catch {
      case iae: IllegalArgumentException => NotMappedError(iae.getMessage).raiseError[F, String]
    }
