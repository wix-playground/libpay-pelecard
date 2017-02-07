package com.wix.pay.pelecard


import com.google.api.client.http._
import com.wix.pay.creditcard.CreditCard
import com.wix.pay.model._
import com.wix.pay.shva.model.{IsShvaRejectedStatusCode, StatusCodes}
import com.wix.pay.{PaymentErrorException, PaymentException, PaymentGateway, PaymentRejectedException}

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}


object Endpoints {
  val production = "https://gateway20.pelecard.biz/services/"
}

class PelecardGateway(requestFactory: HttpRequestFactory,
                      connectTimeout: Option[Duration] = None,
                      readTimeout: Option[Duration] = None,
                      numberOfRetries: Int = 0,
                      endpointUrl: String = Endpoints.production,
                      merchantParser: PelecardMerchantParser = new JsonPelecardMerchantParser,
                      authorizationParser: PelecardAuthorizationParser = new JsonPelecardAuthorizationParser) extends PaymentGateway {
  private val pelecard = new PelecardHttpClient(
    requestFactory = requestFactory,
    connectTimeout = connectTimeout,
    readTimeout = readTimeout,
    numberOfRetries = numberOfRetries,
    endpointUrl = endpointUrl
  )

  override def authorize(merchantKey: String, creditCard: CreditCard, payment: Payment, customer: Option[Customer], deal: Option[Deal]): Try[String] = {
    Try {
      require(payment.installments == 1, "Installments are not implemented")

      val merchant = merchantParser.parse(merchantKey)

      val authorization = authorize(merchant, creditCard, payment)

      authorizationParser.stringify(authorization)
    } match {
      case Success(authorizationKey) => Success(authorizationKey)
      case Failure(e: PaymentException) => Failure(e)
      case Failure(e) => Failure(PaymentErrorException(e.getMessage, e))
    }
  }

  override def capture(merchantKey: String, authorizationKey: String, amount: Double): Try[String] = {
    Try {
      val merchant = merchantParser.parse(merchantKey)
      val authorization = authorizationParser.parse(authorizationKey)

      val request = PelecardHelper.createDebitRegularTypeRequest(merchant, authorization, amount)
      val response = pelecard.debitRegularType(request)

      verifyShvaStatusCode(response.StatusCode, response.ErrorMessage)

      response.ResultData.PelecardTransactionId
    } match {
      case Success(transactionId) => Success(transactionId)
      case Failure(e: PaymentException) => Failure(e)
      case Failure(e) => Failure(PaymentErrorException(e.getMessage, e))
    }
  }

  override def sale(merchantKey: String, creditCard: CreditCard, payment: Payment, customer: Option[Customer], deal: Option[Deal]): Try[String] = {
    Try {
      require(payment.installments == 1, "Installments are not implemented")

      val merchant = merchantParser.parse(merchantKey)

      val request = PelecardHelper.createDebitRegularTypeRequest(merchant, creditCard, payment.currencyAmount)
      val response = pelecard.debitRegularType(request)

      verifyShvaStatusCode(response.StatusCode, response.ErrorMessage)

      response.ResultData.PelecardTransactionId
    } match {
      case Success(transactionId) => Success(transactionId)
      case Failure(e: PaymentException) => Failure(e)
      case Failure(e) => Failure(PaymentErrorException(e.getMessage, e))
    }
  }

  override def voidAuthorization(merchantKey: String, authorizationKey: String): Try[String] = {
    Try {
//      val merchant = merchantParser.parse(merchantKey)
      val authorization = authorizationParser.parse(authorizationKey)

      // PeleCard doesn't support voiding an authorization. Authorizations are automatically voided after a while
      // (usually around 2 days).
      authorization.transactionId
    }
  }

  private def authorize(merchant: PelecardMerchant,
                        creditCard: CreditCard,
                        payment: Payment): PelecardAuthorization = {
    val token = tokenize(merchant, creditCard)

    val request = PelecardHelper.createAuthorizeCreditCardRequest(merchant, creditCard, payment.currencyAmount)
    val response = pelecard.authorizeCreditCard(request)

    verifyShvaStatusCode(response.StatusCode, response.ErrorMessage)

    PelecardAuthorization(
      transactionId = response.ResultData.PelecardTransactionId,
      authorizationNumber = response.ResultData.DebitApproveNumber,
      token = token,
      currency = request.currency
    )
  }

  private def tokenize(merchant: PelecardMerchant, creditCard: CreditCard) = {
    val request = PelecardHelper.createConvertToTokenRequest(merchant, creditCard)
    val response = pelecard.convertToToken(request)

    verifyShvaStatusCode(response.StatusCode, response.ErrorMessage)
    response.ResultData.Token
  }

  private def verifyShvaStatusCode(statusCode: String, errorMessage: String): Unit = {
    statusCode match {
      case StatusCodes.success => // Operation successful.
      case IsShvaRejectedStatusCode(rejectedStatusCode) => throw PaymentRejectedException(s"$errorMessage (code = $rejectedStatusCode)")
      case _ => throw PaymentErrorException(s"$errorMessage (code = $statusCode)")
    }
  }
}
