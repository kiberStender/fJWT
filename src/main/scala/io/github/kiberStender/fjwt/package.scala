package io.github.kiberStender

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, catsSyntaxEq}
import io.github.kiberStender.fjwt.error.JWTError.{InvalidSignature, Not2TokenParts, Not3TokenParts}

import java.nio.charset.StandardCharsets
import java.time.{LocalDateTime, ZoneId}

package object fjwt:
  extension (zoneId: ZoneId) {

    /** Method to simplify getting epoch time from a zone id
      *
      * @param ldt
      *   The [[LocalDateTime]] object to be have epoch time extracted
      * @return
      *   A [[Long]] value containing the date and time formatted in epoch
      */
    private[fjwt] def toEpoch(ldt: LocalDateTime): Long = ldt.atZone(zoneId).toInstant.toEpochMilli
  }

  extension (str: String) {

    /** Method to convert a given [[String]] into an Array of bytes based on UTF-8 charset
      * @return
      *   The [[Array]][[Byte]] of the respective [[String]]
      */
    private[fjwt] def toBytesUTF8: Array[Byte] = str getBytes StandardCharsets.UTF_8

    /** Method to validate the signature of a JWT token
      * @param originalSignature
      *   The original signature coming from the token tha needs to be validated
      * @tparam F
      *   The type of container to be used as return type
      * @return
      *   Either true or an [[InvalidSignature]] error
      */
    private[fjwt] def isValidSignature[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]](
        originalSignature: String
    ): F[Boolean] =
      if str === originalSignature then true.pure[F]
      else InvalidSignature.raiseError[F, Boolean]

    private[fjwt] def isEmptyValue[F[*]: [F[*]] =>> ApplicativeError[F, Throwable], E <: Throwable](
        nullCase: => E
    )(emptyCase: => E): F[String] =
      if str === null then nullCase.raiseError[F, String]
      else if str.isEmpty then emptyCase.raiseError[F, String]
      else str.pure[F]

    private[fjwt] def is2Parts[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]
        : F[(String, String)] =
      str split "\\." match
        case Array(header, payload, _) => (header, payload).pure[F]
        case _                         => Not2TokenParts.raiseError[F, (String, String)]

    private[fjwt] def is3Parts[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]
        : F[(String, String, String)] =
      str split "\\." match
        case Array(header, payload, signature) => (header, payload, signature).pure[F]
        case _ => Not3TokenParts.raiseError[F, (String, String, String)]
  }
