package br.com.fjwt
package decoder

import br.com.fjwt.crypto.base64.decode.Base64Decoder
import br.com.fjwt.crypto.hs512.HS512Encoder
import io.circe.*, io.circe.parser.*
import cats.*, cats.syntax.all.*

trait JWTDecoder[F[*]]:
  def decode[A](privateKey: String)(token: String)(using D: Decoder[A]): F[A]

object JWTDecoder:
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]](
      base64Decoder: Base64Decoder[F],
      hs512Encoder: HS512Encoder[F]
  ): JWTDecoder[F] = new JWTDecoder[F]:

    private def isSignatureValid(
        encoded: String
    )(signature: String)(payload: String): F[String] =
      if (encoded == signature) base64Decoder.decode(payload)
      else new Throwable("JWT Signature does not match").raiseError[F, String]

    private def isValid: (String, Array[String]) => F[String] = {
      case (privateKey, Array(header, payload, signature)) =>
        for
          encoded <- hs512Encoder.encode(privateKey)(s"$header.$payload")
          result <- isSignatureValid(encoded)(signature)(payload)
        yield result
      case _ =>
        new Throwable("Token has to has 3 parts: header.payload.signature")
          .raiseError[F, String]
    }

    private def decodePayload[A](payload: String)(using D: Decoder[A]): F[A] =
      parser
        .decode[A](payload)
        .fold(
          _.raiseError[F, A],
          _.pure[F]
        )

    def decode[A](
        privateKey: String
    )(token: String)(using D: Decoder[A]): F[A] =
      for
        payloadStr <- isValid(privateKey, token split "\\.")
        decodedPayload <- decodePayload(payloadStr)
      yield decodedPayload
