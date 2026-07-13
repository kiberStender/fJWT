package io.github.kiberStender
package fjwt
package payload

import cats.ApplicativeError
import cats.syntax.all.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId}
import io.circe.{Codec, parser}
import io.circe.syntax.EncoderOps
import io.github.kiberStender.fjwt.json.{JsonDecoder, JsonEncoder}

case class Payload(name: String, admin: Boolean)

object Payload {
  import io.circe.generic.semiauto.deriveCodec

  private implicit val pCodec: Codec[Payload] = deriveCodec

  implicit def pJsonEncoder[F[*]: ApplicativeError[*[_], Throwable]]: JsonEncoder[F, Payload] =
    (json: Payload) => json.asJson.noSpaces.pure[F]

  implicit def pJsondecoder[F[*] : ApplicativeError[*[_], Throwable]]: JsonDecoder[F, Payload] =
    (json: String) => parser.decode[Payload](json) match {
      case Left(value) => value.raiseError[F, Payload]
      case Right(value) => value.pure[F]
    }
}