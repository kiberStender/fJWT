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
  InvalidSignatureError,
  Not2TokenPartsError,
  Not3TokenPartsError
}
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm

import java.nio.charset.StandardCharsets
import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}
import scala.util.matching.Regex

package object fjwt:

  /** Helper method to extract data from a given JSON formatted String using Regex
    * @param pattern
    *   The regex pattern
    * @param json
    *   The JSON formatted String to have data extracted from
    * @tparam F
    *   The effect type
    * @return
    *   An Option[String]
    */
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

    /** A helper method to convert a given String into a Byte Array with UTF-8 characters
      * @return
      */
    private[fjwt] def toBytesUTF8: Array[Byte] = str getBytes StandardCharsets.UTF_8

    /** A helper method to find out if a given String is empty or null
      * @param nullCase
      *   The Exception to return in case it is null
      * @param emptyCase
      *   The Exception to return in case it is empty
      * @tparam F
      *   The effect type
      * @tparam N
      *   The type of the Null Exception
      * @tparam E
      *   The type of the Empty Exception
      * @return
      *   Either the String back or the given exception for the given case
      */
    private[fjwt] def isEmptyValue[F[*]: [F[*]] =>> ApplicativeError[
      F,
      Throwable
    ], N <: Throwable, E <: Throwable](nullCase: => N)(emptyCase: => E): F[String] =
      if str === null then nullCase.raiseError[F, String]
      else if str.isEmpty then emptyCase.raiseError[F, String]
      else str.pure[F]

    /** A convenience method to merge two JSON formatted Strings into a single JSON formatted String
      * @param strJson
      *   The String to be merged
      * @return
      *   A new JSON formatted String
      */
    private[fjwt] def merge(strJson: String): String =
      s"${str.stripSuffix("}")},${strJson.stripPrefix("{")}"

    /** A helper method to check if a given token has At Least two(2) parts eg: abcd.ab
      * @tparam F
      *   The effect type
      * @return
      *   Either a Tuple2 containing each part or [[Not2TokenPartsError]]
      */
    private[fjwt] def is2Parts[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]
        : F[(String, String)] =
      str split "\\." match
        case Array(header, payload, _*) => (header, payload).pure[F]
        case _                          => Not2TokenPartsError.raiseError[F, (String, String)]

    /** A helper method to check if a given token has At Least three(3) parts eg: abcd.ab.cd
      * @tparam F
      *   The effect type
      * @return
      *   Either a Tuple3 containing each part or [[Not3TokenPartsError]]
      */
    private[fjwt] def is3Parts[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]
        : F[(String, String, String)] =
      str split "\\." match
        case Array(header, payload, signature, _*) => (header, payload, signature).pure[F]
        case _ => Not3TokenPartsError.raiseError[F, (String, String, String)]

    /** A helper method to check if a given signature(the third part of the JWT) is valid
      * @param originalSignature
      *   The original signature that comes with the token
      * @tparam F
      *   The effect type
      * @return
      *   Either True or [[InvalidSignatureError]]
      */
    private[fjwt] def isValidSignature[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]](
        originalSignature: String
    ): F[Boolean] =
      if str === originalSignature then true.pure[F]
      else InvalidSignatureError.raiseError[F, Boolean]
  }

  extension (n: Long) {

    /** A helper method to convert a Long object to a [[LocalDateTime]]
      * @param zoneId
      *   The time zone of the user to properly convert the [[LocalDateTime]] to a Long object
      * @return
      *   A [[LocalDateTime]] instance
      */
    def toLocalDateTime(using zoneId: ZoneId): LocalDateTime =
      Instant.ofEpochMilli(n).atZone(zoneId).toLocalDateTime
  }

  extension (ldt: LocalDateTime) {

    /** A helper method to convert a [[LocalDateTime]] object to a Long
      * @param zoneId
      *   The time zone of the user to properly convert the [[LocalDateTime]] to a Long object
      * @return
      *   A Long instance
      */
    def toEpochMilli(using zoneId: ZoneId): Long =
      ZonedDateTime.of(ldt, zoneId).toInstant.toEpochMilli
  }

  extension (alg: HmacAlgorithm) {

    /** A syntax sugar function to make it easier to read a hmac hashing the token to produce the
      * signature
      * @param privateKey
      *   The key used to hash the string
      * @param str
      *   The string to be hashed
      * @tparam F
      *   The effect type
      * @return
      *   The hashed String
      */
    def hash[F[*]: Hmac](privateKey: String)(str: String): F[Array[Byte]] =
      implicitly[Hmac[F]].hash(alg)(privateKey)(str)
  }
