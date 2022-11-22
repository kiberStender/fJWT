package io.github.kiberStender
package fjwt

import io.github.kiberStender.fjwt.error.JWTError.{EmptyPrivateKey, NullPrivateKey}
import cats.MonadError
import cats.syntax.all.{
  catsSyntaxApplicativeErrorId,
  catsSyntaxOptionId,
  toFlatMapOps,
  toFunctorOps
}
import io.circe.Codec
import io.circe.syntax.EncoderOps
import io.github.kiberStender.fjwt.crypto.base64.Base64Encoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoder
import io.github.kiberStender.fjwt.error.JWTError
import io.github.kiberStender.fjwt.validation.StringValidation

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
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]](
      base64Encoder: Base64Encoder[F],
      hsEncoder: HmacEncoder[F]
  ): JWTEncoder[F] = new JWTEncoder[F]:
    lazy val stringValidation: StringValidation[F] = StringValidation.dsl

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
        key <- stringValidation.validate(privateKey)(NullPrivateKey)(EmptyPrivateKey)
        encodedHeader <- base64Encoder.encode(Alg.encoder(header).noSpaces)
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
        jwt <- hsEncoder.encode(key)(body)
      yield s"$encodedHeader.$encodedPayload.$jwt"

    def encode[P: Codec](privateKey: String)(payload: P)(using ZoneId): F[String] =
      encode(privateKey)()(payload)
