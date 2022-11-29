package io.github.kiberStender
package fjwt
package utils

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.circe.{Codec, parser}

import io.github.kiberStender.fjwt.error.JWTError.NotMappedError

/** A typeclass to decode a String into a {@link Claim}[P] object
  * @tparam F
  *   The container to wrap the return methods
  */
trait ClaimDecoder[F[*]]:
  /** Method to decode a given {@link String} into an an object of type {@link Claim}[P] when
    * providing its {@link Codec} instance
    * @param payload
    *   The string to be decoded into the object of type {@link Claim}[P]
    * @tparam P
    *   The type of the object that payload is serialized
    * @return
    *   Either the {@link Claim}[P] instance or an error thrown during circe decoding
    */
  def decode[P: Codec](payload: String): F[Claim[P]]

/** Instance factory for {@link ClaimDecoder}[F]
  */
object ClaimDecoder:
  /** Method to instantiate a {@link ClaimDecoder]}[F]
    * @tparam F
    *   An instance of {@link ApplicativeError}[F, Throwable]
    * @return
    *   An instance of {@link ClaimDecoder]}[F]
    */
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: ClaimDecoder[F] = new ClaimDecoder[F]:
    override def decode[P: Codec](payload: String): F[Claim[P]] =
      parser
        .decode[Claim[P]](payload)
        .fold(throwable => NotMappedError(throwable.getMessage).raiseError[F, Claim[P]], _.pure[F])
