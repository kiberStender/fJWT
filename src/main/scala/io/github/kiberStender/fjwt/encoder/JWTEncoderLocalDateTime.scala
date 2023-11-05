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
import io.github.kiberStender.fjwt.model.Claim
import io.github.kiberStender.fjwt.model.Alg
import io.github.kiberStender.fjwt.model.Alg.*
import io.github.kiberStender.fjwt.crypto.base64.Base64Encoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms
import io.github.kiberStender.fjwt.error.JWTError.{EmptyPrivateKey, NullPrivateKey}

import java.time.{LocalDateTime, ZoneId}

object JWTEncoderLocalDateTime:
  /** Factory method to create an instance of [[JWTEncoder]][F] that uses [[LocalDateTime]] for time
    * values
    * @param base64Encoder
    *   An instance of [[Base64Encoder]][F] used to verify the signature of the token
    * @param encodeAlg
    *   An instance of [[HmacEncoder]][F] used to sign the token
    * @param zoneId
    *   The [[ZoneId]] used to encode the the claim temporal values
    * @tparam F
    *   An instance of [[MonadError]][F, [[Throwable]]]
    * @tparam P
    *   The type of the Payload to be encoded
    * @return
    *   Either the encoded string token or a [[Throwable]]
    */
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      base64Encoder: Base64Encoder[F],
      encodeAlg: HmacEncoderAlgorithms
  )(using zoneId: ZoneId): JWTEncoder[F, LocalDateTime, P] = new JWTEncoder[F, LocalDateTime, P]:

    def encode(privateKey: String)(
        iss: Option[String],
        sub: Option[String],
        aud: Option[String],
        exp: Option[LocalDateTime],
        nbf: Option[LocalDateTime],
        iat: Option[LocalDateTime],
        jti: Option[String]
    )(payload: P): F[String] =
      lazy val header: Alg = encodeAlg.toAlg
      for
        key <- privateKey.isEmptyValue(NullPrivateKey)(EmptyPrivateKey)
        encodedHeader <- base64Encoder.encodeURLSafe(header.asJson.noSpaces)
        claim = Claim(
          iss,
          sub,
          aud,
          exp.map(zoneId.toEpoch),
          nbf.map(zoneId.toEpoch),
          iat.map(zoneId.toEpoch),
          jti
        ).asJson
        encodedPayload <- base64Encoder.encodeURLSafe(
          (claim deepMerge payload.asJson).dropNullValues.noSpaces
        )
        body = s"$encodedHeader.$encodedPayload"
        byteSig <- encodeAlg.encode(key)(body)
        signature <- base64Encoder.encodeURLSafe(byteSig)
      yield s"$body.$signature"
