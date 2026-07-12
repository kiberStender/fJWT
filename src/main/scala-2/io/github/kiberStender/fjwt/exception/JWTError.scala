package io.github.kiberStender
package fjwt
package exception

/** Enum to delimit and map all possible errors that the API can "throw"
  */
sealed trait JWTError extends Throwable {
  def message: String

  override def toString: String = message
}

object JWTError {
  case object ExpiredToken extends JWTError {
    val message = "Token is expired"
  }

  case class NotMappedError(message: String) extends JWTError

  case object NullPrivateKey extends JWTError {
    val message = "Private key cannot null"
  }

  case object EmptyPrivateKey extends JWTError {
    val message = "Private key cannot be empty"
  }

  case object InvalidSignature extends JWTError {
    val message = "JWT Signature does not match"
  }

  case object NullToken extends JWTError {
    val message = "Token cannot null"
  }

  case object EmptyToken extends JWTError {
    val message = "Token cannot be empty"
  }

  case object Not2TokenParts extends JWTError {
    val message = "Token has less than 2 parts: header.payload"
  }
  case object Not3TokenParts extends JWTError {
    val message = "Token has less than 3 parts: header.payload.signature"
  }

  case class InvalidAlgException(alg: String) extends JWTError {
    val message = s"Provided alg is invalid or not supported: [$alg]"
  }
}
