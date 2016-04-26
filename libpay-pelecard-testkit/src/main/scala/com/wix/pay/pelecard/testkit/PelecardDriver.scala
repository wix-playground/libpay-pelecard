package com.wix.pay.pelecard.testkit

import com.wix.hoopoe.http.testkit.EmbeddedHttpProbe
import com.wix.pay.pelecard._
import com.wix.pay.pelecard.model._
import spray.http._


class PelecardDriver(port: Int) {
  private val probe = new EmbeddedHttpProbe(port, EmbeddedHttpProbe.NotFoundHandler)

  private val debitRegularTypeRequestParser = new DebitRegularTypeRequestParser
  private val debitRegularTypeResponseParser = new DebitRegularTypeResponseParser
  private val authorizeCreditCardRequestParser = new AuthorizeCreditCardRequestParser
  private val authorizeCreditCardResponseParser = new AuthorizeCreditCardResponseParser
  private val convertToTokenRequestParser = new ConvertToTokenRequestParser
  private val convertToTokenResponseParser = new ConvertToTokenResponseParser

  def startProbe() {
    probe.doStart()
  }

  def stopProbe() {
    probe.doStop()
  }

  def resetProbe() {
    probe.handlers.clear()
  }

  def aDebitRegularTypeRequestFor(request: DebitRegularTypeRequest): DebitRegularTypeRequestCtx = {
    new DebitRegularTypeRequestCtx(
      request = request
    )
  }

  def anAuthorizeCreditCardRequestFor(request: AuthorizeCreditCardRequest): AuthorizeCreditCardRequestCtx = {
    new AuthorizeCreditCardRequestCtx(
      request = request
    )
  }

  def aConvertToTokenRequestFor(request: ConvertToTokenRequest): ConvertToTokenRequestCtx = {
    new ConvertToTokenRequestCtx(
      request = request
    )
  }

  abstract class Ctx(val resource: String) {
    def isStubbedRequestEntity(entity: HttpEntity, headers: List[HttpHeader]): Boolean = {
      isAuthorized(headers) && verifyContent(entity)
    }

    private def isAuthorized(headers: List[HttpHeader]): Boolean = {
      true
    }

    def verifyContent(entity: HttpEntity): Boolean

    protected def returns(statusCode: StatusCode, responseJson: String): Unit = {
      probe.handlers += {
        case HttpRequest(
        HttpMethods.POST,
        Uri.Path(`resource`),
        headers,
        entity,
        _) if isStubbedRequestEntity(entity, headers) =>
          HttpResponse(
            status = statusCode,
            entity = HttpEntity(ContentTypes.`application/json`, responseJson))
      }
    }
  }

  class DebitRegularTypeRequestCtx(request: DebitRegularTypeRequest) extends Ctx("/DebitRegularType") {
    override def verifyContent(entity: HttpEntity): Boolean = {
      val actualRequest = debitRegularTypeRequestParser.parse(entity.asString)
      request == actualRequest
    }

    def returns(response: DebitRegularTypeResponse): Unit = {
      returns(StatusCodes.OK, debitRegularTypeResponseParser.stringify(response))
    }
  }

  class AuthorizeCreditCardRequestCtx(request: AuthorizeCreditCardRequest) extends Ctx("/AuthorizeCreditCard") {
    override def verifyContent(entity: HttpEntity): Boolean = {
      val actualRequest = authorizeCreditCardRequestParser.parse(entity.asString)
      request == actualRequest
    }

    def returns(response: AuthorizeCreditCardResponse): Unit = {
      returns(StatusCodes.OK, authorizeCreditCardResponseParser.stringify(response))
    }
  }

  class ConvertToTokenRequestCtx(request: ConvertToTokenRequest) extends Ctx("/ConvertToToken") {
    override def verifyContent(entity: HttpEntity): Boolean = {
      val actualRequest = convertToTokenRequestParser.parse(entity.asString)
      request == actualRequest
    }

    def returns(response: ConvertToTokenResponse): Unit = {
      returns(StatusCodes.OK, convertToTokenResponseParser.stringify(response))
    }
  }
}
