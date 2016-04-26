package com.wix.pay.pelecard.model

case class DebitRegularTypeRequest(terminalNumber: String,
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
                                   authorizationNumber: String,
                                   paramX: String)
