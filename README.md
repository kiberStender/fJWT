# fJWT

Simple scala 3 library to generate a validate JWT written in Tagless final style

For a given hypotetical class Payload that's how one encode and decode it

# Encoding example

```scala
import cats.*, cats.syntax.all.*
import io.circe.*

import java.time.ZoneId
import java.time.Instant

import br.com.fjwt.crypto.base64.encode.Base64Encoder
import br.com.fjwt.crypto.hs512.HS512Encoder
import br.com.fjwt.encode.JWTEncoder

type F = [T] =>> T
lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl
lazy val hs512Encoder: HS512Encoder[F] = HS512Encoder.dsl
lazy val encoder: JWTEncoder[F] = JWTEncoder.dsl(base64Encoder, hs512Encoder)
given zoneId: ZoneId = ZoneId.of("UTC")

final case class Payload(name: String, admin: Boolean)

given Encoder[Payload] = io.circe.generic.semiauto.deriveCodec

val payload: Payload = Payload("John Doe", true)
val key: String = "a-super-secret-key"
val sub: String = "1234567890".some
val iatEpochMilli: Long = 1516239022
val expEpochMilli: Long = 1516239150
val iat: Option[LocalDateTime] = Some(Instant.ofEpochMilli(iatEpochMilli).atZone(zoneId).toLocalDateTime())
val exp: Option[LocalDateTime] = Some(Instant.ofEpochMilli(expEpochMilli).atZone(zoneId).toLocalDateTime())

val encoded = encoder.encode(key)(sub = sub, exp = exp, iat = iat)(payload)

```

# Decoding example

```scala
import cats.*, cats.syntax.all.*
import io.circe.*

import br.com.fjwt.crypto.base64.decode.Base64Decoder
import br.com.fjwt.crypto.hs512.HS512Encoder
import br.com.fjwt.decode.JWTDecoder

type F = [T] =>> Either[Throwable, T]
lazy val base64Encoder: Base64Decoder[F] = Base64Decoder.dsl
lazy val hs512Encoder: HS512Encoder[F] = HS512Encoder.dsl
lazy val decoder: JWTDecoder[F] = JWTDecoder.dsl(base64Encoder, hs512Encoder)
given zoneId: ZoneId = ZoneId.of("UTC")

final case class Payload(name: String, admin: Boolean)

given Decoder[Payload] = io.circe.generic.semiauto.deriveCodec

val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae6"
val key = "a-super-secret-key"

val decoded = decoder.decode(key)(input)

```

### Giving that when you are encoding some data no problem may occur for it is just a string being encoded no special type is necessary as long as it implements Monad[F], but when decoding there are four possible errors that may happen

**Token does not contax exact 3 parts** `The token must have 3 parts header.payload.signature. If there is more or less than 3 it is considered invalid`
**Invalid Signature** `It may be a fraud so the signature may not match`
**Expired Token** `The token might have an exp field indicating when it will expire and at the time you are decoding it might be already expired`
**Payload Decoding failure** `During the parsing of the payload some error may occur like bad payload format`

### For these reasons the JWTDecoder has a special type F[*] =>> MonadError[F, Throwable]
