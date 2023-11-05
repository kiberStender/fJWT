package io.github.kiberStender
package fjwt
package encoder

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
import io.github.kiberStender.fjwt.error.JWTError.{EmptyPrivateKey, NullPrivateKey}

import java.time.{LocalDateTime, ZoneId}

/** A trait that describes the [[JWTEncoder]] typeclass
  *
  * @tparam F
  *   A given container that wraps the return type
  * @tparam Time
  *   The type of time fields: exp, nbf and iat
  * @tparam P
  *   The type of the Payload
  */
trait JWTEncoder[F[*], Time, P]:
  /** A method to encode a given payload data described by the type P into a encrypted [[String]]
    * value
    *
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
    * @return
    *   The encoded [[String]] or an error wrapped in F
    */
  def encode(privateKey: String)(
      iss: Option[String] = None,
      sub: Option[String] = None,
      aud: Option[String] = None,
      exp: Option[Time] = None,
      nbf: Option[Time] = None,
      iat: Option[Time] = None,
      jti: Option[String] = None
  )(payload: P): F[String]
