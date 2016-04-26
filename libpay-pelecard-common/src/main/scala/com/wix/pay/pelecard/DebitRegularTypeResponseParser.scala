package com.wix.pay.pelecard

import com.wix.pay.pelecard.model.DebitRegularTypeResponse
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class DebitRegularTypeResponseParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): DebitRegularTypeResponse = {
    Serialization.read[DebitRegularTypeResponse](str)
  }

  def stringify(obj: DebitRegularTypeResponse): String = {
    Serialization.write(obj)
  }
}
