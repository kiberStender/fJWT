package br.com.fjwt

import br.com.fjwt.crypto.base64.Base64Decoder
import br.com.fjwt.crypto.hs.HmacEncoder
import br.com.fjwt.error.JWTError
import br.com.fjwt.error.JWTError.*
import br.com.fjwt.validation.StringValidation

import cats.MonadError
import cats.syntax.all.{
  catsSyntaxApplicativeErrorId,
  catsSyntaxApplicativeId,
  toFlatMapOps,
  toFunctorOps
}

import io.circe.*
import io.circe.parser.*

import java.time.{Instant, LocalDateTime, ZoneId}

trait JWTDecoder[F[*]]:
  def decode[P: Codec](privateKey: String)(accessToken: String)(using ZoneId): F[P]

object JWTDecoder:
  def dsl[F[*]: [F[*]] =>> MonadError[F, JWTError]](
      base64Decoder: Base64Decoder[F],
      hsEncoder: HmacEncoder[F]
  ): JWTDecoder[F] = new JWTDecoder[F]:
    lazy val stringValidation: StringValidation[F] = StringValidation.dsl

    private def isSignatureValid(encoded: String)(signature: String)(payload: String): F[String] =
      if encoded == signature then base64Decoder decode payload
      else InvalidSignature.raiseError[F, String]

    private def isValid: (String, Array[String]) => F[String] =
      case (privateKey, Array(header, payload, signature)) =>
        for
          encoded <- hsEncoder.encode(privateKey)(s"$header.$payload")
          result <- isSignatureValid(encoded)(signature)(payload)
        yield result
      case _ => WrongTokenParts.raiseError[F, String]

    private def decodeClaim[P: Codec](payload: String): F[Claim[P]] =
      parser
        .decode[Claim[P]](payload)
        .fold(throwable => NotMappedError(throwable.getMessage).raiseError[F, Claim[P]], _.pure[F])

    private def isExpired[P: Codec](claim: Claim[P])(using zoneId: ZoneId): F[P] =
      claim.exp
        .map { exp =>
          val localDateExp = Instant.ofEpochMilli(exp).atZone(zoneId).toLocalDateTime
          if localDateExp isAfter LocalDateTime.now() then claim.payload.pure[F]
          else ExpiredToken.raiseError[F, P]
        }
        .getOrElse(claim.payload.pure[F])

    def decode[P: Codec](privateKey: String)(accessToken: String)(using ZoneId): F[P] =
      for
        key <- stringValidation.validate(privateKey)(NullPrivateKey)(EmptyPrivateKey)
        token <- stringValidation.validate(accessToken)(NullToken)(EmptyToken)
        payloadStr <- isValid(key, token split "\\.")
        decodedPayload <- decodeClaim(payloadStr)
        payload <- isExpired(decodedPayload)
      yield payload
