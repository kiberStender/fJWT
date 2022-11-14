package io.github.kiberStender
package fjwt
package crypto
package hs

import cats.syntax.all.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import cats.Applicative

import io.github.kiberStender.fjwt.error.JWTError
import org.apache.commons.codec.digest.HmacUtils

trait HmacEncoder[F[*]]:
  def alg: HmacEncoderAlgorithms
  def encode(privateKey: String)(payload: String): F[String]

object HmacEncoder:
  def hs1Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacSHA1
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hs224Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacSHA224
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hs256Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacSHA256
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hs384Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacSHA384
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hs512Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacSHA512
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hMD5Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HmacMD5
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hsEncoder[F[*]: Applicative](
      algorithms: HmacEncoderAlgorithms
  ): HmacEncoder[F] =
    new HmacEncoder[F]:
      val alg: HmacEncoderAlgorithms = algorithms
      def encode(privateKey: String)(payload: String): F[String] =
        hmacEncoder(alg)(privateKey)(payload)

  private def hmacEncoder[F[*]: Applicative](
      alg: HmacEncoderAlgorithms
  )(privateKey: String)(str: String): F[String] =
    new HmacUtils(alg.alg, privateKey).hmacHex(str).pure[F]
