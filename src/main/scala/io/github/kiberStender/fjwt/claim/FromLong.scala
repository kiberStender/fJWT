package io.github.kiberStender
package fjwt
package claim

import io.github.kiberStender.fjwt.models.Claim

/** A trait to convert T(a time unit) to Long, making it easier to generate the
  * token without forcing the user to use a specific time type
  * @tparam F
  *   The effect type
  * @tparam T
  *   The generic type representing a time unit
  */
trait FromLong[F[*], T] {

  /** Method to convert T to Long
    * @param claim
    *   The raw claim after being parsed from the String token that will have
    *   its time fields(exp,nbf, iat) converted to T
    * @return
    *   either a Claim[T] or whatever error it might throw
    */
  def fromLong(claim: Claim[Long]): F[Claim[T]]
}
