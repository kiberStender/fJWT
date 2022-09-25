package br.com.fjwt
package crypto
package base64
package decode

import cats.*
import cats.syntax.all.*
import java.util.Base64

trait Base64Decoder[F[*]]:
  def decode(str: String): F[String]

object Base64Decoder:
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: Base64Decoder[F] = new Base64Decoder[F]:
    def decode(str: String): F[String] =
      try {
        lazy val decodedBytes = Base64.getDecoder().decode(str)
        new String(decodedBytes).pure[F]
      } catch {
        case iae: IllegalArgumentException => iae.raiseError[F, String]
      }
