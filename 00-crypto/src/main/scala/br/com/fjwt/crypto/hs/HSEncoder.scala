package br.com.fjwt
package crypto
package hs

import cats.*, cats.syntax.all.*
import org.apache.commons.codec.digest.HmacUtils

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import java.util.Formatter
import java.nio.charset.StandardCharsets

trait HmacEncoder[F[*]]:
  def alg: HmacEncoderAlgorithms
  def encode(privateKey: String)(str: String): F[String]

object HmacEncoder:
  def hs1Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HS1
      def encode(privateKey: String)(str: String): F[String] =
        new HmacUtils(alg.alg, privateKey).hmacHex(str).pure[F]

  def hs224Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HS224
      def encode(privateKey: String)(str: String): F[String] =
        new HmacUtils(alg.alg, privateKey).hmacHex(str).pure[F]

  def hs256Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HS256
      def encode(privateKey: String)(str: String): F[String] =
        new HmacUtils(alg.alg, privateKey).hmacHex(str).pure[F]

  def hs384Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HS384
      def encode(privateKey: String)(str: String): F[String] =
        new HmacUtils(alg.alg, privateKey).hmacHex(str).pure[F]        

  def hs512Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HS512
      def encode(privateKey: String)(str: String): F[String] =
        new HmacUtils(alg.alg, privateKey).hmacHex(str).pure[F]
 
  def hMD5Encoder[F[*]: Applicative]: HmacEncoder[F] =
    new HmacEncoder:
      val alg: HmacEncoderAlgorithms = HmacEncoderAlgorithms.HMD5
      def encode(privateKey: String)(str: String): F[String] =
        new HmacUtils(alg.alg, privateKey).hmacHex(str).pure[F]

