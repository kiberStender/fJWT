package br.com.fjwt
package crypto
package base64

import br.com.fjwt.error.JWTError

import cats.ApplicativeError
import cats.syntax.all.toFunctorOps
import cats.syntax.all.catsSyntaxApplicativeId

import java.util.Base64

trait Base64Encoder[F[*]]:
  def encode(str: String): F[String]

object Base64Encoder:
  private lazy val encoder: Base64.Encoder = Base64.getEncoder
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, JWTError]]: Base64Encoder[F] = (str: String) =>
    (encoder encodeToString str.getBytes()).pure[F]
