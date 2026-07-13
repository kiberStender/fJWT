package io.github.kiberStender
package fjwt
package header

import cats.implicits.catsSyntaxApplicativeErrorId
import cats.syntax.all.catsSyntaxApplicativeId
import io.github.kiberStender.fjwt.exception.JWTError.InvalidAlgError
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.{HmacSHA1, HmacSHA512}
import org.scalatest._
import flatspec._
import io.github.kiberStender.fjwt.header.AlgTasks._
import matchers._

class AlgTasksTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen {
  "Extract" should "extract alg HS1 from the given json" in {
    type F[T] = Either[Throwable, T]
    Given("""{"alg":"HS1","typ":"JWT"}""")
    val headerJson = """{"alg":"HS1","typ":"JWT"}"""
    val expected: F[HmacAlgorithm] = HmacSHA1.pure[F]

    When("extracting the alg value")
    val actual = headerJson.extractAlg[F]

    Then("it should return HmacSHA1")
    actual should equal(expected)
  }

  it must "extract alg HS512 from the given json" in {
    type F[T] = Either[Throwable, T]
    Given("""{"alg":"HS512","typ":"JWT"}""")
    val headerJson = """{"alg":"HS512","typ":"JWT"}"""
    val expected: F[HmacAlgorithm] = HmacSHA512.pure[F]

    When("extracting the alg value")
    val actual = headerJson.extractAlg[F]

    Then("it should return HmacSHA1")
    actual should equal(expected)
  }

  it must "throw an exception from the given json" in {
    type F[T] = Either[Throwable, T]
    Given("""{"alg":"HS381","typ":"JWT"}""")
    val headerJson = """{"alg":"HS381","typ":"JWT"}"""
    val expected: F[HmacAlgorithm] = InvalidAlgError("HS381").raiseError[F, HmacAlgorithm]

    When("extracting the alg value")
    val actual = headerJson.extractAlg[F]

    Then("it throw an exception")
    actual should equal(expected)
  }

  it must "throw an exception from the given algless json" in {
    type F[T] = Either[Throwable, T]
    Given("""{"typ":"JWT"}""")
    val headerJson = """{"typ":"JWT"}"""
    val expected: F[HmacAlgorithm] = InvalidAlgError("Empty value").raiseError[F, HmacAlgorithm]

    When("extracting the alg value")
    val actual = headerJson.extractAlg[F]

    Then("it throw an exception")
    actual should equal(expected)
  }
}
