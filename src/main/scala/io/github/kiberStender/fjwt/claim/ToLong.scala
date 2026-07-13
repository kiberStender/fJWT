package io.github.kiberStender
package fjwt
package claim

import io.github.kiberStender.fjwt.models.Claim

/** A trait to convert Long to T(a time unit), making it easier to generate the
  * token without forcing the user to use a specific time type
  * @tparam F
  *   The effect type
  * @tparam T
  *   The generic type representing a time unit
  */
trait ToLong[F[*], T] {

  /** Method to convert Long to T
    * @param claim
    *   The claim before being parsed to the String token that will have its
    *   time fields(exp,nbf, iat) converted to Long
    * @return
    *   either a Claim[Long] or whatever error it might throw
    */
  def toLong(claim: Claim[T]): F[Claim[Long]]
}
