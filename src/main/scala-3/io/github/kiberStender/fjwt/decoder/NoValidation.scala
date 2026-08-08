package io.github.kiberStender
package fjwt
package decoder

import cats.MonadError
import cats.syntax.all.{toFlatMapOps, toFunctorOps}
import io.github.kiberStender.fjwt.claim.ClaimTasks.*
import io.github.kiberStender.fjwt.claim.FromLong
import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.header.AlgTasks.*
import io.github.kiberStender.fjwt.json.JsonDecoder
import io.github.kiberStender.fjwt.models.JWToken

object NoValidation:
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
  def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]: Base64Decoder: [F[*]] =>> FromLong[F, T]: [F[
      *
  ]] =>> JsonDecoder[F, P], T, P]: JWTDecoder[F, T, P] = new JWTDecoder[F, T, P]:
    def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]] =
      for
        (headerStr, encodedPayloadStr) <- accessToken.is2Parts[F]
        decodedHeaderStr <- implicitly[Base64Decoder[F]] decode headerStr
        decodedPayloadStr <- implicitly[Base64Decoder[F]] decode encodedPayloadStr
        header <- decodedHeaderStr.extractAlg[F]
        claim <- decodedPayloadStr.extractClaim[F, T]
        payload <- implicitly[JsonDecoder[F, P]].decode(decodedPayloadStr)
      yield JWToken(header, claim, payload)
