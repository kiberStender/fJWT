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

package object fjwt:

  private[fjwt] def extractField[F[*]: [F[*]] =>> MonadError[F, Throwable]](pattern: Regex)(
      json: String
  ): F[Option[String]] =
    pattern findFirstMatchIn json match
      case Some(m) =>
        m.group(1)
          .pure[F]
          .map(value => if (value.isEmpty || value === "null") None else Some(value))
      case None => (None: Option[String]).pure[F]

  extension (str: String) {
    private[fjwt] def toBytesUTF8: Array[Byte] = str getBytes StandardCharsets.UTF_8

    private[fjwt] def isEmptyValue[F[*]: [F[*]] =>> ApplicativeError[
      F,
      Throwable
    ], N <: Throwable, E <: Throwable](nullCase: => N)(emptyCase: => E): F[String] =
      if str === null then nullCase.raiseError[F, String]
      else if str.isEmpty then emptyCase.raiseError[F, String]
      else str.pure[F]

    private[fjwt] def merge(strJson: String): String =
      s"${str.stripSuffix("}")},${strJson.stripPrefix("{")}"

    private[fjwt] def is2Parts[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]
        : F[(String, String)] =
      str split "\\." match
        case Array(header, payload, _*) => (header, payload).pure[F]
        case _                          => Not2TokenParts.raiseError[F, (String, String)]

    private[fjwt] def is3Parts[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]
        : F[(String, String, String)] =
      str split "\\." match
        case Array(header, payload, signature, _*) => (header, payload, signature).pure[F]
        case _ => Not3TokenParts.raiseError[F, (String, String, String)]

    private[fjwt] def isValidSignature[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]](
        originalSignature: String
    ): F[Boolean] =
      if str === originalSignature then true.pure[F] else InvalidSignature.raiseError[F, Boolean]
  }

  extension (n: Long) {
    def toLocalDateTime(using zoneId: ZoneId): LocalDateTime =
      Instant.ofEpochMilli(n).atZone(zoneId).toLocalDateTime
  }

  extension (ldt: LocalDateTime) {
    def toEpochMilli(using zoneId: ZoneId): Long =
      ZonedDateTime.of(ldt, zoneId).toInstant.toEpochMilli
  }

  extension (alg: HmacAlgorithm) {
    def hash[F[*]: Hmac](privateKey: String)(str: String): F[Array[Byte]] =
      implicitly[Hmac[F]].hash(alg)(privateKey)(str)
  }
