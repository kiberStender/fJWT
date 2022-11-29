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

/** A trait that describes the JWTEncoder typeclass
  * @tparam F
  *   A given container that wraps the return type
  */
trait JWTEncoder[F[*]]:
  /** A method to encode a given payload data described by the type P into a encrypted {@link
    * String} value
    * @param privateKey
    *   The key that will be used to encode the payload
    * @param iss
    *   The issuer of the token[Optional]
    * @param sub
    *   The subject of the token[Optional]
    * @param aud
    *   The intended audience of the token[Optional]
    * @param exp
    *   The expiration time of the token[Optional]
    * @param nbf
    *   The not before datetime(The token cannot be used before the given date) of the
    *   token[Optional]
    * @param iat
    *   The Issued at(The time the token was issued) of the token[Optional]
    * @param jti
    *   The token ID[Optional]
    * @param payload
    *   The data you want to transmit itself
    * @param ZoneId
    *   The {@link ZoneID} that will be used to convert the {@link LocalDateTime} parameters to
    *   {@link Long} values
    * @tparam P
    *   The type of the payload
    * @return
    *   The encoded {@link String} or an error wrapped in F
    */
  def encode[P: Codec](privateKey: String)(
      iss: Option[String] = None,
      sub: Option[String] = None,
      aud: Option[String] = None,
      exp: Option[LocalDateTime] = None,
      nbf: Option[LocalDateTime] = None,
      iat: Option[LocalDateTime] = LocalDateTime.now().some,
      jti: Option[String] = None
  )(payload: P)(using ZoneId): F[String]

  /** A method to encode a given payload data described by the type P into a encrypted {@link String
    * value} where you don't have to provide the {@link Claim} manually In this method only issued
    * at(iat) is populated in {@link Claim}
    * @param privateKey
    *   The key that will be used to encode the payload
    * @param payload
    *   The data you want to transmit itself
    * @param ZoneId
    *   The {@link ZoneID} that will be used to convert the {@link LocalDateTime} parameters to
    *   {@link Long} values
    * @tparam P
    *   The type of the payload
    * @return
    *   The encoded {@link String} or an error wrapped in F
    */
  def encode[P: Codec](privateKey: String)(payload: P)(using ZoneId): F[String] =
    encode(privateKey)()(payload)

/** Instance factory for {@link JWTEncoder}
  */
object JWTEncoder:
  /** Method to create an instance of {@link JWTEncoder} by providing an instance of {@link
    * Base64Encoder} and {@link HmacEncoder}
    * @param base64Encoder
    *   An instance of {@link Base64Encoder} to encode the header and the payload of the token
    * @param hsEncoder
    *   An instance of {@link HmacEncoder} to encode both the header and the payload of the token in
    *   order to generate the signature of the token
    * @tparam F
    *   An instance of {@link MonadError[F, Throwable]}
    * @return
    *   An instance of {@link JWTEncoder}
    */
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
