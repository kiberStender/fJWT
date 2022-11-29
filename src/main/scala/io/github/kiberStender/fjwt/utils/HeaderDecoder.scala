package io.github.kiberStender
package fjwt
package utils

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, toFunctorOps}
import io.circe.parser
import io.github.kiberStender.fjwt.crypto.hs.{HmacEncoder, HmacEncoderAlgorithms}

/** A typeclass to decode a string into an object of type {@link Alg} that represents the header of
  * the JWT
  * @tparam F
  *   The container to wrap the return methods
  */
trait HeaderDecoder[F[*]]:
  /** The method to decode a {@link String} into the {@link Alg} object
    * @param headerStr
    *   The {@link String} json representation to be decoded into an {@link Alg} object
    * @return
    *   Either The {@link Alg} object or an error wrapped in F
    */
  def decode(headerStr: String): F[HmacEncoder[F]]

/** Object factory for {@link HeaderDecoder}[F]
  */
object HeaderDecoder:
  /** Method to instantiate an {@link HeaderDecoder}[F]
    * @tparam F
    *   An {@link ApplicativeError}[F, Throwable] instance
    * @return
    *   An instance of {@link HeaderDecoder}[F]
    */
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: HeaderDecoder[F] = (headerStr: String) => parser
    .decode[Alg](headerStr)
    .map(_.alg)
    .fold(_.raiseError[F, HmacEncoderAlgorithms], _.pure[F])
    .map(HmacEncoder.hsEncoder)
