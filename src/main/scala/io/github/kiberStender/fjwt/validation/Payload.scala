package io.github.kiberStender
package fjwt
package validation

import cats.ApplicativeError
import cats.syntax.all.{
  catsSyntaxApplicativeErrorId,
  catsSyntaxApplicativeId,
  catsSyntaxEq,
  toFlatMapOps,
  toFunctorOps
}
import io.circe.Codec

import io.github.kiberStender.fjwt.error.JWTError.ExpiredToken
import io.github.kiberStender.fjwt.model.Claim

import java.time.{Instant, LocalDateTime, ZoneId}

object Payload:
  type Payload[P] = (Claim, P)
  extension [P: Codec](jwtPayload: Payload[P]) {
    def isExpired[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]](using
        zoneId: ZoneId
    ): F[Payload[P]] = jwtPayload match {
      case (claim, _) =>
        claim.exp
          .map { exp =>
            val localDateExp = Instant.ofEpochMilli(exp).atZone(zoneId).toLocalDateTime
            if localDateExp isAfter LocalDateTime.now() then jwtPayload.pure[F]
            else ExpiredToken.raiseError[F, Payload[P]]
          }
          .getOrElse(jwtPayload.pure[F])
    }
  }
