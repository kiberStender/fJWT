package io.github.kiberStender
package fjwt
package models
package crypto

/** Enum to narrow down the possible hmac algorithms
  */
enum HmacAlgorithm(val alg: String, val fullName: String):
  case HmacSHA1 extends HmacAlgorithm("HS1", "HmacSHA1")
  case HmacSHA224 extends HmacAlgorithm("HS224", "HmacSHA224")
  case HmacSHA256 extends HmacAlgorithm("HS256", "HmacSHA256")
  case HmacSHA384 extends HmacAlgorithm("HS384", "HmacSHA384")
  case HmacSHA512 extends HmacAlgorithm("HS512", "HmacSHA512")
