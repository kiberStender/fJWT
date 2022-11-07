package io.github.kiberStender.fjwt.crypto.base64

import io.github.kiberStender.fjwt.error.JWTError.NotMappedError

import cats.*
import cats.syntax.all.*
import io.github.kiberStender.fjwt.error.JWTError

import java.util.Base64

trait Base64Decoder[F[*]]:
  def decode(str: String): F[String]

object Base64Decoder:
  private lazy val decoder: Base64.Decoder = Base64.getDecoder

  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, JWTError]]: Base64Decoder[F] = (str: String) =>
    try {
      new String(decoder decode str).pure[F]
    } catch {
      case iae: IllegalArgumentException => NotMappedError(iae.getMessage).raiseError[F, String]
    }
