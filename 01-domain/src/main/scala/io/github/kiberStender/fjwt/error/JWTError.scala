package io.github.kiberStender
package fjwt
package error

enum JWTError(val message: String):
  case NotMappedError(errorMessage: String) extends JWTError(errorMessage)
  case ExpiredToken extends JWTError("Token is expired")
  case InvalidSignature extends JWTError("JWT Signature does not match")
  case NullPrivateKey extends JWTError("Private key cannot null")
  case EmptyPrivateKey extends JWTError("Private key cannot be empty")
  case NullToken extends JWTError("Token cannot null")
  case EmptyToken extends JWTError("Token cannot be empty")
  case WrongTokenParts extends JWTError("Token has to has 3 parts: header.payload.signature")
