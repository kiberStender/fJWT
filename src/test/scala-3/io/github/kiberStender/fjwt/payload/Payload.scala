package io.github.kiberStender
package fjwt
package payload

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.circe.syntax.EncoderOps
import io.circe.{Codec, parser}
import io.github.kiberStender.fjwt.json.{JsonDecoder, JsonEncoder}

case class Payload(name: String, admin: Boolean)

object Payload:
  import io.circe.generic.semiauto.deriveCodec

  given Codec[Payload] = deriveCodec

  given pJsonEncoder[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: JsonEncoder[F, Payload] with
    def encode(json: Payload): F[String] = json.asJson.noSpaces.pure[F]

  given pJsondecoder[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: JsonDecoder[F, Payload] with
    def decode(json: String): F[Payload] = parser.decode[Payload](json) match
      case Left(value) => value.raiseError[F, Payload]
      case Right(value) => value.pure[F]