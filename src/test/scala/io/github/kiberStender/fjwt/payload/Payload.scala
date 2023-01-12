package io.github.kiberStender
package fjwt
package payload

import io.circe.Codec

final case class Payload(name: String, admin: Boolean)

object Payload:
  given Codec[Payload] = io.circe.generic.semiauto.deriveCodec
