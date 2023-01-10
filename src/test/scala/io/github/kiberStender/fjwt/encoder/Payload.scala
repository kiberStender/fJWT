package io.github.kiberStender
package fjwt
package encoder

import io.circe.Codec

private final case class Payload(name: String, admin: Boolean)

object Payload:
  given Codec[Payload] = io.circe.generic.semiauto.deriveCodec
