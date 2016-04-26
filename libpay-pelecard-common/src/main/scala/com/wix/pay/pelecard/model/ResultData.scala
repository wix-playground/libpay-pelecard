package com.wix.pay.pelecard.model

case class ResultData(PelecardTransactionId: String = "",
                      Token: String = "",
                      VoucherId: String = "",
                      ShvaResult: String = "",
                      ShvaFileNumber: String = "",
                      StationNumber: String = "",
                      Reciept: String = "",
                      JParam: String = "",
                      CreditCardNumber: String = "",
                      CreditCardExpDate: String = "",
                      CreditCardCompanyClearer: String = "",
                      CreditCardCompanyIssuer: String = "",
                      CreditCardStarsDiscountTotal: String = "",
                      CreditType: String = "",
                      CreditCardAbroadCard: String = "",
                      DebitType: String = "",
                      DebitCode: String = "",
                      DebitTotal: String = "",
                      DebitApproveNumber: String = "",
                      DebitCurrency: String = "",
                      TotalPayments: String = "",
                      FirstPaymentTotal: String = "",
                      FixedPaymentTotal: String = "",
                      AdditionalDetailsParamX: String = "",
                      shvaOutput: String = "",
                      CardHebName: String = "",
                      CreditCardBrand: String = "",
                      ApprovedBy: String = "",
                      CallReason: String = "")