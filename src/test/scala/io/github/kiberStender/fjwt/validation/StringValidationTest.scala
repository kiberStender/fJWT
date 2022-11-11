package io.github.kiberStender
package fjwt
package validation

import io.github.kiberStender.fjwt.error.JWTError.{EmptyToken, NullToken}
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.error.JWTError
import org.scalatest.flatspec.AnyFlatSpecLike

class StringValidationTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[Throwable, T]
  private lazy val stringValidation: StringValidation[F] = StringValidation.dsl

  "JwtTokenValidation" should "return a valid accessToken when it is not empty nor null" in {
    // GIVEN
    val input: String = "a-token"
    val expected: F[String] = input.pure[F]

    // WHEN
    val actual = stringValidation.validate(input)(NullToken)(EmptyToken)

    // THEN
    assert(actual === expected)
  }

  it must "return an EmptyToken" in {
    // GIVEN
    val input: String = ""
    val expected: F[String] = EmptyToken.raiseError[F, String]

    // WHEN
    val actual = stringValidation.validate(input)(NullToken)(EmptyToken)

    // THEN
    assert(actual === expected)
  }

  it must "return a NullToken" in {
    // GIVEN
    val input: String = null
    val expected: F[String] = NullToken.raiseError[F, String]

    // WHEN
    val actual = stringValidation.validate(input)(NullToken)(EmptyToken)

    // THEN
    assert(actual === expected)
  }
