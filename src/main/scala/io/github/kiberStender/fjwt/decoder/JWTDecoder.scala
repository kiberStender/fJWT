package io.github.kiberStender
package fjwt
package decoder

import io.github.kiberStender.fjwt.models.JWToken

/** A trait that describes the [[JWTDecoder]] typeclass
  * @tparam F
  *   A given container that wraps the return type
  * @tparam T
  *   It is the type of time measurement you want to use. It is generic to be
  *   flexible to either user any library you want(Jodatime, Java LocalDateTime
  *   library, etc) or your own implementation like a simple Long or whatever
  *   you need at the moment
  * @tparam P
  *   The type of the Payload to be decoded
  */
trait JWTDecoder[F[*], T, P] {

  /** The method to decode a given payload object described by the type P
    * @param privateKey
    *   The private key previously used to encode the payload
    * @param accessToken
    *   The JWT token that will be decoded
    * @return
    *   The payload object wrapped in F or an Error wrapped in F describing the
    *   problem
    */
  def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]]
}
