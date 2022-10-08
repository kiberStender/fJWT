package br.com.fjwt.validation

import br.com.fjwt.error.JWTError
import br.com.fjwt.error.JWTError.NullPrivateKey
import br.com.fjwt.error.JWTError.EmptyPrivateKey
import br.com.fjwt.error.JWTError.NullToken
import br.com.fjwt.error.JWTError.EmptyToken

import org.scalatest.flatspec.AnyFlatSpecLike

import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId

class CodecValidationTest extends AnyFlatSpecLike:
  private type F = [T] =>> Either[JWTError, T]
  private lazy val codecValidation: CodecValidation[F] = CodecValidation.dsl

  "CodecValidation" should "return a valid privateKey when it is not empty nor null" in {
    // GIVEN
    val input: String = "a-key"
    val expected: F[String] = input.pure[F]

    // WHEN
    val actual = codecValidation.validatePrivateKey(input)

    // THEN
    assert(actual === expected)
  }

  it must "return an EmptyPrivateKey" in {
    // GIVEN
    val input: String = ""
    val expected: F[String] = EmptyPrivateKey.raiseError[F, String]

    // WHEN
    val actual = codecValidation.validatePrivateKey(input)

    // THEN
    assert(actual === expected)
  }

  it must "return a NullPrivateKey" in {
    // GIVEN
    val input: String = null
    val expected: F[String] = NullPrivateKey.raiseError[F, String]

    // WHEN
    val actual = codecValidation.validatePrivateKey(input)

    // THEN
    assert(actual === expected)
  }

  it must "return a valid accessToken when it is not empty nor null" in {
    // GIVEN
    val input: String = "a-token"
    val expected: F[String] = input.pure[F]

    // WHEN
    val actual = codecValidation.validateToken(input)

    // THEN
    assert(actual === expected)
  }

  it must "return an EmptyToken" in {
    // GIVEN
    val input: String = ""
    val expected: F[String] = EmptyToken.raiseError[F, String]

    // WHEN
    val actual = codecValidation.validateToken(input)

    // THEN
    assert(actual === expected)
  }

  it must "return a NullToken" in {
    // GIVEN
    val input: String = null
    val expected: F[String] = NullToken.raiseError[F, String]

    // WHEN
    val actual = codecValidation.validateToken(input)

    // THEN
    assert(actual === expected)
  }