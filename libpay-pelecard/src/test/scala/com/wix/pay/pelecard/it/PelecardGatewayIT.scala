package com.wix.pay.pelecard.it


import com.google.api.client.http.javanet.NetHttpTransport
import com.wix.pay.creditcard.{CreditCard, CreditCardOptionalFields, YearMonth}
import com.wix.pay.model.CurrencyAmount
import com.wix.pay.pelecard.PelecardMatchers._
import com.wix.pay.pelecard.model._
import com.wix.pay.pelecard.testkit.PelecardDriver
import com.wix.pay.pelecard.{PelecardAuthorization, PelecardMerchant, _}
import com.wix.pay.shva.model.StatusCodes
import com.wix.pay.{PaymentGateway, PaymentRejectedException}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

import scala.concurrent.duration._


class PelecardGatewayIT extends SpecWithJUnit {
  val pelecardPort = 10010

  val requestFactory = new NetHttpTransport().createRequestFactory()
  val driver = new PelecardDriver(port = pelecardPort)
  step {
    driver.startProbe()
  }

  sequential

  trait Ctx extends Scope {
    val merchantParser = new JsonPelecardMerchantParser()
    val authorizationParser = new JsonPelecardAuthorizationParser()

    val someMerchant = PelecardMerchant(
      terminalNumber = "some terminal number",
      user = "some user",
      password = "some password",
      shopNumber = "some shop number"
    )
    val merchantKey = merchantParser.stringify(someMerchant)

    val someCurrencyAmount = CurrencyAmount("ILS", 33.3)
    val someAdditionalFields = CreditCardOptionalFields.withFields(
      csc = Some("123"),
      holderId = Some("some holder ID"))
    val someCreditCard = CreditCard(
      number = "4012888818888",
      expiration = YearMonth(2020, 12),
      additionalFields = Some(someAdditionalFields))

    private val helper = new PelecardHelper

    val someAuthorization = PelecardAuthorization(
      transactionId = "someTransactionId",
      token = "someToken",
      authorizationNumber = "someAuthorizationNumber",
      currency = anAuthorizeCreditCardRequest().currency
    )
    val authorizationKey = authorizationParser.stringify(someAuthorization)
    val someCaptureAmount = 11.1

    def aDebitRegularTypeRequest(): DebitRegularTypeRequest = {
      helper.createDebitRegularTypeRequest(someMerchant, someCreditCard, someCurrencyAmount)
    }

    def aDebitRegularTypeRequestPostAuthorize(): DebitRegularTypeRequest = {
      helper.createDebitRegularTypeRequest(someMerchant, someAuthorization, someCaptureAmount)
    }

    def anAuthorizeCreditCardRequest(): AuthorizeCreditCardRequest = {
      helper.createAuthorizeCreditCardRequest(someMerchant, someCreditCard, someCurrencyAmount)
    }

    def aConvertToTokenRequest(): ConvertToTokenRequest = {
      helper.createConvertToTokenRequest(someMerchant, someCreditCard)
    }

    private def aSuccessfulResultData(withAuthorizationNumber: Boolean = false,
                                      withToken: Boolean = false): ResultData = {
      ResultData(
        PelecardTransactionId = someAuthorization.transactionId,
        DebitApproveNumber = if (withAuthorizationNumber) someAuthorization.authorizationNumber else null,
        Token = if (withToken) someAuthorization.token else null
      )
    }

    def aSuccessfulDebitRegularTypeResponse(): DebitRegularTypeResponse = {
      DebitRegularTypeResponse(
        StatusCode = StatusCodes.success,
        ErrorMessage = "some success message",
        ResultData = aSuccessfulResultData()
      )
    }

    def aPaymentRejectedDebitRegularTypeResponse(): DebitRegularTypeResponse = {
      DebitRegularTypeResponse(
        StatusCode = StatusCodes.rejected,
        ErrorMessage = "some error message",
        ResultData = null
      )
    }

    def aSuccessfulAuthorizeCreditCardResponse(): AuthorizeCreditCardResponse = {
      AuthorizeCreditCardResponse(
        StatusCode = StatusCodes.success,
        ErrorMessage = "some success message",
        ResultData = aSuccessfulResultData(withAuthorizationNumber = true)
      )
    }

    def aPaymentRejectedAuthorizeResponse(): AuthorizeCreditCardResponse = {
      AuthorizeCreditCardResponse(
        StatusCode = StatusCodes.rejected,
        ErrorMessage = "some error message",
        ResultData = null
      )
    }

    def aSuccessfulConvertToTokenResponse(): ConvertToTokenResponse = {
      ConvertToTokenResponse(
        StatusCode = StatusCodes.success,
        ErrorMessage = "some success message",
        ResultData = aSuccessfulResultData(withToken = true)
      )
    }

    val pelecard: PaymentGateway = new PelecardGateway(
      requestFactory = requestFactory,
      connectTimeout = Some(5.seconds),
      readTimeout = Some(5.seconds),
      numberOfRetries = 1,
      endpointUrl = s"http://localhost:$pelecardPort/",
      merchantParser = merchantParser,
      authorizationParser = authorizationParser
    )

    driver.resetProbe()
  }

  "sale request via PeleCard gateway" should {
    "gracefully fail on rejected card" in new Ctx {
      driver.aDebitRegularTypeRequestFor(
        aDebitRegularTypeRequest()
      ) returns aPaymentRejectedDebitRegularTypeResponse()

      pelecard.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount
      ) must beAFailedTry(
        check = beAnInstanceOf[PaymentRejectedException]
      )
    }

    "successfully yield a transaction ID on valid request" in new Ctx {
      driver.aDebitRegularTypeRequestFor(
        aDebitRegularTypeRequest()
      ) returns aSuccessfulDebitRegularTypeResponse()

      pelecard.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount
      ) must beASuccessfulTry(
        check = ===(someAuthorization.transactionId)
      )
    }
  }

  "authorize request via PeleCard gateway" should {
    "gracefully fail on rejected card" in new Ctx {
      driver.aConvertToTokenRequestFor(
        aConvertToTokenRequest()
      ) returns aSuccessfulConvertToTokenResponse()

      driver.anAuthorizeCreditCardRequestFor(
        anAuthorizeCreditCardRequest()
      ) returns aPaymentRejectedAuthorizeResponse()

      pelecard.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount
      ) must beAFailedTry(
        check = beAnInstanceOf[PaymentRejectedException]
      )
    }

    "successfully yield an authorization key on valid request" in new Ctx {
      driver.aConvertToTokenRequestFor(
        aConvertToTokenRequest()
      ) returns aSuccessfulConvertToTokenResponse()

      driver.anAuthorizeCreditCardRequestFor(
        anAuthorizeCreditCardRequest()
      ) returns aSuccessfulAuthorizeCreditCardResponse()

      pelecard.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount
      ) must beASuccessfulTry(
        check = beAuthorizationKey(
          authorization = beAuthorization(
            transactionId = ===(someAuthorization.transactionId),
            authorizationNumber = ===(someAuthorization.authorizationNumber)
          )
        )
      )
    }
  }

  "capture request via PeleCard gateway" should {
    "successfully yield a transaction ID on valid request" in new Ctx {
      driver.aDebitRegularTypeRequestFor(
        aDebitRegularTypeRequestPostAuthorize()
      ) returns aSuccessfulDebitRegularTypeResponse()

      pelecard.capture(
        merchantKey = merchantKey,
        authorizationKey = authorizationKey,
        amount = someCaptureAmount
      ) must beASuccessfulTry(
        check = ===(someAuthorization.transactionId)
      )
    }
  }

  "voidAuthorization request via PeleCard gateway" should {
    "successfully yield a transaction ID on valid request" in new Ctx {
      pelecard.voidAuthorization(
        merchantKey = merchantKey,
        authorizationKey = authorizationKey
      ) must beASuccessfulTry(
        check = ===(someAuthorization.transactionId)
      )
    }
  }

  step {
    driver.stopProbe()
  }
}
