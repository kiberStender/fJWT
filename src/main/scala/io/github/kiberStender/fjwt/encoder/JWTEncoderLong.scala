package io.github.kiberStender
package fjwt
package encoder

import cats.MonadError
import cats.syntax.all.{
  catsSyntaxApplicativeErrorId,
  catsSyntaxApplicativeId,
  catsSyntaxOptionId,
  toFlatMapOps,
  toFunctorOps
}
import io.circe.Codec
import io.circe.syntax.EncoderOps
import io.github.kiberStender.fjwt.model.Alg.*
import io.github.kiberStender.fjwt.crypto.base64.Base64Encoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms
import io.github.kiberStender.fjwt.error.JWTError.{EmptyPrivateKey, NullPrivateKey}
import io.github.kiberStender.fjwt.model.{Alg, Claim}

import java.time.{LocalDateTime, ZoneId}

object JWTEncoderLong:
  /** Factory method to create an instance of [[JWTEncoder]][F] that uses [[Long]] for time values
    *
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
  )(using zoneId: ZoneId): JWTEncoder[F, Long, P] = new JWTEncoder[F, Long, P]:

    def encode(privateKey: String)(
        iss: Option[String] = None,
        sub: Option[String] = None,
        aud: Option[String] = None,
        exp: Option[Long] = None,
        nbf: Option[Long] = None,
        iat: Option[Long] = None,
        jti: Option[String] = None
    )(payload: P): F[String] =
      for
        key <- privateKey.isEmptyValue(NullPrivateKey)(EmptyPrivateKey)
        header = encodeAlg.toAlg
        encodedHeader <- base64Encoder.encodeURLSafe(header.asJson.noSpaces)
        claim = Claim(
          iss,
          sub,
          aud,
          exp,
          nbf,
          iat.orElse(zoneId.toEpoch(LocalDateTime.now()).some),
          jti
        ).asJson
        cleanClaim = (claim deepMerge payload.asJson).dropNullValues.noSpaces
        encodedPayload <- base64Encoder encodeURLSafe cleanClaim
        body = s"$encodedHeader.$encodedPayload"
        signature <- encodeAlg.encode(key)(body)
        base64Signature <- base64Encoder.encodeURLSafe(signature)
      yield s"$body.$base64Signature"
