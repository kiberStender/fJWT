package io.github.kiberStender

import cats.{ApplicativeError, MonadError}
import cats.syntax.all.{
  catsSyntaxApplicativeErrorId,
  catsSyntaxApplicativeId,
  catsSyntaxEq,
  toFlatMapOps
}
import io.github.kiberStender.fjwt.crypto.base64.Base64Encoder
import io.github.kiberStender.fjwt.crypto.hmac.Hmac
import io.github.kiberStender.fjwt.exception.JWTException.{
  InvalidSignatureException,
  Not2TokenPartsException,
  Not3TokenPartsException
}
import io.github.kiberStender.fjwt.models.Header

import java.nio.charset.StandardCharsets
import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}

/** The root package object for the FJWT library, providing extension methods and implicit classes
  * that streamline string manipulation, token parsing, signature computation and validation, and
  * time conversion across effect types.
  */
package object fjwt {

  /** Extension methods for `String` providing JWT-specific parsing, validation, signature checking,
    * and transformation operations lifted into effect types.
    *
    * @param str
    *   The underlying string being enhanced (typically representing a token or body segment).
    */
  implicit class StringOps(str: String) {

    /** Converts the string into a UTF-8 encoded byte array.
      */
    private[fjwt] def toBytesUTF8: Array[Byte] = str getBytes StandardCharsets.UTF_8

    /** Validates that the string is neither null nor empty, lifting custom error instances into the
      * effect type `F` if a validation failure occurs.
      *
      * @tparam F
      *   The effect type constructor, requiring `cats.ApplicativeError[F, Throwable]`.
      * @tparam N
      *   The type of the exception raised if the string is null.
      * @tparam E
      *   The type of the exception raised if the string is empty.
      * @param nullCase
      *   A call-by-name exception instance for the null case.
      * @param emptyCase
      *   A call-by-name exception instance for the empty case.
      * @return
      *   The original string, suspended in `F` if checks pass.
      */
    private[fjwt] def isEmptyValue[F[*]: ApplicativeError[
      *[*],
      Throwable
    ], N <: Throwable, E <: Throwable](nullCase: => N)(emptyCase: => E): F[String] =
      if (str === null) nullCase.raiseError[F, String]
      else if (str.isEmpty) emptyCase.raiseError[F, String]
      else str.pure[F]

    /** Merges two JSON object strings by stripping the closing brace of the first and the opening
      * brace of the second, combining them into a single JSON object representation.
      */
    private[fjwt] def merge(strJson: String): String =
      s"${str.stripSuffix("}")},${strJson.stripPrefix("{")}"

    /** Splits a raw token string into its header and payload segments, expecting at least two
      * dot-separated parts (`header.payload`).
      *
      * @tparam F
      *   The effect type constructor, requiring `cats.ApplicativeError[F, Throwable]`.
      * @return
      *   A tuple of `(header, payload)` strings suspended in `F`, or a
      *   [[JWTException.Not2TokenPartsException]] if malformed.
      */
    private[fjwt] def toHeaderAndPayload[F[*]: ApplicativeError[*[*], Throwable]]
        : F[(String, String)] =
      str split "\\." match {
        case Array(header, payload, _*) => (header, payload).pure[F]
        case _                          => Not2TokenPartsException.raiseError[F, (String, String)]
      }

    /** Splits a raw token string into its header, payload, and signature segments, expecting at
      * least three dot-separated parts (`header.payload.signature`).
      *
      * @tparam F
      *   The effect type constructor, requiring `cats.ApplicativeError[F, Throwable]`.
      * @return
      *   A tuple of `(header, payload, signature)` strings suspended in `F`, or a
      *   [[JWTException.Not3TokenPartsException]] if malformed.
      */
    private[fjwt] def toHeaderPayloadAndSignature[F[*]: ApplicativeError[*[*], Throwable]]
        : F[(String, String, String)] =
      str split "\\." match {
        case Array(header, payload, signature, _*) => (header, payload, signature).pure[F]
        case _ => Not3TokenPartsException.raiseError[F, (String, String, String)]
      }

    /** Computes the HMAC signature of the underlying string using the provided header and private
      * key, encodes the resulting hash into URL-safe Base64, and verifies it against the original
      * signature.
      *
      * @tparam F
      *   The effect type constructor, requiring `cats.MonadError[F, Throwable]`, `Hmac`, and
      *   `Base64Encoder`.
      * @param header
      *   The JWT header containing algorithm configuration details.
      * @param key
      *   The secret private key used for HMAC calculation.
      * @param origSignature
      *   The original signature string extracted from the token to compare against.
      * @return
      *   `true` suspended in `F` if the calculated signature matches the original, or a suspended
      *   [[JWTException.InvalidSignatureException]] if they differ.
      */
    private[fjwt] def validateSignature[F[*]: MonadError[*[*], Throwable]: Hmac: Base64Encoder](
        header: Header
    )(key: String)(origSignature: String): F[Boolean] =
      implicitly[Hmac[F]]
        .hash(header)(key)(str)
        .flatMap(implicitly[Base64Encoder[F]].encodeURLSafe)
        .flatMap { calc_sig =>
          if (calc_sig === origSignature) true.pure[F]
          else InvalidSignatureException.raiseError[F, Boolean]
        }
  }

  /** Extension methods for `Long` epoch timestamp values to facilitate conversion into local
    * date-time representations.
    *
    * @param n
    *   The underlying epoch timestamp in milliseconds.
    */
  implicit class LongOps(n: Long) {

    /** Converts an epoch millisecond timestamp into a `java.time.LocalDateTime` using the implicit
      * time zone in scope.
      *
      * @param zoneId
      *   The implicit time zone required for conversion.
      * @return
      *   The equivalent `LocalDateTime`.
      */
    def toLocalDateTime(implicit zoneId: ZoneId): LocalDateTime =
      Instant.ofEpochMilli(n).atZone(zoneId).toLocalDateTime
  }

  /** Extension methods for `java.time.LocalDateTime` to facilitate conversion back into epoch
    * timestamp representations.
    *
    * @param ldt
    *   The underlying `LocalDateTime` being enhanced.
    */
  implicit class LocalDateTimeOps(ldt: LocalDateTime) {

    /** Converts a `java.time.LocalDateTime` into an epoch millisecond timestamp using the implicit
      * time zone in scope.
      *
      * @param zoneId
      *   The implicit time zone required for conversion.
      * @return
      *   The equivalent epoch time in milliseconds (`Long`).
      */
    def toEpochMilli(implicit zoneId: ZoneId): Long =
      ZonedDateTime.of(ldt, zoneId).toInstant.toEpochMilli
  }
}
