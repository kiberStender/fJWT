package io.github.kiberStender
package fjwt
package encoder

import cats.MonadError
import cats.syntax.all.{
  catsSyntaxApplicativeErrorId,
  catsSyntaxOptionId,
  toFlatMapOps,
  toFunctorOps
}
import io.circe.Codec
import io.circe.syntax.EncoderOps
import io.github.kiberStender.fjwt.{Alg, Claim}
import io.github.kiberStender.fjwt.crypto.base64.Base64Encoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoder
import io.github.kiberStender.fjwt.error.JWTError.{EmptyPrivateKey, NullPrivateKey}
import io.github.kiberStender.fjwt.validation.StringValidation
import io.github.kiberStender.fjwt.utils.toEpoch

import java.time.{LocalDateTime, ZoneId}

object JWTEncoderLocalDateTime:
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      base64Encoder: Base64Encoder[F],
      hsEncoder: HmacEncoder[F]
  )(using zoneId: ZoneId): JWTEncoder[F, LocalDateTime, P] = new JWTEncoder[F, LocalDateTime, P]:
    private lazy val stringValidation: StringValidation[F] = StringValidation.dsl

    def encode(privateKey: String)(
        iss: Option[String],
        sub: Option[String],
        aud: Option[String],
        exp: Option[LocalDateTime],
        nbf: Option[LocalDateTime],
        iat: Option[LocalDateTime],
        jti: Option[String]
    )(payload: P): F[String] =
      lazy val header = Alg(hsEncoder.alg, "JWT")
      for
        key <- stringValidation.validate(privateKey)(NullPrivateKey)(EmptyPrivateKey)
        encodedHeader <- base64Encoder.encode(Alg.encoder(header).noSpaces)
        claim = Claim[P](
          iss,
          sub,
          aud,
          exp.map(zoneId.toEpoch),
          nbf.map(zoneId.toEpoch),
          iat.map(zoneId.toEpoch),
          jti,
          payload
        ).asJson
        encodedPayload <- base64Encoder.encode(claim.noSpaces)
        body = s"$encodedHeader.$encodedPayload"
        jwt <- hsEncoder.encode(key)(body)
      yield s"$encodedHeader.$encodedPayload.$jwt"
