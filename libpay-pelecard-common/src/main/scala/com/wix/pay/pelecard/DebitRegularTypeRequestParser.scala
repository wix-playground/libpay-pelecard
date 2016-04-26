package com.wix.pay.pelecard

import com.wix.pay.pelecard.model.DebitRegularTypeRequest
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class DebitRegularTypeRequestParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): DebitRegularTypeRequest = {
    Serialization.read[DebitRegularTypeRequest](str)
  }

  def stringify(obj: DebitRegularTypeRequest): String = {
    Serialization.write(obj)
  }
}
