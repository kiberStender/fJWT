package io.github.kiberStender
package fjwt
package validation

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.error.JWTError

/** A trait that describes A String validation typeclass
  * @tparam F
  *   The container type to wrap the return value
  */
trait StringValidation[F[*]]:
  /** The method to validate a given String
    * @param token
    *   The string to be validated
    * @param nullCase
    *   The function to be triggered in case the String is null
    * @param emptyCase
    *   The function to be triggered in case the String is empty
    * @tparam E
    *   The type to constrain the errors
    * @return
    *   Either the valid String or some error describing what went wrong
    */
  def validate[E <: Throwable](token: String)(nullCase: => E)(emptyCase: => E): F[String]

/** Instance factory for {@link StringValidation}
  */
object StringValidation:
  /** A method to create an instance of {@link StringValidation} that requires an instance of {@link
    * ApplicativeError}[F, Throwable]
    * @tparam F
    *   An instance of {@link ApplicativeError}[F, Throwable]
    * @return
    *   The instance of {@link StringValidation}[F]
    */
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: StringValidation[F] =
    new StringValidation[F]:
      override def validate[E <: Throwable](
          token: String
      )(nullCase: => E)(emptyCase: => E): F[String] =
        if token == null then nullCase.raiseError[F, String]
        else if token.isEmpty then emptyCase.raiseError[F, String]
        else token.pure[F]
