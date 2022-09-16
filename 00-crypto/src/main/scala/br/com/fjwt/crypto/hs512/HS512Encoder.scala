package br.com.fjwt
package crypto
package hs512

import cats.*, cats.syntax.all.*
import org.apache.commons.codec.digest.HmacUtils
import org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_512

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import java.util.Formatter
import java.nio.charset.StandardCharsets

trait HS512Encoder[F[*]]:
  def encode(privateKey: String)(str: String): F[String]

object HS512Encoder:
  def dsl[F[*]: Applicative]: HS512Encoder[F] =
    new HS512Encoder:
      def encode(privateKey: String)(str: String): F[String] =
        new HmacUtils(HMAC_SHA_512, privateKey).hmacHex(str).pure[F]
