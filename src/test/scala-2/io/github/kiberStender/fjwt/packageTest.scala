package io.github.kiberStender
package fjwt

import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.github.kiberStender.fjwt.exception.JWTError.{Not2TokenPartsError, Not3TokenPartsError}
import org.scalatest._
import flatspec._
import matchers._

class packageTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen {
  "isEmptyValue" should "throw the correct error inm case of null value" in {
    type F[T] = Either[Throwable, T]

    Given("a null value")
    val key: String = null
    When("checking")
    val actual = key.isEmptyValue[F, Throwable, Throwable](new Throwable("Null"))(new Throwable("Empty"))

    Then("it should return Null")
    actual.fold(_.getMessage should equal("Null"), _ => assert(false))
  }

  it should "throw the correct error inm case of empty value" in {
    type F[T] = Either[Throwable, T]

    Given("a null value")
    val key = ""

    When("checking")
    val actual = key.isEmptyValue[F, Throwable, Throwable](new Throwable("Null"))(new Throwable("Empty"))

    Then("it should return Empty")
    actual.isLeft should be(true)
    actual.fold(_.getMessage should equal("Empty"), _ => assert(false))
  }

  "Merge" should "merge two string formatted Json" in {
    Given("""Given the jsons {"field1":3,"field4":5} and {"field3":98}""")
    val str1 = """{"field1":3,"field4":5}"""
    val str2 = """{"field3":98}"""
    val expected = """{"field1":3,"field4":5,"field3":98}"""

    When("Merging both")
    val actual = str1 merge str2

    Then(s"it should produce $expected")
    actual should equal(expected)
  }

  "is2Parts" should "return a tuple of 2 items if the token has exact 2 parts" in {
    type F[T] = Either[Throwable, T]

    Given("the token abcd.ab")
    val token = "abcd.ab"
    val expected = ("abcd", "ab").pure[F]

    When("checking if it has at least 2 parts")
    val actual = token.is2Parts[F]

    Then(s"it should return $expected")
    actual.isRight should equal(true)
    actual should be(expected)
  }

  it should "return a tuple of 2 items if the token has more 2 part" in {
    type F[T] = Either[Throwable, T]

    Given("the token abcd.ab.cdef")
    val token = "abcd.ab.cdef"
    val expected = ("abcd", "ab").pure[F]

    When("checking if it has at least 2 parts")
    val actual = token.is2Parts[F]

    Then(s"it should return $expected")
    actual.isRight should equal(true)
    actual should be(expected)
  }

  it should "return the error Not2TokenParts if the token has less than 2 parts" in {
    type F[T] = Either[Throwable, T]

    Given("the token abcdabcdef")
    val token = "abcdabcdef"
    val expected = (Not2TokenPartsError: Throwable).raiseError[F, (String, String)]

    When("checking if it has at least 2 parts")
    val actual = token.is2Parts[F]

    Then(s"it should return $expected")
    actual.isLeft should equal(true)
    actual should be(expected)
  }

  "is3Parts" should "returns a tuple of 3 items if the token has exact 3 parts" in {
    type F[T] = Either[Throwable, T]

    Given("the token abcd.ab.cdef")
    val token = "abcd.ab.cdef"
    val expected = ("abcd", "ab", "cdef").pure[F]

    When("checking if it has at least 3 parts")
    val actual = token.is3Parts[F]

    Then(s"it should return $expected")
    actual.isRight should equal(true)
    actual should be(expected)
  }

  it should "returns a tuple of 3 items if the token has more 3 part" in {
    type F[T] = Either[Throwable, T]

    Given("the token abcd.ab.cdef.ghs")
    val token = "abcd.ab.cdef.ghs"
    val expected = ("abcd", "ab", "cdef").pure[F]

    When("checking if it has at least 3 parts")
    val actual = token.is3Parts[F]

    Then(s"it should return $expected")
    actual.isRight should equal(true)
    actual should be(expected)
  }

  it should "return the error Not3TokenParts if the token has 1 part" in {
    type F[T] = Either[Throwable, T]

    Given("the token abcdabcdef")
    val token = "abcdabcdef"
    val expected = (Not3TokenPartsError: Throwable).raiseError[F, (String, String, String)]

    When("checking if it has at least 3 parts")
    val actual = token.is3Parts[F]

    Then(s"it should return $expected")
    actual.isLeft should equal(true)
    actual should be(expected)
  }

  it should "return the error Not3TokenParts if the token has 2 part" in {
    type F[T] = Either[Throwable, T]

    Given("the token abcd.abcdef")
    val token = "abcd.abcdef"
    val expected = (Not3TokenPartsError: Throwable).raiseError[F, (String, String, String)]

    When("checking if it has at least 3 parts")
    val actual = token.is3Parts[F]

    Then(s"it should return $expected")
    actual.isLeft should equal(true)
    actual should be(expected)
  }
}
