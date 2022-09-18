package br.com.fjwt
package decoder

import br.com.fjwt.crypto.base64.decode.Base64Decoder
import br.com.fjwt.crypto.hs512.HS512Encoder
import br.com.fjwt.claim.Claim

import io.circe.*, io.circe.parser.*
import cats.*, cats.syntax.all.*
import java.time.{ZoneId, Instant, LocalDateTime}

trait JWTDecoder[F[*]]:
  def decode[P: Codec](privateKey: String)(token: String)(using
      zoneId: ZoneId
  ): F[P]

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

    private def decodeClaim[P: Codec](payload: String): F[Claim[P]] =
      parser
        .decode[Claim[P]](payload)
        .fold(
          _.raiseError[F, Claim[P]],
          _.pure[F]
        )

    private def isExpired[P: Codec](claim: Claim[P])(using
        zoneId: ZoneId
    ): F[P] =
      lazy val now = LocalDateTime.now()
      claim.exp
        .map(Instant.ofEpochMilli(_).atZone(zoneId).toLocalDateTime())
        .map(_ isAfter now)
        .map({
          case true  => claim.payload.pure[F]
          case false => new Throwable("Token is expired").raiseError[F, P]
        })
        .getOrElse(claim.payload.pure[F])

    def decode[P: Codec](
        privateKey: String
    )(token: String)(using zoneId: ZoneId): F[P] =
      for
        payloadStr <- isValid(privateKey, token split "\\.")
        decodedPayload <- decodeClaim(payloadStr)
        payload <- isExpired(decodedPayload)
      yield payload
