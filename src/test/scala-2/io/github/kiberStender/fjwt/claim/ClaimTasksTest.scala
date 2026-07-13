package io.github.kiberStender
package fjwt
package claim

import cats.syntax.all.{catsSyntaxApplicativeId, catsSyntaxOptionId}
import org.scalatest._
import flatspec._
import io.github.kiberStender.fjwt.claim.ClaimTasks.{ClaimOps, ClaimStringOps}
import io.github.kiberStender.fjwt.models.Claim
import matchers._

import java.time.{LocalDateTime, ZoneId}

class ClaimTasksTest extends AnyFlatSpecLike with should.Matchers with GivenWhenThen {
  private type F[T] = Either[Throwable, T]

  "extractClaim" should "return a proper Claim[Long]" in {
    Given("""the payload: {"iss":null,"sub":"1234567890","aud":null,"exp":null,"nbf":null,"iat":1516239022,"jti":null,"name":"John Doe","admin":true} """)
    import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.fromLong
    val payload = """{"iss":null,"sub":"1234567890","aud":null,"exp":null,"nbf":null,"iat":1516239022,"jti":null,"name":"John Doe","admin":true}"""
    val expected: F[Claim[Long]] = Claim[Long](
      iss=None,
      sub="1234567890".some,
      aud=None,
      exp=None,
      nbf=None,
      iat=1516239022L.some,
      jti=None
    ).pure[F]

    When("parsing from the json")
    val actual = payload.extractClaim[F, Long]

    Then(s"it should return $expected")
    for {
      act <- actual
      exp <- expected
    } yield act should be(exp)
  }

  it should "return a proper Claim[Long] when fields are missing" in {
    Given("""the payload: {"sub":"1234567890","iat":1516239022,"name":"John Doe","admin":true}""")
    import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.fromLong
    val payload = """{"sub":"1234567890","iat":1516239022,"name":"John Doe","admin":true}"""
    val expected: F[Claim[Long]] = Claim[Long](
      iss=None,
      sub="1234567890".some,
      aud=None,
      exp=None,
      nbf=None,
      iat=1516239022L.some,
      jti=None
    ).pure[F]

    When("parsing from the json")
    val actual = payload.extractClaim[F, Long]

    Then(s"it should return $expected")
    for {
      act <- actual
      exp <- expected
    } yield act should be(exp)
  }

  it should "return a proper Claim[LocalDateTime]" in {
    Given("""the payload: {"iss":null,"sub":"1234567890","aud":null,"exp":null,"nbf":null,"iat":1516239022,"jti":null,"name":"John Doe","admin":true} """)
    implicit val zoneId: ZoneId = ZoneId.of("UTC")
    import io.github.kiberStender.fjwt.implicits.claim.Implicits.LocalDateTimeInstances.fromLong
    val payload = """{"iss":null,"sub":"1234567890","aud":null,"exp":null,"nbf":null,"iat":1516239022,"jti":null,"name":"John Doe","admin":true}"""
    val expected: F[Claim[LocalDateTime]] = Claim[LocalDateTime](
      iss=None,
      sub="1234567890".some,
      aud=None,
      exp=None,
      nbf=None,
      iat=1516239022L.toLocalDateTime.some,
      jti=None
    ).pure[F]

    When("parsing from the json")
    val actual = payload.extractClaim[F, LocalDateTime]

    Then(s"it should return $expected")
    for {
      act <- actual
      exp <- expected
    } yield act should be(exp)
  }

  it should "return a proper Claim[LocalDateTime] when fields are missing" in {
    Given("""the payload: {"sub":"1234567890","iat":1516239022,"name":"John Doe","admin":true}""")
    implicit val zoneId: ZoneId = ZoneId.of("UTC")
    import io.github.kiberStender.fjwt.implicits.claim.Implicits.LocalDateTimeInstances.fromLong
    val payload = """{"sub":"1234567890","iat":1516239022,"name":"John Doe","admin":true}"""
    val expected: F[Claim[LocalDateTime]] = Claim[LocalDateTime](
      iss=None,
      sub="1234567890".some,
      aud=None,
      exp=None,
      nbf=None,
      iat=1516239022L.toLocalDateTime.some,
      jti=None
    ).pure[F]

    When("parsing from the json")
    val actual = payload.extractClaim[F, LocalDateTime]

    Then(s"it should return $expected")
    for {
      act <- actual
      exp <- expected
    } yield act should be(exp)
  }

  "toJson" should "convert a given Claim[Long] to a json formatted String" in {
    Given("the payload:")
    import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.toLong

    val claim: Claim[Long] = Claim[Long](
      iss=None,
      sub="1234567890".some,
      aud=None,
      exp=None,
      nbf=None,
      iat=1516239022L.some,
      jti=None
    )
    val expected: F[String] = """{"sub":"1234567890","iat":1516239022}""".pure[F]

    When("converting to json")
    val actual = claim.toJson[F]

    Then(s"it should return $expected")
    for {
      act <- actual
      exp <- expected
    } yield act should be(exp)
  }

  it should "convert a given Claim[LocalDateTime] to a json formatted String" in {
    Given("the payload")
    implicit val zoneId: ZoneId = ZoneId.of("UTC")
    import io.github.kiberStender.fjwt.implicits.claim.Implicits.LocalDateTimeInstances.toLong

    val claim: Claim[LocalDateTime] = Claim[LocalDateTime](
      iss=None,
      sub="1234567890".some,
      aud=None,
      exp=None,
      nbf=None,
      iat=1516239022L.toLocalDateTime.some,
      jti=None
    )
    val expected: F[String] = """{"sub":"1234567890","iat":1516239022}""".pure[F]

    When("converting to json")
    val actual = claim.toJson[F]

    Then(s"it should return $expected")
    for {
      act <- actual
      exp <- expected
    } yield act should be(exp)
  }
}
