package com.wix.pay.pelecard

import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class JsonPelecardMerchantParser() extends PelecardMerchantParser {
  private implicit val formats = DefaultFormats

  override def parse(merchantKey: String): PelecardMerchant = {
    Serialization.read[PelecardMerchant](merchantKey)
  }

  override def stringify(merchant: PelecardMerchant): String = {
    Serialization.write(merchant)
  }
}
