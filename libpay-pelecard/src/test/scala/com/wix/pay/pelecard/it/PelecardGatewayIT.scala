package com.wix.pay.pelecard.it



import scala.concurrent.duration._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.wix.pay.creditcard.{CreditCard, CreditCardOptionalFields, YearMonth}
import com.wix.pay.model.{CurrencyAmount, Payment}
import com.wix.pay.pelecard.PelecardMatchers._
import com.wix.pay.pelecard.model.{ResultData, _}
import com.wix.pay.pelecard.testkit.PelecardDriver
import com.wix.pay.pelecard.{PelecardAuthorization, PelecardMerchant, _}
import com.wix.pay.shva.model.StatusCodes
import com.wix.pay.{PaymentGateway, PaymentRejectedException}


class PelecardGatewayIT extends SpecWithJUnit {
  val pelecardPort = 10010

  val requestFactory: HttpRequestFactory = new NetHttpTransport().createRequestFactory()
  val driver = new PelecardDriver(port = pelecardPort)

  val merchantParser = new JsonPelecardMerchantParser()
  val authorizationParser = new JsonPelecardAuthorizationParser()

  val someMerchant = PelecardMerchant(
    terminalNumber = "some terminal number",
    user = "some user",
    password = "some password",
    shopNumber = "some shop number")
  val merchantKey: String = merchantParser.stringify(someMerchant)

  val somePayment = Payment(currencyAmount = CurrencyAmount("ILS", 33.3))
  val someAdditionalFields: CreditCardOptionalFields = CreditCardOptionalFields.withFields(
    csc = Some("123"),
    holderId = Some("some holder ID"))
  val someCreditCard = CreditCard(
    number = "4012888818888",
    expiration = YearMonth(2020, 12),
    additionalFields = Some(someAdditionalFields))

  val someAuthorization = PelecardAuthorization(
    transactionId = "someTransactionId",
    token = "someToken",
    authorizationNumber = "someAuthorizationNumber",
    currency = anAuthorizeCreditCardRequest().currency)
  val authorizationKey: String = authorizationParser.stringify(someAuthorization)
  val someCaptureAmount = 11.1


  def aDebitRegularTypeRequest(): DebitRegularTypeRequest = {
    PelecardHelper.createDebitRegularTypeRequest(someMerchant, someCreditCard, somePayment.currencyAmount)
  }

  def aDebitRegularTypeRequestPostAuthorize(): DebitRegularTypeRequest = {
    PelecardHelper.createDebitRegularTypeRequest(someMerchant, someAuthorization, someCaptureAmount)
  }

  def anAuthorizeCreditCardRequest(): AuthorizeCreditCardRequest = {
    PelecardHelper.createAuthorizeCreditCardRequest(someMerchant, someCreditCard, somePayment.currencyAmount)
  }

  def aConvertToTokenRequest(): ConvertToTokenRequest = {
    PelecardHelper.createConvertToTokenRequest(someMerchant, someCreditCard)
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
      ResultData = aSuccessfulResultData())
  }

  def aPaymentRejectedDebitRegularTypeResponse(): DebitRegularTypeResponse = {
    DebitRegularTypeResponse(
      StatusCode = StatusCodes.rejected,
      ErrorMessage = "some error message",
      ResultData = null)
  }

  def aSuccessfulAuthorizeCreditCardResponse(): AuthorizeCreditCardResponse = {
    AuthorizeCreditCardResponse(
      StatusCode = StatusCodes.success,
      ErrorMessage = "some success message",
      ResultData = aSuccessfulResultData(withAuthorizationNumber = true))
  }

  def aPaymentRejectedAuthorizeResponse(): AuthorizeCreditCardResponse = {
    AuthorizeCreditCardResponse(
      StatusCode = StatusCodes.rejected,
      ErrorMessage = "some error message",
      ResultData = null)
  }

  def aSuccessfulConvertToTokenResponse(): ConvertToTokenResponse = {
    ConvertToTokenResponse(
      StatusCode = StatusCodes.success,
      ErrorMessage = "some success message",
      ResultData = aSuccessfulResultData(withToken = true))
  }

  val pelecard: PaymentGateway = new PelecardGateway(
    requestFactory = requestFactory,
    connectTimeout = Some(5.seconds),
    readTimeout = Some(5.seconds),
    numberOfRetries = 1,
    endpointUrl = s"http://localhost:$pelecardPort/",
    merchantParser = merchantParser,
    authorizationParser = authorizationParser)


  step {
    driver.start()
  }


  sequential


  trait Ctx extends Scope {
    driver.reset()
  }


  "sale request via PeleCard gateway" should {
    "gracefully fail on rejected card" in new Ctx {
      driver.aDebitRegularTypeRequestFor(aDebitRegularTypeRequest()) returns aPaymentRejectedDebitRegularTypeResponse()

      pelecard.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment) must beAFailedTry(check = beAnInstanceOf[PaymentRejectedException])
    }

    "successfully yield a transaction ID on valid request" in new Ctx {
      driver.aDebitRegularTypeRequestFor(aDebitRegularTypeRequest()) returns aSuccessfulDebitRegularTypeResponse()

      pelecard.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment) must beASuccessfulTry(check = ===(someAuthorization.transactionId))
    }
  }


  "authorize request via PeleCard gateway" should {
    "gracefully fail on rejected card" in new Ctx {
      driver.aConvertToTokenRequestFor(aConvertToTokenRequest()) returns aSuccessfulConvertToTokenResponse()

      driver.anAuthorizeCreditCardRequestFor(
        anAuthorizeCreditCardRequest()) returns aPaymentRejectedAuthorizeResponse()

      pelecard.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment) must beAFailedTry(check = beAnInstanceOf[PaymentRejectedException])
    }


    "successfully yield an authorization key on valid request" in new Ctx {
      driver.aConvertToTokenRequestFor(aConvertToTokenRequest()) returns aSuccessfulConvertToTokenResponse()

      driver.anAuthorizeCreditCardRequestFor(anAuthorizeCreditCardRequest()) returns
        aSuccessfulAuthorizeCreditCardResponse()

      pelecard.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment) must beASuccessfulTry(
          check = beAuthorizationKey(
            authorization = beAuthorization(
              transactionId = ===(someAuthorization.transactionId),
              authorizationNumber = ===(someAuthorization.authorizationNumber))))
    }
  }


  "capture request via PeleCard gateway" should {
    "successfully yield a transaction ID on valid request" in new Ctx {
      driver.aDebitRegularTypeRequestFor(aDebitRegularTypeRequestPostAuthorize()) returns
        aSuccessfulDebitRegularTypeResponse()

      pelecard.capture(
        merchantKey = merchantKey,
        authorizationKey = authorizationKey,
        amount = someCaptureAmount) must beASuccessfulTry(check = ===(someAuthorization.transactionId))
    }
  }

  "voidAuthorization request via PeleCard gateway" should {
    "successfully yield a transaction ID on valid request" in new Ctx {
      pelecard.voidAuthorization(
        merchantKey = merchantKey,
        authorizationKey = authorizationKey) must beASuccessfulTry(check = ===(someAuthorization.transactionId))
    }
  }


  step {
    driver.stop()
  }
}
