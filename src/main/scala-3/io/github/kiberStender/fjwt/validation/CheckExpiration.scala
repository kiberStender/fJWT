package io.github.kiberStender
package fjwt
package validation

import cats.ApplicativeError
import io.github.kiberStender.fjwt.models.Claim

/** A trait to manage how to calculate if a given token is expired or not
  * @tparam C
  *   The type of the claim to be analyzed
  */
trait CheckExpiration[C <: Claim[Any]]:

  /** Method to check if a given claim is expired
    * @param claim
    *   The claim to be analyzed
    * @tparam F
    *   The effect type
    * @return
    *   True if the claim has an expiration date, and it's not yet reached or if it does not have an
    *   expiration date, false if it has no expiration date, and
    *   [[io.github.kiberStender.fjwt.exception.JWTError.ExpiredToken]] if it is either the actual
    *   time or if it is in the past and
    */
  def isExpired[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]](claim: C): F[Boolean]
