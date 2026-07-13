package io.github.kiberStender
package fjwt
package exception

/** Enum to delimit and map all possible errors that the API can "throw"
  */
enum JWTError(val message: String) extends Throwable(message):
  case NotMappedError(errorMessage: String) extends JWTError(errorMessage)
  case ExpiredTokenError extends JWTError("Token is expired")
  case InvalidSignatureError extends JWTError("JWT Signature does not match")
  case NullPrivateKeyError extends JWTError("Private key cannot null")
  case EmptyPrivateKeyError extends JWTError("Private key cannot be empty")
  case NullTokenError extends JWTError("Token cannot null")
  case EmptyTokenError extends JWTError("Token cannot be empty")
  case Not2TokenPartsError extends JWTError("Token has less than 2 parts: header.payload")
  case Not3TokenPartsError extends JWTError("Token has less than 3 parts: header.payload.signature")
  case InvalidAlgError(alg: String)
      extends JWTError(s"Provided alg is invalid or not supported: [$alg]")
