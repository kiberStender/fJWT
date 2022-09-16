package br.com.fjwt
package encoder

import br.com.fjwt.crypto.base64.encode.Base64Encoder
import br.com.fjwt.crypto.hs512.HS512Encoder

import cats.*
import cats.syntax.all.*

import io.circe.*

trait JWTEncoder[F[*]]:
  def encode[P](privateKey: String)(payload: P)(using EA: Encoder[P]): F[String]

object JWTEncoder:
  def dsl[F[*]: Monad](
      base64Encoder: Base64Encoder[F],
      hs512Encoder: HS512Encoder[F]
  ): JWTEncoder[F] =
    new JWTEncoder[F]:
      def encode[P](privateKey: String)(payload: P)(using
          EA: Encoder[P]
      ): F[String] =
        lazy val header = Alg(JWTAlgorythm.HS512, "JWT")
        for
          encodedHeader <- base64Encoder.encode(
            JWTHeader.encoder(header).noSpaces
          )
          encodedPayload <- base64Encoder.encode(EA(payload).noSpaces)
          body = s"$encodedHeader.$encodedPayload"
          jwt <- hs512Encoder.encode(privateKey)(body)
        yield s"$encodedHeader.$encodedPayload.$jwt"
