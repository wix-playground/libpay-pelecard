package com.wix.pay.pelecard

import org.specs2.matcher.{AlwaysMatcher, Matcher, Matchers}

trait PelecardMatchers extends Matchers {
  def authorizationParser: PelecardAuthorizationParser

  def beMerchant(terminalNumber: Matcher[String] = AlwaysMatcher(),
                 user: Matcher[String] = AlwaysMatcher(),
                 password: Matcher[String] = AlwaysMatcher(),
                 shopNumber: Matcher[String] = AlwaysMatcher()): Matcher[PelecardMerchant] = {
    terminalNumber ^^ { (_: PelecardMerchant).terminalNumber aka "terminal number" } and
      user ^^ { (_: PelecardMerchant).user aka "user" } and
      password ^^ { (_: PelecardMerchant).password aka "password" } and
      shopNumber ^^ { (_: PelecardMerchant).shopNumber aka "shopNumber" }
  }

  def beAuthorization(transactionId: Matcher[String] = AlwaysMatcher(),
                      token: Matcher[String] = AlwaysMatcher(),
                      authorizationNumber: Matcher[String] = AlwaysMatcher(),
                      currency: Matcher[String] = AlwaysMatcher()): Matcher[PelecardAuthorization] = {
    transactionId ^^ { (_: PelecardAuthorization).transactionId aka "transaction ID" } and
      token ^^ { (_: PelecardAuthorization).token aka "token" } and
      authorizationNumber ^^ { (_: PelecardAuthorization).authorizationNumber aka "authorization number" } and
      currency ^^ { (_: PelecardAuthorization).currency aka "currency" }
  }

  def beAuthorizationKey(authorization: Matcher[PelecardAuthorization]): Matcher[String] = {
    authorization ^^ { authorizationParser.parse(_: String) aka "parsed authorization"}
  }
}

object PelecardMatchers extends PelecardMatchers {
  override val authorizationParser = new JsonPelecardAuthorizationParser()
}