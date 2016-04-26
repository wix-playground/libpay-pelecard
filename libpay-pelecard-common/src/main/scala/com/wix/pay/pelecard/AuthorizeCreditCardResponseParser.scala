package com.wix.pay.pelecard

import com.wix.pay.pelecard.model.AuthorizeCreditCardResponse
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class AuthorizeCreditCardResponseParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): AuthorizeCreditCardResponse = {
    Serialization.read[AuthorizeCreditCardResponse](str)
  }

  def stringify(obj: AuthorizeCreditCardResponse): String = {
    Serialization.write(obj)
  }

}
