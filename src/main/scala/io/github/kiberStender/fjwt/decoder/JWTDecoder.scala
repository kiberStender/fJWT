package io.github.kiberStender
package fjwt
package decoder

import cats.MonadError
import cats.syntax.all.{toFlatMapOps, toFunctorOps}
import io.circe.Codec
import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoder
import io.github.kiberStender.fjwt.error.JWTError.{EmptyPrivateKey, EmptyToken, NullPrivateKey, NullToken}
import io.github.kiberStender.fjwt.utils.PayloadExtractor
import io.github.kiberStender.fjwt.validation.StringValidation

import java.time.ZoneId

/** A trait that describes the {@link JWTDecoder} typeclass
  * @tparam F
  *   A given container that wraps the return type
  * @tparam P
  *   The type of the Payload to be decoded
  */
trait JWTDecoder[F[*], P]:
  /** The method to decode a given payload object described by the type P
    * @param privateKey
    *   The private key previously used to encode the payload
    * @param accessToken
    *   The JWT token that will be decoded
    * @return
    *   The payload object wrapped in F or an Error wrapped in F describing the problem
    */
  def decode(privateKey: String)(accessToken: String): F[P]

/** Instance factory for {@link JWTDecoder}
  */
object JWTDecoder:
  private def decodeJWT[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      payloadExtractor: PayloadExtractor[F, P],
      stringValidation: StringValidation[F]
  )(privateKey: String)(accessToken: String): F[P] =
    for
      key <- stringValidation.validate(privateKey)(NullPrivateKey)(EmptyPrivateKey)
      token <- stringValidation.validate(accessToken)(NullToken)(EmptyToken)
      payload <- payloadExtractor.extract(key)(token)
    yield payload

  /** A method to instantiate a {@link JWTDecoder}[F, P] that loads the Hmac algorythmn based on the
    * token's header
    * @param base64Decoder
    *   An instance of {@link Base64Decoder}[F]
    * @param ZoneId
    *   The {@link ZoneId} used to encode the the claim temporal values
    * @tparam F
    *   An instance of {@link MonadError}[F, Throwable]
    * @tparam P
    *   The type of the Payload to be decoded
    * @return
    *   Either the decoded Payload P or an Exception
    */
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      base64Decoder: Base64Decoder[F]
  )(using ZoneId): JWTDecoder[F, P] = new JWTDecoder[F, P]:
    lazy val payloadExtractor: PayloadExtractor[F, P] = PayloadExtractor.dsl(base64Decoder)
    lazy val stringValidation: StringValidation[F] = StringValidation.dsl

    def decode(privateKey: String)(accessToken: String): F[P] =
      decodeJWT(payloadExtractor, stringValidation)(privateKey)(accessToken)

  /** A method to instantiate a {@link JWTDecoder} that ignores the token's header
    * @param base64Decoder
    *   An instance of {@link Base64Decoder}[F]
    * @param hmacEncoder
    *   An instance of {@link HmacEncoder}[F]
    * @param ZoneId
    *   The {@link ZoneId} used to encode the the claim temporal values
    * @tparam F
    *   An instance of {@link MonadError}[F, Throwable]
    * @tparam P
    *   The type of the Payload to be decoded
    * @return
    *   Either the decoded Payload P or an Exception
    */
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      base64Decoder: Base64Decoder[F],
      hmacEncoder: HmacEncoder[F]
  )(using ZoneId): JWTDecoder[F, P] = new JWTDecoder[F, P]:
    lazy val payloadExtractor: PayloadExtractor[F, P] =
      PayloadExtractor.dsl(base64Decoder, hmacEncoder)
    lazy val stringValidation: StringValidation[F] = StringValidation.dsl

    def decode(privateKey: String)(accessToken: String): F[P] =
      decodeJWT(payloadExtractor, stringValidation)(privateKey)(accessToken)
