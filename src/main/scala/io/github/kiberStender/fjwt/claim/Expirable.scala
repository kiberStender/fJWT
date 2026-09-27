package io.github.kiberStender
package fjwt
package claim

import io.github.kiberStender.fjwt.models.Claim

/** A typeclass defining an effectful operation to evaluate the lifecycle
  * expiration of a JWT claim.
  *
  * Within the FJWT library, the `Expirable` typeclass is used during the
  * decoding pipeline to ensure that a token's temporal bounds (such as its
  * `exp` claim) are respected. If the claim represents an expired token,
  * implementations typically raise a suspended error (such as
  * [[JWTException.ExpiredTokenException]]) within the effect type `F`. If the
  * token is valid, it returns the claim suspended in `F`.
  *
  * @tparam F
  *   The effect type constructor (e.g., `cats.effect.IO`, `scala.util.Try`).
  * @tparam T
  *   The domain representation of time used within the claim (e.g., `Long`,
  *   `LocalDateTime`).
  * @tparam C
  *   The higher-kinded type representing the JWT Claim, bounded by [[Claim]].
  *
  * @example
  *   {{{
  * // Evaluating expiration using an implicitly available Expirable instance
  * val claim = SimpleClaim[LocalDateTime](exp = Some(LocalDateTime.now().minusHours(1)))
  *
  * val checkExpiration: Try[SimpleClaim[LocalDateTime]] =
  *   implicitly[Expirable[Try, LocalDateTime, SimpleClaim]].isExpired(claim)
  *   }}}
  */
trait Expirable[F[*], T, C[X] <: Claim[X]] {

  /** Evaluates whether the given claim has expired.
    *
    * @param claim
    *   The strongly-typed claim to be inspected.
    * @return
    *   The claim suspended in the effect `F` if valid, or a suspended
    *   expiration error if the token has expired.
    */
  def isExpired(claim: C[T]): F[C[T]]
}
