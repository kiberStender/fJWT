package io.github.kiberStender
package fjwt
package decoder

import cats.MonadError
import cats.syntax.all.{toFlatMapOps, toFunctorOps}
import io.github.kiberStender.fjwt.claim.ClaimTasks.*
import io.github.kiberStender.fjwt.claim.{Expirable, FromLong}
import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.crypto.hmac.Hmac
import io.github.kiberStender.fjwt.exception.JWTError.{
  EmptyPrivateKeyError,
  EmptyTokenError,
  NullPrivateKeyError,
  NullTokenError
}
import io.github.kiberStender.fjwt.header.AlgTasks.*
import io.github.kiberStender.fjwt.json.JsonDecoder
import io.github.kiberStender.fjwt.models.JWToken
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm

/** A trait that describes the [[JWTDecoder]] typeclass
  *
  * @tparam F
  *   A given container that wraps the return type
  * @tparam C
  *   The type of claim
  * @tparam P
  *   The type of the Payload to be decoded
  */
trait JWTDecoder[F[*], T, P]:

  /** The method to decode a given payload object described by the type P
    * @param privateKey
    *   The private key previously used to encode the payload
    * @param accessToken
    *   The JWT token that will be decoded
    * @return
    *   The payload object wrapped in F or an Error wrapped in F describing the problem
    */
  def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]]

object JWTDecoder:

  /** A method to instantiate a [[JWTDecoder]][F, P] that performs no validation on the token. It
    * only extracts the payload/claim and try to parse the json PS: Not recommended for production
    * code. Use with care
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
  def noValidation[F[*]: [F[*]] =>> MonadError[
    F,
    Throwable
  ]: Base64Decoder: [F[*]] =>> FromLong[F, T]: [F[*]] =>> JsonDecoder[F, P], T, P]
      : JWTDecoder[F, T, P] =
    new JWTDecoder[F, T, P]:
      def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]] =
        for
          (headerStr, encodedPayloadStr) <- accessToken.is2Parts[F]
          decodedHeaderStr <- implicitly[Base64Decoder[F]] decode headerStr
          decodedPayloadStr <- implicitly[Base64Decoder[F]] decode encodedPayloadStr
          header <- decodedHeaderStr.extractAlg[F]
          claim <- decodedPayloadStr.extractClaim[F, T]
          payload <- implicitly[JsonDecoder[F, P]].decode(decodedPayloadStr)
        yield JWToken(header, claim, payload)

  /** A method to instantiate a [[JWTDecoder]][F, P] that uses the header to find out what algorithm
    * was used to sign the token and performs no expiration validation
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
  def useHeaderNoExpirationValidation[F[*]: [F[*]] =>> MonadError[
    F,
    Throwable
  ]: Base64Encoder: Base64Decoder: Hmac: [F[*]] =>> FromLong[F, T]: [F[*]] =>> JsonDecoder[
    F,
    P
  ], T, P]: JWTDecoder[F, T, P] =
    new JWTDecoder[F, T, P]:
      def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]] =
        for
          key <- privateKey.isEmptyValue(NullPrivateKeyError)(EmptyPrivateKeyError)
          token <- accessToken.isEmptyValue(NullTokenError)(EmptyTokenError)
          (encodedHeaderStr, encodedPayloadStr, origSignature) <- token.is3Parts
          decodedHeaderStr <- implicitly[Base64Decoder[F]] decode encodedHeaderStr
          header <- decodedHeaderStr.extractAlg[F]
          bodyToValidate = s"$encodedHeaderStr.$encodedPayloadStr"
          byteSig <- header.hash(key)(bodyToValidate)
          calculatedSig <- implicitly[Base64Encoder[F]] encodeURLSafe byteSig
          _ <- calculatedSig isValidSignature origSignature
          decodedPayloadStr <- implicitly[Base64Decoder[F]] decode encodedPayloadStr
          claim <- decodedPayloadStr.extractClaim[F, T]
          payload <- implicitly[JsonDecoder[F, P]] decode decodedPayloadStr
        yield JWToken(header, claim, payload)

  /** A method to instantiate a [[JWTDecoder]][F, P] that ignores the header and uses the provided
    * algorithm check the token signatures and performs no expiration validation PS: This method
    * assumes you know what algorithm was used to sign the token to be decoded
    *
    * @param encodeAlg
    *   An instance of [[HmacAlgorithm]][F] used to verify the integrity of the token signature
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
  def noExpirationValidation[F[*]: [F[*]] =>> MonadError[
    F,
    Throwable
  ]: Base64Encoder: Base64Decoder: Hmac: [F[*]] =>> FromLong[F, T]: [F[*]] =>> JsonDecoder[
    F,
    P
  ], T, P](
      encodeAlg: HmacAlgorithm
  ): JWTDecoder[F, T, P] =
    new JWTDecoder[F, T, P]:
      def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]] =
        for
          key <- privateKey.isEmptyValue(NullPrivateKeyError)(EmptyPrivateKeyError)
          token <- accessToken.isEmptyValue(NullTokenError)(EmptyTokenError)
          (encodedHeaderStr, encodedPayloadStr, origSignature) <- token.is3Parts
          decodedHeader <- implicitly[Base64Decoder[F]] decode encodedHeaderStr
          bodyToValidate = s"$encodedHeaderStr.$encodedPayloadStr"
          byteSig <- encodeAlg.hash(key)(bodyToValidate)
          calculatedSig <- implicitly[Base64Encoder[F]] encodeURLSafe byteSig
          _ <- calculatedSig isValidSignature origSignature
          decodedPayloadStr <- implicitly[Base64Decoder[F]] decode encodedPayloadStr
          header <- decodedHeader.extractAlg[F]
          claim <- decodedPayloadStr.extractClaim[F, T]
          payload <- implicitly[JsonDecoder[F, P]] decode decodedPayloadStr
        yield JWToken(header, claim, payload)

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
  def useHeaderAllValidations[F[*]: [F[*]] =>> MonadError[
    F,
    Throwable
  ]: Base64Encoder: Base64Decoder: Hmac: [F[*]] =>> FromLong[F, T]: [F[*]] =>> Expirable[F, T]: [F[
      *
  ]] =>> JsonDecoder[F, P], T, P]: JWTDecoder[F, T, P] =
    new JWTDecoder[F, T, P]:
      def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]] = for
        key <- privateKey.isEmptyValue(NullPrivateKeyError)(EmptyPrivateKeyError)
        token <- accessToken.isEmptyValue(NullTokenError)(EmptyTokenError)
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
      yield JWToken(header, claim, payload)

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
  def allValidations[F[*]: [F[*]] =>> MonadError[
    F,
    Throwable
  ]: Base64Encoder: Base64Decoder: Hmac: [F[*]] =>> FromLong[F, T]: [F[*]] =>> Expirable[F, T]: [F[
      *
  ]] =>> JsonDecoder[F, P], T, P](
      encodeAlg: HmacAlgorithm
  ): JWTDecoder[F, T, P] =
    new JWTDecoder[F, T, P]:
      def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]] =
        for
          key <- privateKey.isEmptyValue(NullPrivateKeyError)(EmptyPrivateKeyError)
          token <- accessToken.isEmptyValue(NullTokenError)(EmptyTokenError)
          (encodedHeaderStr, encodedPayloadStr, origSignature) <- token.is3Parts
          bodyToValidate = s"$encodedHeaderStr.$encodedPayloadStr"
          byteSig <- encodeAlg.hash(key)(bodyToValidate)
          calculatedSig <- implicitly[Base64Encoder[F]] encodeURLSafe byteSig
          _ <- calculatedSig isValidSignature origSignature
          decodedPayloadStr <- implicitly[Base64Decoder[F]] decode encodedPayloadStr
          claim <- decodedPayloadStr.extractClaim[F, T]
          _ <- implicitly[Expirable[F, T]].isExpired(claim)
          payload <- implicitly[JsonDecoder[F, P]] decode decodedPayloadStr
        yield JWToken(encodeAlg, claim, payload)
