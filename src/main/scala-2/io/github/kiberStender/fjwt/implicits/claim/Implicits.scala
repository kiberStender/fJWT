package io.github.kiberStender
package fjwt
package implicits
package claim

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.claim.{Expirable, FromLong, ToLong}
import io.github.kiberStender.fjwt.exception.JWTException.ExpiredTokenException
import io.github.kiberStender.fjwt.models.Claim

import java.time.{LocalDateTime, ZoneId}

object Implicits {
  object LongInstances {

    /** A convenience instance of [[ToLong]] that "converts" Long to Long
      * @tparam F
      *   The effect type
      * @return
      *   An instance of [[ToLong[F, Long]]]
      */
    implicit def toLong[F[*]: ApplicativeError[*[*], Throwable]]: ToLong[F, Long] =
      (claim: Claim[Long]) => claim.pure[F]

    /** A convenience instance of [[FromLong]] that "converts" Long to Long
      * @tparam F
      *   The effect type
      * @return
      *   An instance of [[FromLong[F, Long]]]
      */
    implicit def fromLong[F[*]: ApplicativeError[*[*], Throwable]]: FromLong[F, Long] =
      (claim: Claim[Long]) => claim.pure[F]

    /** An instance of [[Expirable]] check if a Claim[Long] is expired or still valid
      *
      * @param zoneId
      *   The time zone of the user to properly convert the raw Long time type to a
      *   [[LocalDateTime]]
      * @tparam F
      *   The effect type
      * @return
      *   An instance of [[Expirable[F, Long]]]
      */
    implicit def expirable[F[*]: ApplicativeError[*[*], Throwable]](implicit
        zoneId: ZoneId
    ): Expirable[F, Long] = (claim: Claim[Long]) =>
      claim.exp
        .map {
          case exp if exp.toLocalDateTime isAfter LocalDateTime.now() => true.pure[F]
          case _ => ExpiredTokenException.raiseError[F, Boolean]
        }
        .getOrElse(false.pure[F])
  }

  object LocalDateTimeInstances {

    /** A convenience instance of [[ToLong]] that converts Long to [[LocalDateTime]]
      * @param zoneId
      *   The time zone of the user to properly convert the raw Long time type to a
      *   [[LocalDateTime]]
      * @tparam F
      *   The effect type
      * @return
      *   An instance of [[ToLong[F, LocalDateTime]]]
      */
    implicit def toLong[F[*]: ApplicativeError[*[*], Throwable]](implicit
        zoneId: ZoneId
    ): ToLong[F, LocalDateTime] = (claim: Claim[LocalDateTime]) =>
      Claim[Long](
        iss = claim.iss,
        sub = claim.sub,
        aud = claim.aud,
        exp = claim.exp.map(_.toEpochMilli),
        nbf = claim.nbf.map(_.toEpochMilli),
        iat = claim.iat.map(_.toEpochMilli),
        jti = claim.jti
      ).pure[F]

    /** A convenience instance of [[FromLong]] that converts [[LocalDateTime]] to Long
      * @param zoneId
      *   The time zone of the user to properly convert the raw Long time type to a
      *   [[LocalDateTime]]
      * @tparam F
      *   The effect type
      * @return
      *   An instance of [[FromLong[F, LocalDateTime]]]
      */
    implicit def fromLong[F[*]: ApplicativeError[*[*], Throwable]](implicit
        zoneId: ZoneId
    ): FromLong[F, LocalDateTime] = (claim: Claim[Long]) =>
      Claim[LocalDateTime](
        iss = claim.iss,
        sub = claim.sub,
        aud = claim.aud,
        exp = claim.exp.map(_.toLocalDateTime),
        nbf = claim.nbf.map(_.toLocalDateTime),
        iat = claim.iat.map(_.toLocalDateTime),
        jti = claim.jti
      ).pure[F]

    /** An instance of [[Expirable]] check if a Claim[LocalDateTime] is expired or still valid
      * @tparam F
      *   The effect type
      * @return
      *   An instance of [[Expirable[F, LocalDateTime]]]
      */
    implicit def expirable[F[*]: ApplicativeError[*[*], Throwable]]: Expirable[F, LocalDateTime] =
      (claim: Claim[LocalDateTime]) =>
        claim.exp
          .map {
            case exp if exp isAfter LocalDateTime.now() => true.pure[F]
            case _ => ExpiredTokenException.raiseError[F, Boolean]
          }
          .getOrElse(false.pure[F])
  }
}
