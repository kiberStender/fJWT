package br.com.fjwt
package validation

import br.com.fjwt.error.JWTError

import cats.ApplicativeError
import cats.syntax.all.catsSyntaxApplicativeErrorId
import cats.syntax.all.catsSyntaxApplicativeId

trait StringValidation[F[*]]:
  def validate[E <: JWTError](token: String)(nullCase: => E)(emptyCase: => E): F[String]

object StringValidation:
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, JWTError]]: StringValidation[F] =
    new StringValidation[F]:
      override def validate[E <: JWTError](
          token: String
      )(nullCase: => E)(emptyCase: => E): F[String] =
        if token == null then nullCase.raiseError[F, String]
        else if token.isEmpty then emptyCase.raiseError[F, String]
        else token.pure[F]
