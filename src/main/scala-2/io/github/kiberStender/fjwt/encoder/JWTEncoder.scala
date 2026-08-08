package io.github.kiberStender
package fjwt
package encoder

import cats.MonadError
import cats.syntax.all.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import io.github.kiberStender.fjwt.claim.ClaimTasks.ClaimOps
import io.github.kiberStender.fjwt.claim.ToLong
import io.github.kiberStender.fjwt.crypto.base64.Base64Encoder
import io.github.kiberStender.fjwt.crypto.hmac.Hmac
import io.github.kiberStender.fjwt.exception.JWTException.{
  EmptyPrivateKeyException,
  NullPrivateKeyException
}
import io.github.kiberStender.fjwt.json.JsonEncoder
import io.github.kiberStender.fjwt.models.Claim
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm

/** A trait that describes the [[JWTEncoder]] typeclass
  * @tparam F
  *   A given container that wraps the return type
  * @tparam T
  *   The type of the time unit in the claim
  * @tparam P
  *   The type of the Payload
  */
trait JWTEncoder[F[*], T, P] {

  /** A method to encode a given payload data described by the type P into a encrypted [[String]]
    * value
    * @param privateKey
    *   The key that will be used to encode the payload
    * @param claim
    *   The claim holding the token metadata for validation
    * @param payload
    *   The data you want to transmit itself
    * @return
    *   The encoded [[String]] or an error wrapped in F
    */
  def encode(privateKey: String)(claim: Claim[T])(payload: P): F[String]
}

object JWTEncoder {
  def dsl[F[*]: MonadError[*[_], Throwable]: Base64Encoder: Hmac: ToLong[*[*], T]: JsonEncoder[*[
    *
  ], P], T, P](
      hmacAlg: HmacAlgorithm
  ): JWTEncoder[F, T, P] =
    new JWTEncoder[F, T, P] {
      def encode(privateKey: String)(claim: Claim[T])(payload: P): F[String] = for {
        key <- privateKey.isEmptyValue(NullPrivateKeyException)(EmptyPrivateKeyException)
        headerStr <- s"""{"alg":"${hmacAlg.alg}","typ":"JWT"}""".pure[F]
        encodedHeader <- implicitly[Base64Encoder[F]].encodeURLSafe(headerStr)
        claimStr <- claim.toJson[F]
        payloadStr <- implicitly[JsonEncoder[F, P]].encode(payload)

        encodedPayload <- implicitly[Base64Encoder[F]].encodeURLSafe(claimStr merge payloadStr)
        body = s"$encodedHeader.$encodedPayload"
        byteSig <- hmacAlg.hash(key)(body)
        signature <- implicitly[Base64Encoder[F]].encodeURLSafe(byteSig)
      } yield s"$body.$signature"
    }
}
