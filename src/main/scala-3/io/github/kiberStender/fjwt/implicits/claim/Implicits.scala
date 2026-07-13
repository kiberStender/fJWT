package io.github.kiberStender
package fjwt
package implicits
package claim

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.claim.{Expirable, FromLong, ToLong}
import io.github.kiberStender.fjwt.exception.JWTError.ExpiredTokenError
import io.github.kiberStender.fjwt.models.Claim

import java.time.{LocalDateTime, ZoneId}

object Implicits:
  object LongInstances:
    given toLong[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: ToLong[F, Long] with
      def toLong(
          claim: Claim[Long]
      ): F[Claim[Long]] = claim.pure[F]

    given fromLong[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: FromLong[F, Long] with
      def fromLong(
          claim: Claim[Long]
      ): F[Claim[Long]] = claim.pure[F]

    given expirable[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]](using
        zoneId: ZoneId
    ): Expirable[F, Long] with
      def isExpired(
          claim: Claim[Long]
      ): F[Boolean] =
        claim.exp
          .map {
            case exp if exp.toLocalDateTime isAfter LocalDateTime.now() => true.pure[F]
            case _ => ExpiredTokenError.raiseError[F, Boolean]
          }
          .getOrElse(false.pure[F])

  object LocalDateTimeInstances:
    given toLong[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]](using
        zoneId: ZoneId
    ): ToLong[F, LocalDateTime] with
      def toLong(
          claim: Claim[LocalDateTime]
      ): F[Claim[Long]] = Claim[Long](
        iss = claim.iss,
        sub = claim.sub,
        aud = claim.aud,
        exp = claim.exp.map(_.toEpochMilli),
        nbf = claim.nbf.map(_.toEpochMilli),
        iat = claim.iat.map(_.toEpochMilli),
        jti = claim.jti
      ).pure[F]

    given fromLong[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]](using
        zoneId: ZoneId
    ): FromLong[F, LocalDateTime] with
      def fromLong(
          claim: Claim[Long]
      ): F[Claim[LocalDateTime]] = Claim[LocalDateTime](
        iss = claim.iss,
        sub = claim.sub,
        aud = claim.aud,
        exp = claim.exp.map(_.toLocalDateTime),
        nbf = claim.nbf.map(_.toLocalDateTime),
        iat = claim.iat.map(_.toLocalDateTime),
        jti = claim.jti
      ).pure[F]

    given expirable[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: Expirable[F, LocalDateTime]
      with
      def isExpired(
          claim: Claim[LocalDateTime]
      ): F[Boolean] = claim.exp
        .map {
          case exp if exp isAfter LocalDateTime.now() => true.pure[F]
          case _                                      => ExpiredTokenError.raiseError[F, Boolean]
        }
        .getOrElse(false.pure[F])
