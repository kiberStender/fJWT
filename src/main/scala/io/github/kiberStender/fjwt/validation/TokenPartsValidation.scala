package io.github.kiberStender
package fjwt
package validation

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}

import io.github.kiberStender.fjwt.error.JWTError.WrongTokenParts

trait TokenPartsValidation[F[*]]:
  /** A method that validates if the token has 3 parts(header, payload and signature)
    *
    * @param token
    *   The token to be validated
    * @return
    *   An Tuple3 containing each part of the token or an {@link WrongTokenParts} error wrapped in F
    */
  def validate(token: String): F[(String, String, String)]

/** Instance factory for {@link TokenPartsValidation}
  */
object TokenPartsValidation:
  /** Method to instantiate a {@link TokenPartsValidation}[F]
    * @tparam F
    *   An instance of {@link ApplicativeError}[F, Throwable]
    * @return
    *   AN instance of {@link TokenPartsValidation}[F]
    */
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: TokenPartsValidation[F] =
    (token: String) =>
      token split "\\." match
        case Array(header, payload, signature) => (header, payload, signature).pure[F]
        case _ => WrongTokenParts.raiseError[F, (String, String, String)]
