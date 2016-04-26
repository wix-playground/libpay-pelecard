package com.wix.pay.pelecard

import com.wix.pay.pelecard.model.ConvertToTokenResponse
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class ConvertToTokenResponseParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): ConvertToTokenResponse = {
    Serialization.read[ConvertToTokenResponse](str)
  }

  def stringify(obj: ConvertToTokenResponse): String = {
    Serialization.write(obj)
  }

}
