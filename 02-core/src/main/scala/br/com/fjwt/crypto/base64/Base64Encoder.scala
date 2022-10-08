package br.com.fjwt
package crypto
package base64

import br.com.fjwt.error.JWTError
import br.com.fjwt.validation.CodecValidation

import cats.ApplicativeError
import cats.syntax.all.toFunctorOps

import java.util.Base64

trait Base64Encoder[F[*]]:
  def encode(str: String): F[String]

object Base64Encoder:
  private lazy val encoder: Base64.Encoder = Base64.getEncoder
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, JWTError]](using
      codecValidation: CodecValidation[F]
  ): Base64Encoder[F] = (str: String) =>
    for token <- codecValidation validateToken str yield encoder encodeToString token.getBytes()
