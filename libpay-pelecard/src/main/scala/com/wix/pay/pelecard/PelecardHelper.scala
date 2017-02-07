package com.wix.pay.pelecard

import com.wix.pay.creditcard.CreditCard
import com.wix.pay.model.CurrencyAmount
import com.wix.pay.pelecard.model.Conversions._
import com.wix.pay.pelecard.model._

object PelecardHelper {
  def createDebitRegularTypeRequest(merchant: PelecardMerchant, creditCard: CreditCard,
                                    currencyAmount: CurrencyAmount): DebitRegularTypeRequest = {
    DebitRegularTypeRequest(
      terminalNumber = merchant.terminalNumber,
      user = merchant.user,
      password = merchant.password,
      shopNumber = merchant.shopNumber,
      creditCard = creditCard.number,
      creditCardDateMmYy = toPelecardYearMonth(
        year = creditCard.expiration.year,
        month = creditCard.expiration.month
      ),
      token = "",
      total = toPelecardAmount(currencyAmount.amount),
      currency = toPelecardCurrency(currencyAmount.currency),
      cvv2 = creditCard.csc.getOrElse(""),
      id = creditCard.holderId.getOrElse(""),
      authorizationNumber = "",
      paramX = ""
    )
  }

  def createDebitRegularTypeRequest(merchant: PelecardMerchant, authorization: PelecardAuthorization,
                                    amount: Double): DebitRegularTypeRequest = {
    DebitRegularTypeRequest(
      terminalNumber = merchant.terminalNumber,
      user = merchant.user,
      password = merchant.password,
      shopNumber = merchant.shopNumber,
      creditCard = "",
      creditCardDateMmYy = "",
      token = authorization.token,
      total = toPelecardAmount(amount),
      currency = authorization.currency,
      cvv2 = "",
      id = "",
      authorizationNumber = authorization.authorizationNumber,
      paramX = ""
    )
  }

  def createAuthorizeCreditCardRequest(merchant: PelecardMerchant, creditCard: CreditCard,
                                       currencyAmount: CurrencyAmount): AuthorizeCreditCardRequest = {
    AuthorizeCreditCardRequest(
      terminalNumber = merchant.terminalNumber,
      user = merchant.user,
      password = merchant.password,
      shopNumber = merchant.shopNumber,
      creditCard = creditCard.number,
      creditCardDateMmYy = toPelecardYearMonth(
        year = creditCard.expiration.year,
        month = creditCard.expiration.month
      ),
      token = "",
      total = toPelecardAmount(currencyAmount.amount),
      currency = toPelecardCurrency(currencyAmount.currency),
      cvv2 = creditCard.csc.getOrElse(""),
      id = creditCard.holderId.getOrElse(""),
      paramX = ""
    )
  }

  def createConvertToTokenRequest(merchant: PelecardMerchant, creditCard: CreditCard): ConvertToTokenRequest = {
    ConvertToTokenRequest(
      terminalNumber = merchant.terminalNumber,
      user = merchant.user,
      password = merchant.password,
      shopNumber = merchant.shopNumber,
      creditCard = creditCard.number,
      creditCardDateMmYy = toPelecardYearMonth(
        year = creditCard.expiration.year,
        month = creditCard.expiration.month
      ),
      addFourDigits = false.toString
    )
  }
}
