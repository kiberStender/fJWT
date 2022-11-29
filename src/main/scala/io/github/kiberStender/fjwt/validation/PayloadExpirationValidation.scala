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

import java.time.{Instant, LocalDateTime, ZoneId}

/** Typeclass to validate if a given JWT is expired
  * @tparam F
  *   The container to wrap the return methods
  */
trait PayloadExpirationValidation[F[*]]:
  /** Method to validate the token by checking if the expired at (exp) field is not empty and if so,
    * check against the actual time to see if the token is expired or not
    * @param claim
    *   The object that contains the expired at field
    * @param zoneId
    *   The zone id in which the object was encoded
    * @tparam P
    *   The type of the Payload
    * @return
    *   Either the payload or an {@link ExpiredToken} error
    */
  def validate[P: Codec](claim: Claim[P])(using zoneId: ZoneId): F[P]

/** Instance factory for {@link PayloadExpirationValidation}
  */
object PayloadExpirationValidation:
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: PayloadExpirationValidation[F] =
    new PayloadExpirationValidation[F]:
      override def validate[P: Codec](claim: Claim[P])(using zoneId: ZoneId): F[P] =
        claim.exp
          .map { exp =>
            val localDateExp = Instant.ofEpochMilli(exp).atZone(zoneId).toLocalDateTime
            if localDateExp isAfter LocalDateTime.now() then claim.payload.pure[F]
            else ExpiredToken.raiseError[F, P]
          }
          .getOrElse(claim.payload.pure[F])
