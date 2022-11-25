package io.github.kiberStender
package fjwt
package crypto
package base64

import cats.Applicative
import cats.syntax.all.{toFunctorOps, catsSyntaxApplicativeId}

import java.util.Base64

/**
 * A trait describing a {@link Base64Encoder}
 * @tparam F A given container that wraps the return type
 */
trait Base64Encoder[F[*]]:
  /**
   * A method to encode a given string using Base64 algorithm
   * @param str The String to be encoded in Base64 algorithm
   * @return A String encoded in Base64 algorithm wrapped in F
   */
  def encode(str: String): F[String]

/**
 * A object that provides method to instantiate a {@link Base64Encoder}
 */
object Base64Encoder:
  private lazy val encoder: Base64.Encoder = Base64.getEncoder

  /**
   * A method to instantiate a Base64Encoder
   * @tparam F An instance of {@link Applicative}
   * @return The instance of {@link Base64Encoder }
   */
  def dsl[F[*]: Applicative]: Base64Encoder[F] = (str: String) =>
    (encoder encodeToString str.getBytes()).pure[F]
