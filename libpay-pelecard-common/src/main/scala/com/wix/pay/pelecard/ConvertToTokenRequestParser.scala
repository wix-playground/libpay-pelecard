package com.wix.pay.pelecard

import com.wix.pay.pelecard.model.ConvertToTokenRequest
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class ConvertToTokenRequestParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): ConvertToTokenRequest = {
    Serialization.read[ConvertToTokenRequest](str)
  }

  def stringify(obj: ConvertToTokenRequest): String = {
    Serialization.write(obj)
  }
}
