package io.github.kiberStender
package fjwt
package exception

/** Enum to delimit and map all possible errors that the API can "throw"
  */
sealed trait JWTError extends Throwable {
  def message: String
}

object JWTError {
  case object ExpiredTokenError extends JWTError {
    val message = "Token is expired"
  }

  case class NotMappedError(message: String) extends JWTError

  case object NullPrivateKeyError extends JWTError {
    val message = "Private key cannot null"
  }

  case object EmptyPrivateKeyError extends JWTError {
    val message = "Private key cannot be empty"
  }

  case object InvalidSignatureError extends JWTError {
    val message = "JWT Signature does not match"
  }

  case object NullTokenError extends JWTError {
    val message = "Token cannot null"
  }

  case object EmptyTokenError extends JWTError {
    val message = "Token cannot be empty"
  }

  case object Not2TokenPartsError extends JWTError {
    val message = "Token has less than 2 parts: header.payload"
  }
  case object Not3TokenPartsError extends JWTError {
    val message = "Token has less than 3 parts: header.payload.signature"
  }

  case class InvalidAlgError(alg: String) extends JWTError {
    val message = s"Provided alg is invalid or not supported: [$alg]"
  }
}
