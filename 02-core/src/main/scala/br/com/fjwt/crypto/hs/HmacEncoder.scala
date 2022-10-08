package br.com.fjwt
package crypto
package hs

import br.com.fjwt.error.JWTError
import br.com.fjwt.validation.CodecValidation

import cats.MonadError
import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.toFunctorOps
import cats.syntax.all.toFlatMapOps

import org.apache.commons.codec.digest.HmacUtils

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import java.util.Formatter
import java.nio.charset.StandardCharsets

trait HmacEncoder[F[*]]:
  def alg: HmacEncoderAlgorithms
  def encode(privateKey: String)(str: String): F[String]

object HmacEncoder:
  def hs1Encoder[F[*]: [F[*]] =>> MonadError[F, JWTError]](using
      CodecValidation[F]
  ): HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HS1
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hs224Encoder[F[*]: [F[*]] =>> MonadError[F, JWTError]](using
      codecValidation: CodecValidation[F]
  ): HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HS224
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hs256Encoder[F[*]: [F[*]] =>> MonadError[F, JWTError]](using
      codecValidation: CodecValidation[F]
  ): HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HS256
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hs384Encoder[F[*]: [F[*]] =>> MonadError[F, JWTError]](using
      codecValidation: CodecValidation[F]
  ): HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HS384
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hs512Encoder[F[*]: [F[*]] =>> MonadError[F, JWTError]](using
      codecValidation: CodecValidation[F]
  ): HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HS512
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  def hMD5Encoder[F[*]: [F[*]] =>> MonadError[F, JWTError]](using
      codecValidation: CodecValidation[F]
  ): HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HMD5
      def encode(privateKey: String)(str: String): F[String] = hmacEncoder(alg)(privateKey)(str)

  private def hmacEncoder[F[*]: [F[*]] =>> MonadError[F, JWTError]](
      alg: HmacEncoderAlgorithms
  )(privateKey: String)(str: String)(using codecValidation: CodecValidation[F]) =
    for
      key <- codecValidation validatePrivateKey privateKey
      payload <- codecValidation validateToken str
    yield new HmacUtils(alg.alg, key).hmacHex(payload)
