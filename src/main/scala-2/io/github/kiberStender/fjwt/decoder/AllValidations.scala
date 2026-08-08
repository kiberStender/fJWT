package io.github.kiberStender
package fjwt
package decoder

import cats.MonadError
import cats.syntax.all.{toFlatMapOps, toFunctorOps}
import io.github.kiberStender.fjwt.claim.ClaimTasks.ClaimStringOps
import io.github.kiberStender.fjwt.claim.{Expirable, FromLong}
import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.crypto.hmac.Hmac
import io.github.kiberStender.fjwt.exception.JWTException.{
  EmptyPrivateKeyException,
  EmptyTokenException,
  NullPrivateKeyException,
  NullTokenException
}
import io.github.kiberStender.fjwt.json.JsonDecoder
import io.github.kiberStender.fjwt.models.JWToken
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm

object AllValidations {

  /** A method to instantiate a [[JWTDecoder]][F, P] that ignores the header and uses the provided
    * algorithm to check the token signature and performs both signature validation and expiration
    * validation PS: This method assumes you know what algorithm was used to sig the token to be
    * decoded
    *
    * @param encodeAlg
    *   An instance of [[HmacAlgorithm]] used to verify the integrity of the token signature
    * @tparam F
    *   An instance of [[MonadError]][F, [[Throwable]]
    * @tparam T
    *   It is the type of time measurement you want to use. It is generic to be flexible to either
    *   user any library you want(Joda Time, Java LocalDateTime library, etc) or your own
    *   implementation like a simple Long or whatever you need at the moment
    * @tparam P
    *   The type of the Payload to be decoded
    * @return
    *   Either the decoded and parsed Payload P or an [[Throwable]]
    */
  def dsl[F[*]: MonadError[*[_], Throwable]: Base64Encoder: Base64Decoder: Hmac: FromLong[
    *[*],
    T
  ]: Expirable[*[*], T]: JsonDecoder[*[*], P], T, P](
      encodeAlg: HmacAlgorithm
  ): JWTDecoder[F, T, P] =
    new JWTDecoder[F, T, P] {
      def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]] =
        for {
          key <- privateKey.isEmptyValue(NullPrivateKeyException)(EmptyPrivateKeyException)
          token <- accessToken.isEmptyValue(NullTokenException)(EmptyTokenException)
          (encodedHeaderStr, encodedPayloadStr, origSignature) <- token.is3Parts[F]
          bodyToValidate = s"$encodedHeaderStr.$encodedPayloadStr"
          byteSig <- encodeAlg.hash(key)(bodyToValidate)
          calculatedSig <- implicitly[Base64Encoder[F]] encodeURLSafe byteSig
          _ <- calculatedSig isValidSignature origSignature
          decodedPayloadStr <- implicitly[Base64Decoder[F]] decode encodedPayloadStr
          claim <- decodedPayloadStr.extractClaim[F, T]
          _ <- implicitly[Expirable[F, T]].isExpired(claim)
          payload <- implicitly[JsonDecoder[F, P]] decode decodedPayloadStr
        } yield JWToken(encodeAlg, claim, payload)
    }
}
