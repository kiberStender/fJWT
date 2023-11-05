package io.github.kiberStender
package fjwt
package decoder

import cats.MonadError
import cats.syntax.all.{
  catsSyntaxApplicativeId,
  catsSyntaxApplicativeErrorId,
  toFlatMapOps,
  toFunctorOps
}
import io.circe.{Codec, parser}
import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms
import io.github.kiberStender.fjwt.error.JWTError.{
  EmptyPrivateKey,
  EmptyToken,
  NotMappedError,
  NullPrivateKey,
  NullToken
}
import io.github.kiberStender.fjwt.model.{Alg, Claim}
import io.github.kiberStender.fjwt.model
import io.github.kiberStender.fjwt.validation.Payload.*

import java.time.ZoneId

/** A trait that describes the [[JWTDecoder]] typeclass
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
  def decode(privateKey: String)(accessToken: String): F[Payload[P]]

/** Instance factory for [[JWTDecoder]]
  */
object JWTDecoder:
  private def extractPayload[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      jwtPayload: String
  ): F[Payload[P]] =
    (for
      claim <- parser.decode[Claim](jwtPayload)
      p <- parser.decode[P](jwtPayload)
    yield (claim, p))
      .fold(
        throwable => NotMappedError(throwable.getMessage).raiseError[F, Payload[P]],
        _.pure[F]
      )

  private def extractAlg[F[*]: [F[*]] =>> MonadError[F, Throwable]](
      headerStr: String
  ): F[HmacEncoderAlgorithms] =
    parser
      .decode[Alg](headerStr)
      .map(_.toHmacAlgorithms)
      .fold(_.raiseError[F, HmacEncoderAlgorithms], _.pure[F])

  /** A method to instantiate a [[JWTDecoder]][F, P] that performs no validation on the token. It
    * only extracts the payload/claim and try to parse the json PS: Not recommended for production
    * code. Use with care
    * @param base64Decoder
    *   An instance of [[Base64Decoder]][F] used to decode the encrypted payload
    * @tparam F
    *   An instance of [[MonadError]][F, [[Throwable]]]
    * @tparam P
    *   The type of the Payload to be decoded
    * @return
    *   Either the decoded and parsed Payload P or an [[Throwable]]
    */
  def noValidation[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      base64Decoder: Base64Decoder[F]
  ): JWTDecoder[F, P] = new JWTDecoder[F, P]:
    def decode(privateKey: String)(accessToken: String): F[Payload[P]] =
      for
        (_, encodedPayloadStr) <- accessToken.is2Parts
        decodedPayloadStr <- base64Decoder decode encodedPayloadStr
        payload <- extractPayload(decodedPayloadStr)
      yield payload

  /** A method to instantiate a [[JWTDecoder]][F, P] that uses the header to find out what algorithm
    * was used to sign the token and performs no expiration validation
    * @param base64Encoder
    *   An instance of [[Base64Encoder]][F] used to verify the signature of the token
    * @param base64Decoder
    *   An instance of [[Base64Decoder]][F] used to decode the encrypted payload
    * @tparam F
    *   An instance of [[MonadError]][F, [[Throwable]]]
    * @tparam P
    *   The type of the Payload to be decoded
    * @return
    *   Either the decoded and parsed Payload P or an [[Throwable]]
    */
  def useHeaderNoExpirationValidation[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      base64Encoder: Base64Encoder[F],
      base64Decoder: Base64Decoder[F]
  ): JWTDecoder[F, P] = new JWTDecoder[F, P]:
    def decode(privateKey: String)(accessToken: String): F[Payload[P]] =
      for
        key <- privateKey.isEmptyValue(NullPrivateKey)(EmptyPrivateKey)
        token <- accessToken.isEmptyValue(NullToken)(EmptyToken)
        (encodedHeaderStr, encodedPayloadStr, origSignature) <- token.is3Parts
        decodedHeader <- base64Decoder decode encodedHeaderStr
        decodeAlg <- extractAlg(decodedHeader)
        bodyToValidate = s"$encodedHeaderStr.$encodedPayloadStr"
        byteSig <- decodeAlg.encode(key)(bodyToValidate)
        calculatedSig <- base64Encoder encodeURLSafe byteSig
        _ <- calculatedSig isValidSignature origSignature
        decodedPayloadStr <- base64Decoder decode encodedPayloadStr
        payload <- extractPayload(decodedPayloadStr)
      yield payload

  /** A method to instantiate a [[JWTDecoder]][F, P] that ignores the header and uses the provided
    * algorithm check the token signatures and performs no expiration validation
    *
    * PS: This method assumes you know what algorithm was used to sig the token to be decoded
    * @param base64Encoder
    *   An instance of [[Base64Encoder]][F] used to verify the signature of the token
    * @param base64Decoder
    *   An instance of [[Base64Decoder]][F] used to decode the encrypted payload
    * @param encodeAlg
    *   An instance of [[HmacEncoder]][F] used to verify the integrity of the token signature
    * @tparam F
    *   An instance of [[MonadError]][F, [[Throwable]]]
    * @tparam P
    *   The type of the Payload to be decoded
    * @return
    *   Either the decoded and parsed Payload P or an [[Throwable]]
    */
  def noExpirationValidation[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      base64Encoder: Base64Encoder[F],
      base64Decoder: Base64Decoder[F],
      encodeAlg: HmacEncoderAlgorithms
  ): JWTDecoder[F, P] = new JWTDecoder[F, P]:
    def decode(privateKey: String)(accessToken: String): F[Payload[P]] =
      for
        key <- privateKey.isEmptyValue(NullPrivateKey)(EmptyPrivateKey)
        token <- accessToken.isEmptyValue(NullToken)(EmptyToken)
        (encodedHeaderStr, encodedPayloadStr, origSignature) <- token.is3Parts
        decodedHeader <- base64Decoder decode encodedHeaderStr
        bodyToValidate = s"$encodedHeaderStr.$encodedPayloadStr"
        byteSig <- encodeAlg.encode(key)(bodyToValidate)
        calculatedSig <- base64Encoder encodeURLSafe byteSig
        _ <- calculatedSig isValidSignature origSignature
        decodedPayloadStr <- base64Decoder decode encodedPayloadStr
        payload <- extractPayload(decodedPayloadStr)
      yield payload

  /** A method to instantiate a [[JWTDecoder]][F, P] that uses the header to find out what algorithm
    * was used to sign the token and performs both signature validation and expiration validation
    * @param base64Encoder
    *   An instance of [[Base64Encoder]][F] used to verify the signature of the token
    * @param base64Decoder
    *   An instance of [[Base64Decoder]][F] used to decode the encrypted payload
    * @param ZoneId
    *   The [[ZoneId]] used to encode the the claim temporal values
    * @tparam F
    *   An instance of [[MonadError]][F, [[Throwable]]]
    * @tparam P
    *   The type of the Payload to be decoded
    * @return
    *   Either the decoded and parsed Payload P or an [[Throwable]]
    */
  def useHeaderAllValidations[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      base64Encoder: Base64Encoder[F],
      base64Decoder: Base64Decoder[F]
  )(using ZoneId): JWTDecoder[F, P] = new JWTDecoder[F, P]:
    def decode(privateKey: String)(accessToken: String): F[Payload[P]] =
      for
        key <- privateKey.isEmptyValue(NullPrivateKey)(EmptyPrivateKey)
        token <- accessToken.isEmptyValue(NullToken)(EmptyToken)
        (encodedHeaderStr, encodedPayloadStr, origSignature) <- token.is3Parts
        decodedHeader <- base64Decoder decode encodedHeaderStr
        decodeAlg <- extractAlg(decodedHeader)
        bodyToValidate = s"$encodedHeaderStr.$encodedPayloadStr"
        byteSig <- decodeAlg.encode(key)(bodyToValidate)
        calculatedSig <- base64Encoder encodeURLSafe byteSig
        _ <- calculatedSig isValidSignature origSignature
        decodedPayloadStr <- base64Decoder decode encodedPayloadStr
        payload <- extractPayload(decodedPayloadStr)
        notExpiredPayload <- payload.isExpired
      yield notExpiredPayload

  /** A method to instantiate a [[JWTDecoder]][F, P] that ignores the header and uses the provided
    * algorithm to check the token signature and performs both signature validation and expiration
    * validation
    *
    * PS: This method assumes you know what algorithm was used to sig the token to be decoded
    * @param base64Encoder
    *   An instance of [[Base64Encoder]][F] used to verify the signature of the token
    * @param base64Decoder
    *   An instance of [[Base64Decoder]][F] used to decode the encrypted payload
    * @param encodeAlg
    *   An instance of [[HmacEncoder]][F] used to verify the integrity of the token signature
    * @param ZoneId
    *   The [[ZoneId]] used to encode the the claim temporal values
    * @tparam F
    *   An instance of [[MonadError]][F, [[Throwable]]]
    * @tparam P
    *   The type of the Payload to be decoded
    * @return
    *   Either the decoded and parsed Payload P or an [[Throwable]]
    */
  def allValidations[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](
      base64Encoder: Base64Encoder[F],
      base64Decoder: Base64Decoder[F],
      encodeAlg: HmacEncoderAlgorithms
  )(using ZoneId): JWTDecoder[F, P] = new JWTDecoder[F, P]:
    def decode(privateKey: String)(accessToken: String): F[Payload[P]] =
      for
        key <- privateKey.isEmptyValue(NullPrivateKey)(EmptyPrivateKey)
        token <- accessToken.isEmptyValue(NullToken)(EmptyToken)
        (encodedHeaderStr, encodedPayloadStr, origSignature) <- token.is3Parts
        decodedHeader <- base64Decoder decode encodedHeaderStr
        bodyToValidate = s"$encodedHeaderStr.$encodedPayloadStr"
        byteSig <- encodeAlg.encode(key)(bodyToValidate)
        calculatedSig <- base64Encoder encodeURLSafe byteSig
        _ <- calculatedSig isValidSignature origSignature
        decodedPayloadStr <- base64Decoder decode encodedPayloadStr
        payload <- extractPayload(decodedPayloadStr)
        notExpiredPayload <- payload.isExpired
      yield notExpiredPayload
