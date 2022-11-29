package io.github.kiberStender
package fjwt

import cats.MonadError
import cats.syntax.all.{toFlatMapOps, toFunctorOps}
import io.circe.Codec
import io.github.kiberStender.fjwt.utils.PayloadExtractor
import io.github.kiberStender.fjwt.validation.StringValidation
import io.github.kiberStender.fjwt.error.JWTError.{
  NullPrivateKey,
  EmptyPrivateKey,
  NullToken,
  EmptyToken
}

import java.time.ZoneId

/** A trait that describes the {@link JWTDecoder} typeclass
  *
  * @tparam F
  *   A given container that wraps the return type
  */
trait JWTDecoder[F[*]]:
  /** The method to decode a given payload object described by the type P
    * @param privateKey
    *   The private key previously used to encode the payload
    * @param accessToken
    *   The JWT token that will be decoded
    * @param ZoneId
    *   The {@link ZoneID} used in claim to make expired date more accurate
    * @tparam P
    *   The type of the payload
    * @return
    *   The payload object wrapped in F or an Error wrapped in F describing the problem
    */
  def decode[P: Codec](privateKey: String)(accessToken: String)(using ZoneId): F[P]

/** Instance factory for {@link JWTDecoder}
  */
object JWTDecoder:
  /** A method to instantiate a {@link JWTDecoder}
    * @param payloadExtractor
    *   An instance of {@link PayloadExtractor}
    * @tparam F
    *   An instance of {@link MonadError}
    * @return
    *   An instance of {@link JWTDecoder}
    */
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]](
      payloadExtractor: PayloadExtractor[F]
  ): JWTDecoder[F] = new JWTDecoder[F]:
    lazy val stringValidation: StringValidation[F] = StringValidation.dsl

    def decode[P: Codec](privateKey: String)(accessToken: String)(using ZoneId): F[P] =
      for
        key <- stringValidation.validate(privateKey)(NullPrivateKey)(EmptyPrivateKey)
        token <- stringValidation.validate(accessToken)(NullToken)(EmptyToken)
        payload <- payloadExtractor.extract(key)(token)
      yield payload
