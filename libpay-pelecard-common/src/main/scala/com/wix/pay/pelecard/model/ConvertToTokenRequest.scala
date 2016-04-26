package com.wix.pay.pelecard.model

case class ConvertToTokenRequest(terminalNumber: String,
                                 user: String,
                                 password: String,
                                 shopNumber: String,
                                 creditCard: String,
                                 creditCardDateMmYy: String,
                                 addFourDigits: String)
