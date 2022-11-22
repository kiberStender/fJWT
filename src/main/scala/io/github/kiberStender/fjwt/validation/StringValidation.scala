package io.github.kiberStender
package fjwt
package validation

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.error.JWTError

trait StringValidation[F[*]]:
  def validate[E <: Throwable](token: String)(nullCase: => E)(emptyCase: => E): F[String]

object StringValidation:
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: StringValidation[F] =
    new StringValidation[F]:
      override def validate[E <: Throwable](
          token: String
      )(nullCase: => E)(emptyCase: => E): F[String] =
        if token == null then nullCase.raiseError[F, String]
        else if token.isEmpty then emptyCase.raiseError[F, String]
        else token.pure[F]
