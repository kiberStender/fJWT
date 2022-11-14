package io.github.kiberStender
package fjwt

import cats.MonadError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, catsSyntaxEq, toFlatMapOps, toFunctorOps}

import io.circe.{Codec, parser}

import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.crypto.hs.{HmacEncoder, HmacEncoderAlgorithms}
import io.github.kiberStender.fjwt.error.JWTError
import io.github.kiberStender.fjwt.error.JWTError.*
import io.github.kiberStender.fjwt.validation.StringValidation

import java.time.{Instant, LocalDateTime, ZoneId}

trait JWTDecoder[F[*]]:
  def decode[P: Codec](privateKey: String)(accessToken: String)(using ZoneId): F[P]

object JWTDecoder:
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]](
      base64Decoder: Base64Decoder[F]
  ): JWTDecoder[F] = new JWTDecoder[F]:
    lazy val stringValidation: StringValidation[F] = StringValidation.dsl

    private def isSignatureValid(encoded: String)(signature: String)(payload: String): F[String] =
      if encoded === signature then base64Decoder decode payload
      else InvalidSignature.raiseError[F, String]

    private def has3Parts: Array[String] => F[(String, String, String)] =
      case Array(header, payload, signature) => (header, payload, signature).pure[F]
      case _ => WrongTokenParts.raiseError[F, (String, String, String)]

    private def decodeHeader(header: String): F[HmacEncoder[F]] =
      parser
        .decode[Alg](header)
        .map(_.alg)
        .fold(_.raiseError[F, HmacEncoderAlgorithms], _.pure[F])
        .map(HmacEncoder.hsEncoder)

    private def extractPayload(privateKey: String)(parts: Array[String]): F[String] =
      for
        (header, payload, signature) <- has3Parts(parts)
        decodedHeader <- base64Decoder.decode(header)
        hmacDecoder <- decodeHeader(decodedHeader)
        encoded <- hmacDecoder.encode(privateKey)(s"$header.$payload")
        result <- isSignatureValid(encoded)(signature)(payload)
      yield result

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
        payloadStr <- extractPayload(key)(token split "\\.")
        decodedPayload <- decodeClaim(payloadStr)
        payload <- isExpired(decodedPayload)
      yield payload
