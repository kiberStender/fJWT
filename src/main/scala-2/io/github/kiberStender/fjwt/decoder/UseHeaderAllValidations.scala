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
import io.github.kiberStender.fjwt.header.AlgTasks.AlgStringOps
import io.github.kiberStender.fjwt.json.JsonDecoder
import io.github.kiberStender.fjwt.models.JWToken

object UseHeaderAllValidations {

  /** A method to instantiate a [[JWTDecoder]][F, P] that uses the header to find out what algorithm
    * was used to sign the token and performs both signature validation and expiration validation
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
  def dsl[F[*]: MonadError[*[_], Throwable]: Base64Encoder: Base64Decoder: Hmac: Expirable[
    *[*],
    T
  ]: FromLong[*[*], T]: JsonDecoder[*[*], P], T, P]: JWTDecoder[F, T, P] =
    new JWTDecoder[F, T, P] {
      def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]] = for {
        key <- privateKey.isEmptyValue(NullPrivateKeyException)(EmptyPrivateKeyException)
        token <- accessToken.isEmptyValue(NullTokenException)(EmptyTokenException)
        (encodedHeaderStr, encodedPayloadStr, origSignature) <- token.is3Parts
        decodedHeaderStr <- implicitly[Base64Decoder[F]] decode encodedHeaderStr
        header <- decodedHeaderStr.extractAlg[F]
        bodyToValidate = s"$encodedHeaderStr.$encodedPayloadStr"
        byteSig <- header.hash(key)(bodyToValidate)
        calculatedSig <- implicitly[Base64Encoder[F]] encodeURLSafe byteSig
        _ <- calculatedSig isValidSignature origSignature
        decodedPayloadStr <- implicitly[Base64Decoder[F]] decode encodedPayloadStr
        claim <- decodedPayloadStr.extractClaim[F, T]
        _ <- implicitly[Expirable[F, T]].isExpired(claim)
        payload <- implicitly[JsonDecoder[F, P]] decode decodedPayloadStr
      } yield JWToken(header, claim, payload)
    }
}
