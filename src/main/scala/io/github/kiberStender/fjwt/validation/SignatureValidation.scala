package io.github.kiberStender
package fjwt
package validation

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, catsSyntaxEq}
import io.github.kiberStender.fjwt.error.JWTError.InvalidSignature

/** Typeclass to validate a given signature
  * @tparam F
  *   The container to wrap the return methods
  */
trait SignatureValidation[F[*]]:
  /** Method to check if a given JWT signature is valid or not
    * @param encoded
    *   The signature that was generated using the two parts of the token(header and claim/payload)
    * @param originalSignature
    *   The original signature that came with the token (the third part of the JWT)
    * @return
    *   Either true or and error {@link InvalidSignature} wrapped in F
    */
  def validate(encoded: String)(originalSignature: String): F[Boolean]

/** Instance factory for {@link SignatureValidation}
  */
object SignatureValidation:
  /** A method to instantiate a {@link SignatureValidation}[F]
    * @tparam F
    *   An instance of {@link ApplicativeError}[F, Throwable]
    * @return
    *   An instance of {@link SignatureValidation}[F]
    */
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: SignatureValidation[F] =
    new SignatureValidation[F]:
      override def validate(encoded: String)(originalSignature: String): F[Boolean] =
        if encoded === originalSignature then true.pure[F]
        else InvalidSignature.raiseError[F, Boolean]
