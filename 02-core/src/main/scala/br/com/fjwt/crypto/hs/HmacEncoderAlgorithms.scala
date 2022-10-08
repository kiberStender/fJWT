package br.com.fjwt
package crypto
package hs

enum HmacEncoderAlgorithms(val alg: String) {
  case HS1 extends HmacEncoderAlgorithms("HmacSHA1")
  case HS224 extends HmacEncoderAlgorithms("HmacSHA224")
  case HS256 extends HmacEncoderAlgorithms("HmacSHA256")
  case HS384 extends HmacEncoderAlgorithms("HmacSHA384")
  case HS512 extends HmacEncoderAlgorithms("HmacSHA512")
  case HMD5 extends HmacEncoderAlgorithms("HmacMD5")
}
