package com.wix.pay.pelecard


import com.wix.pay.pelecard.PelecardMatchers._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class JsonPelecardAuthorizationParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val authorizationParser: PelecardAuthorizationParser = new JsonPelecardAuthorizationParser
  }

  "stringify and then parse" should {
    "yield an authorization similar to the original one" in new Ctx {
      val someAuthorization = PelecardAuthorization(
        transactionId = "some transaction ID",
        token = "some token",
        authorizationNumber = "some authorization number",
        currency = "some currency"
      )

      val authorizationKey = authorizationParser.stringify(someAuthorization)
      authorizationParser.parse(authorizationKey) must beAuthorization(
        transactionId = ===(someAuthorization.transactionId),
        token = ===(someAuthorization.token),
        authorizationNumber = ===(someAuthorization.authorizationNumber),
        currency = ===(someAuthorization.currency)
      )
    }
  }
}
