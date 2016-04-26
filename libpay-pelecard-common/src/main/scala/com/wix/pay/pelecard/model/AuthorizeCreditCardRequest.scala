package com.wix.pay.pelecard.model

case class AuthorizeCreditCardRequest(terminalNumber: String,
                                      user: String,
                                      password: String,
                                      shopNumber: String,
                                      creditCard: String,
                                      creditCardDateMmYy: String,
                                      token: String,
                                      total: String,
                                      currency: String,
                                      cvv2: String,
                                      id: String,
                                      paramX: String)
