package io.github.kiberStender
package fjwt
package implicits
package claim

import cats.{Applicative, ApplicativeError}
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.claim.Expirable
import io.github.kiberStender.fjwt.convert.To
import io.github.kiberStender.fjwt.exception.JWTException.ExpiredTokenException
import io.github.kiberStender.fjwt.models.Claim

import java.time.{LocalDateTime, ZoneId}

/** A utility container object providing standard out-of-the-box typeclass instances for time
  * conversion (`To`) and lifecycle expiration checks (`Expirable`) across different time
  * representations (such as `Long` epoch values and `java.time.LocalDateTime`).
  */
object Implicits {

  /** Provides typeclass instances for handling claims where time is represented as raw epoch `Long`
    * timestamps.
    */
  object LongInstances {

    /** Provides an identity conversion instance for `To[F, C[Long], C[Long]]`.
      *
      * Since the time representation in the claim is already a `Long` (matching the raw JSON
      * numeric format), this converter acts as a no-op that lifts the claim directly into the
      * effect type `F`.
      *
      * @tparam F
      *   The effect type constructor, which must have an instance of `cats.Applicative`.
      * @tparam C
      *   The higher-kinded type representing the JWT Claim, bounded by [[Claim]].
      * @return
      *   An instance of `To[F, C[Long], C[Long]]`.
      */
    implicit def fromLongToLong[F[*]: Applicative, C[X] <: Claim[X]]: To[F, C[Long], C[Long]] =
      (a: C[Long]) => a.pure[F]

    /** Provides an `Expirable` typeclass instance for claims containing `Long` epoch timestamps.
      *
      * This implementation inspects the `exp` field of the claim. If an expiration timestamp is
      * present, it converts it to a `LocalDateTime` using the implicit `ZoneId` in scope and checks
      * whether it is before the current local time (`LocalDateTime.now()`). If it has expired, it
      * raises an [[JWTException.ExpiredTokenException]] suspended in the effect `F`; otherwise, it
      * returns the validated claim suspended in `F`.
      *
      * @tparam F
      *   The effect type constructor, requiring `cats.ApplicativeError[F, Throwable]`.
      * @tparam C
      *   The higher-kinded type representing the JWT Claim, bounded by [[Claim]].
      * @param zoneId
      *   The implicit time zone required to convert epoch time to local date-time.
      * @return
      *   An instance of `Expirable[F, Long, C]`.
      */
    implicit def expirable[F[*]: ApplicativeError[*[*], Throwable], C[X] <: Claim[X]](implicit
        zoneId: ZoneId
    ): Expirable[F, Long, C] = (claim: C[Long]) =>
      claim.exp match {
        case Some(expirable) if expirable.toLocalDateTime isBefore LocalDateTime.now() =>
          ExpiredTokenException.raiseError[F, C[Long]]
        case _ => claim.pure[F]
      }

    /** Provides typeclass instances for handling claims where time is represented natively as
      * `java.time.LocalDateTime`.
      */
    object LocalDateTimeInstances {

      /** Provides an `Expirable` typeclass instance for claims containing `java.time.LocalDateTime`
        * expiration times.
        *
        * This implementation inspects the `exp` field of the claim. If an expiration date-time is
        * present and is before the current local time (`LocalDateTime.now()`), it raises an
        * [[JWTException.ExpiredTokenException]] suspended in the effect `F`. Otherwise, it returns
        * the validated claim suspended in `F`.
        *
        * @tparam F
        *   The effect type constructor, requiring `cats.ApplicativeError[F, Throwable]`.
        * @tparam C
        *   The higher-kinded type representing the JWT Claim, bounded by [[Claim]].
        * @return
        *   An instance of `Expirable[F, LocalDateTime, C]`.
        */
      implicit def expirable[F[*]: ApplicativeError[*[*], Throwable], C[X] <: Claim[X]]
          : Expirable[F, LocalDateTime, C] = (claim: C[LocalDateTime]) =>
        claim.exp match {
          case Some(expirable) if expirable isBefore LocalDateTime.now() =>
            ExpiredTokenException.raiseError[F, C[LocalDateTime]]
          case _ => claim.pure[F]
        }
    }
  }
}
