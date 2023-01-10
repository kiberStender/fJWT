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

object JWTEncoderLong:
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      base64Encoder: Base64Encoder[F],
      hsEncoder: HmacEncoder[F]
  )(using zoneId: ZoneId): JWTEncoder[F, Long, P] = new JWTEncoder[F, Long, P]:
    private lazy val stringValidation: StringValidation[F] = StringValidation.dsl

    def encode(privateKey: String)(
        iss: Option[String] = None,
        sub: Option[String] = None,
        aud: Option[String] = None,
        exp: Option[Long] = None,
        nbf: Option[Long] = None,
        iat: Option[Long] = None,
        jti: Option[String] = None
    )(payload: P): F[String] =
      lazy val header = Alg(hsEncoder.alg, "JWT")
      for
        key <- stringValidation.validate(privateKey)(NullPrivateKey)(EmptyPrivateKey)
        encodedHeader <- base64Encoder.encode(Alg.encoder(header).noSpaces)
        claim = Claim[P](
          iss,
          sub,
          aud,
          exp,
          nbf,
          iat.orElse(zoneId.toEpoch(LocalDateTime.now()).some),
          jti,
          payload
        ).asJson
        encodedPayload <- base64Encoder.encode(claim.noSpaces)
        body = s"$encodedHeader.$encodedPayload"
        jwt <- hsEncoder.encode(key)(body)
      yield s"$encodedHeader.$encodedPayload.$jwt"
