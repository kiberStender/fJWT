package br.com.fjwt
package crypto
package base64

import br.com.fjwt.error.JWTError
import br.com.fjwt.error.JWTError.NotMappedError
import br.com.fjwt.validation.CodecValidation
import cats.*
import cats.syntax.all.*

import java.util.Base64

trait Base64Decoder[F[*]]:
  def decode(str: String): F[String]

object Base64Decoder:
  private lazy val decoder: Base64.Decoder = Base64.getDecoder

  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, JWTError]](using
      codecValidation: CodecValidation[F]
  ): Base64Decoder[F] = (str: String) =>
    try {
      for token <- codecValidation validateToken str yield new String(decoder decode token)
    } catch {
      case iae: IllegalArgumentException => NotMappedError(iae.getMessage).raiseError[F, String]
    }
