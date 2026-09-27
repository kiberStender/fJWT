# F[JWT] - Functional JWT Library for Scala (2.13 & 3.3)

**F[JWT]** is a purely functional, effect-agnostic JSON Web Token library cross-compiled for **Scala 2.13** and **Scala 3.3.8**. Built on top of `cats`, it abstracts away the execution context, JSON serialization, and cryptographic operations through typeclasses (`F[_]: MonadError`). This allows you to integrate JWT encoding and decoding natively into your `cats.effect.IO`, `ZIO`, `Try`, or any other monadic context.

## Core Abstractions

The library is decoupled into several typeclasses:

* **`JsonEncoder` / `JsonDecoder**`: Extensible JSON parsing (e.g., Circe, Play-JSON).
* **`Base64Encoder` / `Base64Decoder` / `Hmac**`: Cryptographic abstractions.
* **`To`**: Type-safe conversion between domain time types (like `LocalDateTime`) and Unix Epoch `Long`.
* **`Expirable`**: Logic abstraction for validating token expiration.

---

## 1. Defining the Domain Models

To use the library, you must provide implementations for your `Header`, `Claim`, and `Payload`. Here is how to configure them using **Circe** for JSON serialization.

*Notice how Scala 3 leverages `derives Codec.AsObject` and the `given`/`using` syntax to vastly reduce boilerplate compared to Scala 2.13.*

### The Header

```scala
// --- Scala 2.13 ---
import cats.{Applicative, ApplicativeError}
import cats.syntax.all._
import io.circe.{Codec, parser}
import io.circe.generic.semiauto.deriveCodec
import io.circe.syntax._

case class SimpleHeader(alg: String, extraField: Int) extends Header

object SimpleHeader {
  private implicit val pCodec: Codec[SimpleHeader] = deriveCodec

  implicit def jsonDecoder[F[*]: ApplicativeError[*[*], Throwable]]: JsonDecoder[F, SimpleHeader] = (json: String) => parser.decode[SimpleHeader](json) match {
    case Left(value) => value.raiseError[F, SimpleHeader]
    case Right(value) => value.pure[F]
  }

  implicit def jsonEncoder[F[*]: Applicative]: JsonEncoder[F, SimpleHeader] = (json: SimpleHeader) => json.asJson.dropNullValues.noSpaces.pure[F]
}

```

```scala 3
// --- Scala 3.3.8 ---
import cats.{Applicative, ApplicativeError}
import cats.syntax.all.*
import io.circe.{Codec, parser}
import io.circe.syntax.*

case class SimpleHeader(alg: String, extraField: Int) extends Header derives Codec.AsObject

object SimpleHeader:
  given [F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: JsonDecoder[F, SimpleHeader] = 
    (json: String) => parser.decode[SimpleHeader](json) match
      case Left(err) => err.raiseError[F, SimpleHeader]
      case Right(value) => value.pure[F]

  given [F[_]: Applicative]: JsonEncoder[F, SimpleHeader] = 
    (json: SimpleHeader) => json.asJson.dropNullValues.noSpaces.pure[F]

```

### The Payload

```scala
// --- Scala 2.13 ---
case class Payload(name: String, admin: Boolean)

object Payload {
  private implicit val pCodec: Codec[Payload] = deriveCodec

  implicit def jsonDecoder[F[*]: ApplicativeError[*[*], Throwable]]: JsonDecoder[F, Payload] = (json: String) => parser.decode[Payload](json) match {
    case Left(value) => value.raiseError[F, Payload]
    case Right(value) => value.pure[F]
  }

  implicit def jsonEncoder[F[*]: Applicative]: JsonEncoder[F, Payload] = (json: Payload) => json.asJson.dropNullValues.noSpaces.pure[F]
}

```

```scala 3
// --- Scala 3.3.8 ---
case class Payload(name: String, admin: Boolean) derives Codec.AsObject

object Payload:
  given [F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: JsonDecoder[F, Payload] = 
    (json: String) => parser.decode[Payload](json) match
      case Left(err) => err.raiseError[F, Payload]
      case Right(value) => value.pure[F]

  given [F[*]: Applicative]: JsonEncoder[F, Payload] = 
    (json: Payload) => json.asJson.dropNullValues.noSpaces.pure[F]

```

---

## 2. Typeclass Implementations

### Claim & `To` Typeclass (Time Conversion)

The `To` typeclass seamlessly transforms time definitions (like `Long` to `LocalDateTime`) between standard domain logic and the JSON layer.

```scala
// --- Scala 2.13 ---
import java.time.{LocalDateTime, ZoneId, Instant}

case class SimpleClaim[T](iss: Option[String] = None, sub: Option[String] = None, aud: Option[String] = None, exp: Option[T] = None, nbf: Option[T] = None, iat: Option[T] = None,  jti: Option[String] = None, extraField: String) extends Claim[T]

object SimpleClaim {
  private implicit val pCodec: Codec[SimpleClaim[Long]] = deriveCodec

  implicit def jsonDecoder[F[*]: ApplicativeError[*[*], Throwable]]: JsonDecoder[F, SimpleClaim[Long]] = (json: String) => parser.decode[SimpleClaim[Long]](json) match {
    case Left(value) => value.raiseError[F, SimpleClaim[Long]]
    case Right(value) => value.pure[F]
  }

  implicit def jsonEncoder[F[*]: Applicative]: JsonEncoder[F, SimpleClaim[Long]] = (json: SimpleClaim[Long]) => json.asJson.dropNullValues.noSpaces.pure[F]

  implicit def fromLongToLocalDateTime(implicit zoneId: ZoneId): SimpleClaim[Long] To SimpleClaim[LocalDateTime] = (claim: SimpleClaim[Long]) => SimpleClaim[LocalDateTime](
    iss = claim.iss,
    sub = claim.sub,
    aud = claim.aud,
    exp = claim.exp.map(ms => LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), zoneId)),
    nbf = claim.nbf.map(ms => LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), zoneId)),
    iat = claim.iat.map(ms => LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), zoneId)),
    jti = claim.jti,
    extraField = claim.extraField
  )

  implicit def fromLocalDateTimeToLong(implicit zoneId: ZoneId): SimpleClaim[LocalDateTime] To SimpleClaim[Long] = (claim: SimpleClaim[LocalDateTime]) => SimpleClaim[Long](
    iss = claim.iss,
    sub = claim.sub,
    aud = claim.aud,
    exp = claim.exp.map(_.atZone(zoneId).toInstant.toEpochMilli),
    nbf = claim.nbf.map(_.atZone(zoneId).toInstant.toEpochMilli),
    iat = claim.iat.map(_.atZone(zoneId).toInstant.toEpochMilli),
    jti = claim.jti,
    extraField = claim.extraField
  )
}

```

```scala 3
// --- Scala 3.3.8 ---
import java.time.{LocalDateTime, ZoneId, Instant}

case class SimpleClaim[T](iss: Option[String] = None, sub: Option[String] = None, aud: Option[String] = None, exp: Option[T] = None, nbf: Option[T] = None, iat: Option[T] = None, jti: Option[String] = None, extraField: String) extends Claim[T]

object SimpleClaim:
  given Codec[SimpleClaim[Long]] = Codec.derived

  given [F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: JsonDecoder[F, SimpleClaim[Long]] = 
    (json: String) => parser.decode[SimpleClaim[Long]](json) match
      case Left(err) => err.raiseError[F, SimpleClaim[Long]]
      case Right(value) => value.pure[F]

  given [F[_]: Applicative]: JsonEncoder[F, SimpleClaim[Long]] = 
    (json: SimpleClaim[Long]) => json.asJson.dropNullValues.noSpaces.pure[F]

  given (using zoneId: ZoneId): To[SimpleClaim[Long], SimpleClaim[LocalDateTime]] = 
    (claim: SimpleClaim[Long]) => SimpleClaim[LocalDateTime](
      iss = claim.iss, sub = claim.sub, aud = claim.aud,
      exp = claim.exp.map(ms => LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), zoneId)),
      nbf = claim.nbf.map(ms => LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), zoneId)),
      iat = claim.iat.map(ms => LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), zoneId)),
      jti = claim.jti, extraField = claim.extraField
    )

  given (using zoneId: ZoneId): To[SimpleClaim[LocalDateTime], SimpleClaim[Long]] = 
    (claim: SimpleClaim[LocalDateTime]) => SimpleClaim[Long](
      iss = claim.iss, sub = claim.sub, aud = claim.aud,
      exp = claim.exp.map(_.atZone(zoneId).toInstant.toEpochMilli),
      nbf = claim.nbf.map(_.atZone(zoneId).toInstant.toEpochMilli),
      iat = claim.iat.map(_.atZone(zoneId).toInstant.toEpochMilli),
      jti = claim.jti, extraField = claim.extraField
    )

```

### `Expirable` Typeclass

Controls the logic to evaluate whether a token has expired based on your customized domain context.

```scala
// --- Scala 2.13 ---
import cats.ApplicativeError
import cats.syntax.all._

object ExpirationImplicits {
  implicit def expirableLocalDateTime[F[*]: ApplicativeError[*[*], Throwable]]: Expirable[F, LocalDateTime, SimpleClaim] =
    new Expirable[F, LocalDateTime, SimpleClaim] {
      def isExpired(claim: SimpleClaim[LocalDateTime]): F[SimpleClaim[LocalDateTime]] = {
        claim.exp match {
          case Some(expTime) if expTime.isBefore(LocalDateTime.now()) => 
            new RuntimeException("JWT Token has expired").raiseError[F, Boolean]
          case _ => claim.pure[F]
        }
      }
    }
}

```

```scala 3
// --- Scala 3.3.8 ---
import cats.ApplicativeError
import cats.syntax.all.*

object ExpirationImplicits:
  given [F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: Expirable[F, LocalDateTime, SimpleClaim] with
    def isExpired(claim: SimpleClaim[LocalDateTime]): F[SimpleClaim[LocalDateTime]] =
      claim.exp match
        case Some(expTime) if expTime.isBefore(LocalDateTime.now()) => 
          new RuntimeException("JWT Token has expired").raiseError[F, Boolean]
        case _ => claim.pure[F]

```

### Base64 and HMAC Cryptography (Apache Commons)

```scala
// --- Scala 2.13 ---
package io.github.kiberStender.fjwt.implicits.base64

import cats.{Applicative, ApplicativeError}
import cats.syntax.all._
import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.exception.JWTException.NotMappedException
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.HmacUtils

object Implicits {
  implicit def apacheCommonEncoder[F[*]: Applicative]: Base64Encoder[F] = new Base64Encoder[F] {
    def encode(data: Array[Byte]): F[String] = (Base64 encodeBase64String data).pure[F]
    def encodeURLSafe(data: Array[Byte]): F[String] = (Base64 encodeBase64URLSafeString data).pure[F]
  }
  
  implicit def apacheCommonDecoder[F[*]: ApplicativeError[*[*], Throwable]]: Base64Decoder[F] =
    (str: String) =>
      try { new String(Base64 decodeBase64 str).pure[F] } 
      catch { case iae: IllegalArgumentException => NotMappedException(iae.getMessage).raiseError[F, String] }
      
  implicit def hmacEncoderApacheCommons[F[*]: ApplicativeError[*[*], Throwable]]: Hmac[F] =
    new Hmac[F] {
      def hash(header: Header)(privateKey: String)(str: String): F[Array[Byte]] =
        try { extractAlg(header).map(new HmacUtils(_, privateKey).hmac(str)) } 
        catch { case error: Throwable => error.raiseError[F, Array[Byte]] }

      def extractAlg(header: Header): F[String] = header.alg match {
        case "HS1"   => "HmacSHA1".pure[F]
        case "HS224" => "HmacSHA224".pure[F]
        case "HS256" => "HmacSHA256".pure[F]
        case "HS384" => "HmacSHA384".pure[F]
        case "HS512" => "HmacSHA512".pure[F]
      }
    }
}

```

```scala 3
// --- Scala 3.3.8 ---
package io.github.kiberStender.fjwt.implicits.base64

import cats.{Applicative, ApplicativeError}
import cats.syntax.all.*
import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.exception.JWTException.NotMappedException
import io.github.kiberStender.fjwt.Header
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.HmacUtils

object Implicits:
  given apacheCommonEncoder[F[_]: Applicative]: Base64Encoder[F] with
    def encode(data: Array[Byte]): F[String] = Base64.encodeBase64String(data).pure[F]
    def encodeURLSafe(data: Array[Byte]): F[String] = Base64.encodeBase64URLSafeString(data).pure[F]
  
  given apacheCommonDecoder[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: Base64Decoder[F] =
    (str: String) =>
      try new String(Base64.decodeBase64(str)).pure[F]
      catch case iae: IllegalArgumentException => NotMappedException(iae.getMessage).raiseError[F, String]
      
  given hmacEncoderApacheCommons[F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: Hmac[F] with
    def hash(header: Header)(privateKey: String)(str: String): F[Array[Byte]] =
      try extractAlg(header).map(alg => new HmacUtils(alg, privateKey).hmac(str))
      catch case error: Throwable => error.raiseError[F, Array[Byte]]

    def extractAlg(header: Header): F[String] = header.alg match
      case "HS1"   => "HmacSHA1".pure[F]
      case "HS224" => "HmacSHA224".pure[F]
      case "HS256" => "HmacSHA256".pure[F]
      case "HS384" => "HmacSHA384".pure[F]
      case "HS512" => "HmacSHA512".pure[F]
      case _       => new IllegalArgumentException("Unsupported Alg").raiseError[F, String]

```

---

## 3. Usage Examples

Because FJWT uses standard Cats `MonadError`, the execution logic remains exactly the same in both Scala 2 and 3 once implicits/givens are in scope. The following examples evaluate using standard `scala.util.Try`.

### Setup Context and Inputs

```scala
import scala.util.Try
import java.time.ZoneId

// --- In Scala 2.13 ---
import cats.instances.try_._
import io.github.kiberStender.fjwt.implicits.base64.Implicits._
import ExpirationImplicits._
implicit val zoneId: ZoneId = ZoneId.of("UTC")

// --- In Scala 3.3.8 ---
// import cats.instances.try_.* 
// import io.github.kiberStender.fjwt.implicits.base64.Implicits.given
// import ExpirationImplicits.given
// given zoneId: ZoneId = ZoneId.of("UTC")

// Common Setup
type Token = JWToken[SimpleHeader, SimpleClaim[Long], Payload]
val key: String = "a-super-secret-key"
val expectedHeader = SimpleHeader("HS512", 0)

// Sample Data 1
val input1 = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJleHRyYUZpZWxkIjoiZXh0cmEiLCJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX0.RpfatL_4ldMevKdNTqdHE1mbRc8KwN1JH5RofDGDWzNCiE4QLd3kwFtArMbuKWtVfYsEe9cEdKHLNkD6jFbwJw"
val expectedPayload1: Payload = Payload("John Doe", admin = true)
val expectedClaim1: SimpleClaim[Long] = SimpleClaim(None, Some("1234567890"), None, None, None, Some(1516239022L), None, "extra")

// Sample Data 2 (Empty extraField)
val input2 = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJleHRyYUZpZWxkIjoiIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.BztTNHycpmcybI8YdGyEkicwVFYp-APH3RYszxMipf8IHTSS22Zj5LaYlF2-gFF0037ykPdcnvk2vjD9unxdag"
val expectedPayload2: Payload = Payload("John Doe", admin = true)
val expectedClaim2: SimpleClaim[Long] = SimpleClaim(None, Some("1234567890"), None, None, None, Some(1516239022L), None, "")

```

### Decoding Strategies

#### 1. Full Validation Parsing (Static Header)

Enforces signature checks and exact matches against the provided `expectedHeader`, as well as full lifecycle expiration evaluations.

```scala
val strictDecoder = AllValidations.dsl[Try, SimpleHeader, LocalDateTime, SimpleClaim, Payload](expectedHeader)

val decodedTry1: Try[JWToken[SimpleHeader, SimpleClaim[LocalDateTime], Payload]] =
  strictDecoder.decode(key)(input1)

decodedTry1.map(_.payload) // Success(Payload("John Doe", true))

```

#### 2. Parsing Without Expiration (Static Header)

Useful for authenticating valid signatures on Refresh Token operations where the token lifecycle explicitly requires ignoring expiration times.

```scala
val refreshDecoder = NoExpirationValidation.dsl[Try, SimpleHeader, LocalDateTime, SimpleClaim, Payload](expectedHeader)

val decodedTry2 = refreshDecoder.decode(key)(input2)
decodedTry2.map(_.claim.extraField) // Success("")

```

#### 3. Parse Without Validation

Bypasses the `Hmac` cryptographic operations entirely. Ideal for inspecting payloads without possessing the private key.

```scala
val unverifiedDecoder = NoValidation.dsl[Try, SimpleHeader, LocalDateTime, SimpleClaim, Payload]

// Re-using input1, avoiding a key
val unverifiedTry1 = unverifiedDecoder.decode("ignore-key-here")(input1)
unverifiedTry1.map(_.payload.name) // Success("John Doe")

```

#### 4. Full Validation Parsing (Dynamic Header)

Parses the header dynamically out of the Base64 representation to derive the appropriate Hmac algorithm configurations rather than using static verification upfront.

```scala
val dynamicDecoder = UseHeaderAllValidations.dsl[Try, SimpleHeader, LocalDateTime, SimpleClaim, Payload]

val dynamicDecodedTry = dynamicDecoder.decode(key)(input1)
dynamicDecodedTry.map(_.claim.sub) // Success(Some("1234567890"))

```

### Encoding Generation

Generating brand-new tokens invokes `AllValidations.dsl` for `JWTEncoder`:

```scala
val encoder = AllValidations.dsl[Try, SimpleHeader, LocalDateTime, SimpleClaim, Payload]

val newToken = JWToken(
  header = SimpleHeader("HS256", 1),
  claim = SimpleClaim[LocalDateTime](sub = Some("UUID-987"), extraField = "new-meta"),
  payload = Payload("Alice Smith", admin = false)
)

val encodedStringTry: Try[String] = encoder.encode(key)(newToken)

```

# Using the library

Maven repo changed servers for Open Source apps(like this one) and I did not see the email on time to do the migration(my bad), 
so this project will not be updated on maven repo, leaving people to only have the latest version (cited below) on maven repo. 

But I'm going to start new repos here on GitHub to publish this project once again on maven. I'm planning to split this in 2 projects: fjwt-core and fjwt-crypto

### FJWT-Core

This will have only `JWTEncoder`, `JWTDecoder` and`To[F, Claim[Long], Claim[Long]` implemented and every other dependency will be only traits, to keep the jar as minimal as possible.

### FJWT-Crypto

Will contain the implementation for `Base64Encoder`, `Base64Decoder` and `Hmac`  using Apache Commons codec, so people can add it if they want or as mentioned, use their own library to implemented them if they feel like

In order to use the old version of this library just add it to your build dependency list(Beware, this version only works with scala 3)
```scala
libraryDependencies += "io.github.kiberStender" %% "fjwt" % "1.0.3"
```

In case you want to use this version before I'm able to publish it on maven again(I will remove these observations once I publish it) feel free to checkout this repo and build it by yourself