package com.wix.pay.pelecard

import com.wix.pay.pelecard.model.AuthorizeCreditCardRequest
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class AuthorizeCreditCardRequestParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): AuthorizeCreditCardRequest = {
    Serialization.read[AuthorizeCreditCardRequest](str)
  }

  def stringify(obj: AuthorizeCreditCardRequest): String = {
    Serialization.write(obj)
  }
}
