package com.wix.pay.pelecard

/**
 * @param currency   Pelecard (not ISO) currency code.
 */
case class PelecardAuthorization(transactionId: String,
                                 token: String,
                                 authorizationNumber: String,
                                 currency: String)
