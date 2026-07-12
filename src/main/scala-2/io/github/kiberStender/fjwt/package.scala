package io.github.kiberStender

import cats.{ApplicativeError, MonadError}
import cats.syntax.all.{
  catsSyntaxApplicativeErrorId,
  catsSyntaxApplicativeId,
  catsSyntaxEq,
  toFunctorOps
}
import io.github.kiberStender.fjwt.crypto.hmac.Hmac
import io.github.kiberStender.fjwt.exception.JWTError.{
  InvalidSignature,
  Not2TokenParts,
  Not3TokenParts
}
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm

import java.nio.charset.StandardCharsets
import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}
import scala.util.matching.Regex

package object fjwt {

  private[fjwt] def extractField[F[*]: MonadError[*[_], Throwable]](
      pattern: Regex
  )(json: String): F[Option[String]] = {
    pattern findFirstMatchIn json match {
      case Some(m) =>
        m.group(1)
          .pure[F]
          .map(value => if (value.isEmpty || value === "null") None else Some(value))
      case None => (None: Option[String]).pure[F]
    }
  }

  implicit class StringOps(str: String) {
    private[fjwt] def toBytesUTF8: Array[Byte] = str getBytes StandardCharsets.UTF_8

    private[fjwt] def isEmptyValue[F[*]: ApplicativeError[
      *[_],
      Throwable
    ], N <: Throwable, E <: Throwable](nullCase: => N)(emptyCase: => E): F[String] =
      if (str === null) nullCase.raiseError[F, String]
      else if (str.isEmpty) emptyCase.raiseError[F, String]
      else str.pure[F]

    private[fjwt] def merge(strJson: String): String =
      s"${str.stripSuffix("}")},${strJson.stripPrefix("{")}"

    private[fjwt] def is2Parts[F[*]: ApplicativeError[*[_], Throwable]]: F[(String, String)] =
      str split "\\." match {
        case Array(header, payload, _*) => (header, payload).pure[F]
        case _                          => Not2TokenParts.raiseError[F, (String, String)]
      }

    private[fjwt] def is3Parts[F[*]: ApplicativeError[*[_], Throwable]]
        : F[(String, String, String)] =
      str split "\\." match {
        case Array(header, payload, signature, _*) => (header, payload, signature).pure[F]
        case _ => Not3TokenParts.raiseError[F, (String, String, String)]
      }

    private[fjwt] def isValidSignature[F[*]: ApplicativeError[*[_], Throwable]](
        originalSignature: String
    ): F[Boolean] =
      if (str === originalSignature) true.pure[F] else InvalidSignature.raiseError[F, Boolean]
  }

  implicit class LongOps(n: Long) {
    def toLocalDateTime(implicit zoneId: ZoneId): LocalDateTime =
      Instant.ofEpochMilli(n).atZone(zoneId).toLocalDateTime
  }

  implicit class LocalDateTimeOps(ldt: LocalDateTime) {
    def toEpochMilli(implicit zoneId: ZoneId): Long =
      ZonedDateTime.of(ldt, zoneId).toInstant.toEpochMilli
  }

  implicit class HmacAlgorithmOps(alg: HmacAlgorithm) {
    def hash[F[*]: Hmac](privateKey: String)(str: String): F[Array[Byte]] =
      implicitly[Hmac[F]].hash(alg)(privateKey)(str)
  }
}
