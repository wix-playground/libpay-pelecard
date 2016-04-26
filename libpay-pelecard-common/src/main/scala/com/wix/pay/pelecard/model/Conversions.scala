package com.wix.pay.pelecard.model

import java.math.{BigDecimal => JBigDecimal}

object Conversions {
  def toPelecardAmount(amount: Double): String = {
    JBigDecimal.valueOf(amount).movePointRight(2).toString
  }

  def toPelecardYearMonth(year: Int, month: Int): String = {
    f"$month%02d${year % 100}%02d"
  }

  def toPelecardCurrency(currency: String): String = {
    currency match {
      case "ILS" => Currencies.ILS
      case "USD" => Currencies.USD
    }
  }

  def toPelecardBoolean(b: Boolean): String = {
    if (b) "true" else "false"
  }
}
