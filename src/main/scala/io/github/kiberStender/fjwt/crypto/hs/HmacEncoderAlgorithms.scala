package io.github.kiberStender
package fjwt
package crypto
package hs

import io.circe.{Decoder, DecodingFailure, HCursor}

/**
 * Enum to narrow down the possible hmac algorithms
 */
enum HmacEncoderAlgorithms(val alg: String):
  case HmacSHA1 extends HmacEncoderAlgorithms("HmacSHA1")
  case HmacSHA224 extends HmacEncoderAlgorithms("HmacSHA224")
  case HmacSHA256 extends HmacEncoderAlgorithms("HmacSHA256")
  case HmacSHA384 extends HmacEncoderAlgorithms("HmacSHA384")
  case HmacSHA512 extends HmacEncoderAlgorithms("HmacSHA512")
  case HmacMD5 extends HmacEncoderAlgorithms("HmacMD5")

/**
 * Implicit converters for circe
 */
object HmacEncoderAlgorithms:
  given Decoder[HmacEncoderAlgorithms] = (hCursor: HCursor) =>
    for
      value <- hCursor.as[String]
      result <- value match
        case "HmacSHA1"   => Right(HmacSHA1)
        case "HmacSHA224" => Right(HmacSHA224)
        case "HmacSHA256" => Right(HmacSHA256)
        case "HmacSHA384" => Right(HmacSHA384)
        case "HmacSHA512" => Right(HmacSHA512)
        case "HmacMD5"    => Right(HmacMD5)
        case s            => Left(DecodingFailure(s"Invalid algorithm $s", hCursor.history))
    yield result
