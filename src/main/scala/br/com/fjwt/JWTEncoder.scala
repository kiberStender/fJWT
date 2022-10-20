package br.com.fjwt

import br.com.fjwt.crypto.base64.Base64Encoder
import br.com.fjwt.crypto.hs.HmacEncoder
import br.com.fjwt.error.JWTError
import br.com.fjwt.validation.CodecValidation

import cats.MonadError
import cats.syntax.all.catsSyntaxApplicativeErrorId
import cats.syntax.all.catsSyntaxOptionId
import cats.syntax.all.toFunctorOps
import cats.syntax.all.toFlatMapOps

import io.circe.*
import io.circe.syntax.*

import java.time.{LocalDateTime, ZoneId}

trait JWTEncoder[F[*]]:
  def encode[P: Codec](privateKey: String)(
      iss: Option[String] = None,
      sub: Option[String] = None,
      aud: Option[String] = None,
      exp: Option[LocalDateTime] = None,
      nbf: Option[LocalDateTime] = None,
      iat: Option[LocalDateTime] = LocalDateTime.now().some,
      jti: Option[String] = None
  )(payload: P)(using ZoneId): F[String]

  def encode[P: Codec](privateKey: String)(payload: P)(using ZoneId): F[String]

object JWTEncoder:
  def dsl[F[*]: [F[*]] =>> MonadError[F, JWTError]](
      base64Encoder: Base64Encoder[F],
      hsEncoder: HmacEncoder[F]
  ): JWTEncoder[F] =
    new JWTEncoder[F]:
      def toEpochMilli(ldt: LocalDateTime)(using zoneId: ZoneId): Long =
        ldt.atZone(zoneId).toInstant.toEpochMilli

      def encode[P: Codec](privateKey: String)(
          iss: Option[String] = None,
          sub: Option[String] = None,
          aud: Option[String] = None,
          exp: Option[LocalDateTime] = None,
          nbf: Option[LocalDateTime] = None,
          iat: Option[LocalDateTime] = LocalDateTime.now().some,
          jti: Option[String] = None
      )(payload: P)(using ZoneId): F[String] =
        lazy val header = Alg(hsEncoder.alg, "JWT")
        for
          encodedHeader <- base64Encoder.encode(JWTHeader.encoder(header).noSpaces)
          claim = Claim[P](
            iss,
            sub,
            aud,
            exp.map(toEpochMilli),
            nbf.map(toEpochMilli),
            iat.map(toEpochMilli),
            jti,
            payload
          ).asJson
          encodedPayload <- base64Encoder.encode(claim.noSpaces)
          body = s"$encodedHeader.$encodedPayload"
          jwt <- hsEncoder.encode(privateKey)(body)
        yield s"$encodedHeader.$encodedPayload.$jwt"

      def encode[P: Codec](privateKey: String)(payload: P)(using ZoneId): F[String] =
        encode(privateKey)()(payload)