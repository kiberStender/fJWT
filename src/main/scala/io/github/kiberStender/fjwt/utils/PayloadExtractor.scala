package io.github.kiberStender.fjwt.utils

import cats.MonadError
import cats.syntax.all.{
  catsSyntaxApplicativeErrorId,
  catsSyntaxApplicativeId,
  toFlatMapOps,
  toFunctorOps
}
import io.circe.Codec
import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoder
import io.github.kiberStender.fjwt.validation.{
  PayloadExpirationValidation,
  SignatureValidation,
  TokenPartsValidation
}

import java.time.ZoneId

/** A typeclass to extract the P object out of the payload inside a {@link Claim}[P] {@link String}
  * serialized object
  * @tparam F
  *   The container to wrap the return methods
  */
trait PayloadExtractor[F[*]]:
  /** The method to extract an object of type P out of the payload inside a {@link Claim}[P] {@link
    * String} serialized object
    * @param privateKey
    *   The key used to encode the original object into the serialized {@link String}
    * @param token
    *   The token resulted from the encoding of the original object
    * @param ZoneId
    *   The {@link ZoneId} used to encode the the claim temporal values
    * @tparam P
    *   The type of the object to be extracted
    * @return
    *   Either the extracted object from {@link Claim}[P] or the error wrapped in F
    */
  def extract[P: Codec](privateKey: String)(token: String)(using ZoneId): F[P]

/** Instance factory for {@link PayloadExtractor}
  */
object PayloadExtractor:
  /** The method to instantiate {@link PayloadExtractor}[F] using the header algorithm field to tell
    * which Hmac algorithm was used to encode the signature
    * @param base64Decoder
    *   An instance of {@link Base64Decoder}[F]
    * @tparam F
    *   An instance of {@link MonadError}[F, Throwable]
    * @return
    *   An instance of {@link PayloadExtractor}[F]
    */
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]](
      base64Decoder: Base64Decoder[F]
  ): PayloadExtractor[F] = new PayloadExtractor[F]:
    lazy val tokenPartsValidation: TokenPartsValidation[F] = TokenPartsValidation.dsl
    lazy val headerDecoder: HeaderDecoder[F] = HeaderDecoder.dsl
    lazy val signatureValidation: SignatureValidation[F] = SignatureValidation.dsl
    lazy val claimDecoder: ClaimDecoder[F] = ClaimDecoder.dsl
    lazy val payloadExpirationValidation: PayloadExpirationValidation[F] =
      PayloadExpirationValidation.dsl

    override def extract[P: Codec](privateKey: String)(token: String)(using ZoneId: ZoneId): F[P] =
      for
        (header, payload, signature) <- tokenPartsValidation.validate(token)
        decodedHeader <- base64Decoder.decode(header)
        hmacEncoder <- headerDecoder.decode(decodedHeader)
        encoded <- hmacEncoder.encode(privateKey)(s"$header.$payload")
        _ <- signatureValidation.validate(encoded)(signature)
        str <- base64Decoder decode payload
        decodedPayload <- claimDecoder.decode(str)
        result <- payloadExpirationValidation.validate(decodedPayload)
      yield result

  /** The method to instantiate {@link PayloadExtractor}[F] where you provide the Hmac algorithm to
    * check against the signature PS: This is recommended if the JWT alg field in the header does
    * not correspond to any of the {@link
    * io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms} values
    * @param base64Decoder
    *   An instance of {@link Base64Decoder}[F]
    * @param hmacEncoder
    *   An instance of {@link HmacEncoder}[F]
    * @tparam F
    *   An instance of {@link MonadError}[F, Throwable]
    * @return
    *   An instance of {@link PayloadExtractor}[F]
    */
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]](
      base64Decoder: Base64Decoder[F],
      hmacEncoder: HmacEncoder[F]
  ): PayloadExtractor[F] = new PayloadExtractor[F]:
    lazy val tokenPartsValidation: TokenPartsValidation[F] = TokenPartsValidation.dsl
    lazy val signatureValidation: SignatureValidation[F] = SignatureValidation.dsl
    lazy val claimDecoder: ClaimDecoder[F] = ClaimDecoder.dsl
    lazy val payloadExpirationValidation: PayloadExpirationValidation[F] =
      PayloadExpirationValidation.dsl

    override def extract[P: Codec](privateKey: String)(token: String)(using ZoneId: ZoneId): F[P] =
      for
        (header, payload, signature) <- tokenPartsValidation.validate(token)
        encoded <- hmacEncoder.encode(privateKey)(s"$header.$payload")
        _ <- signatureValidation.validate(encoded)(signature)
        str <- base64Decoder decode payload
        decodedPayload <- claimDecoder.decode(str)
        result <- payloadExpirationValidation.validate(decodedPayload)
      yield result
