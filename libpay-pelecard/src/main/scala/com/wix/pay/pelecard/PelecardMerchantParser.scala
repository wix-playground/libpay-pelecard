package com.wix.pay.pelecard

trait PelecardMerchantParser {
  def parse(merchantKey: String): PelecardMerchant
  def stringify(merchant: PelecardMerchant): String
}
