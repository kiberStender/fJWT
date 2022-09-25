package br.com.fjwt
package crypto
package base64

import cats.*, cats.syntax.all.*
import java.util.Base64

trait Base64Encoder[F[*]]:
  def encode(str: String): F[String]

object Base64Encoder:
  def dsl[F[*]: Applicative]: Base64Encoder[F] = new Base64Encoder:
    def encode(str: String): F[String] =
      Base64.getEncoder().encodeToString(str.getBytes()).pure[F]
