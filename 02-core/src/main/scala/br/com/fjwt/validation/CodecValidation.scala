package br.com.fjwt
package validation

import br.com.fjwt.error.JWTError
import br.com.fjwt.error.JWTError.NullPrivateKey
import br.com.fjwt.error.JWTError.EmptyPrivateKey
import br.com.fjwt.error.JWTError.NullToken
import br.com.fjwt.error.JWTError.EmptyToken

import cats.ApplicativeError
import cats.syntax.all.catsSyntaxApplicativeErrorId
import cats.syntax.all.catsSyntaxApplicativeId

trait CodecValidation[F[*]]:
  def validatePrivateKey(privateKey: String): F[String]
  def validateToken(token: String): F[String]

object CodecValidation:
  def dsl[F[*]: [F[*]] =>> ApplicativeError[F, JWTError]]: CodecValidation[F] =
    new CodecValidation[F]:
      def validatePrivateKey(privateKey: String): F[String] =
        if privateKey == null then NullPrivateKey.raiseError[F, String]
        else if privateKey.isEmpty then EmptyPrivateKey.raiseError[F, String]
        else privateKey.pure[F]

      def validateToken(token: String): F[String] =
        if token == null then NullToken.raiseError[F, String]
        else if token.isEmpty then EmptyToken.raiseError[F, String]
        else token.pure[F]
