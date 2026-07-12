package io.github.kiberStender
package fjwt
package claim

import cats.implicits.catsSyntaxEq
import cats.MonadError
import cats.syntax.all.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import io.github.kiberStender.fjwt.models.Claim

object ClaimTasks {
  private[fjwt] implicit class ClaimStringOps(claimJsonStr: String) {
    def extractClaim[F[*]: MonadError[*[*], Throwable]: FromLong[*[*], T], T]: F[Claim[T]] = for {
      iss <- extractField(""""iss"\s*:\s*"([^"]+)"""".r)(claimJsonStr)
      sub <- extractField(""""sub"\s*:\s*"([^"]+)"""".r)(claimJsonStr)
      aud <- extractField(""""aud"\s*:\s*"([^"]+)"""".r)(claimJsonStr)
      exp <- extractField(""""exp"\s*:\s*(\d+)""".r)(claimJsonStr).map(_.map(_.toLong))
      nbf <- extractField(""""nbf"\s*:\s*(\d+)""".r)(claimJsonStr).map(_.map(_.toLong))
      iat <- extractField(""""iat"\s*:\s*(\d+)""".r)(claimJsonStr).map(_.map(_.toLong))
      jti <- extractField(""""iss"\s*:\s*"([^"]+)"""".r)(claimJsonStr)
      claim <- implicitly[FromLong[F, T]].fromLong(Claim[Long](iss, sub, aud, exp, nbf, iat, jti))
    } yield claim
  }

  private[fjwt] implicit class ClaimOps[T](claim: Claim[T]) {
    private def comma(prev: String): String = if (prev === "") "" else ","

    def toJson[F[*]: MonadError[*[*], Throwable]: ToLong[*[*], T]]: F[String] =
      implicitly[ToLong[F, T]].toLong(claim).flatMap {
        case Claim(issOrig, subOrig, audOrig, expOrig, nbfOrig, iatOrig, jtiOrig) =>
          for {
            iss <- issOrig.map(s => s""""iss":"$s"""").getOrElse("").pure[F]
            sub <- subOrig.map(s => s""""sub":"$s"""").getOrElse("").pure[F]
            aud <- audOrig.map(s => s""""aud":"$s"""").getOrElse("").pure[F]
            exp <- expOrig.map(s => s""""exp":$s""").getOrElse("").pure[F]
            nbf <- nbfOrig.map(s => s""""nbf":$s""").getOrElse("").pure[F]
            iat <- iatOrig.map(s => s""""iat":$s""").getOrElse("").pure[F]
            jti <- jtiOrig.map(s => s""""jti":"$s"""").getOrElse("").pure[F]
          } yield s"{$iss${comma(iss)}$sub${comma(aud)}$aud${comma(exp)}$exp${comma(
              nbf
            )}$nbf${comma(iat)}$iat${comma(jti)}$jti}"
      }
  }
}
