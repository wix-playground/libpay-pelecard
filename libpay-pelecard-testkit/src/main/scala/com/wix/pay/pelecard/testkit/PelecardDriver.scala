package com.wix.pay.pelecard.testkit


import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import com.wix.e2e.http.api.StubWebServer
import com.wix.e2e.http.client.extractors.HttpMessageExtractors._
import com.wix.e2e.http.server.WebServerFactory.aStubWebServer
import com.wix.pay.pelecard._
import com.wix.pay.pelecard.model._


class PelecardDriver(port: Int) {
  private val server: StubWebServer = aStubWebServer.onPort(port).build

  private val debitRegularTypeRequestParser = new DebitRegularTypeRequestParser
  private val debitRegularTypeResponseParser = new DebitRegularTypeResponseParser
  private val authorizeCreditCardRequestParser = new AuthorizeCreditCardRequestParser
  private val authorizeCreditCardResponseParser = new AuthorizeCreditCardResponseParser
  private val convertToTokenRequestParser = new ConvertToTokenRequestParser
  private val convertToTokenResponseParser = new ConvertToTokenResponseParser

  def start(): Unit = server.start()
  def stop(): Unit = server.stop()
  def reset(): Unit = server.replaceWith()

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
    def isStubbedRequestEntity(entity: HttpEntity, headers: Seq[HttpHeader]): Boolean = {
      isAuthorized(headers) && verifyContent(entity)
    }

    private def isAuthorized(headers: Seq[HttpHeader]): Boolean = true

    def verifyContent(entity: HttpEntity): Boolean

    protected def returns(statusCode: StatusCode, responseJson: String): Unit = {
      server.appendAll {
        case HttpRequest(
          HttpMethods.POST,
          Path(`resource`),
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
      val actualRequest = debitRegularTypeRequestParser.parse(entity.extractAsString)
      request == actualRequest
    }

    def returns(response: DebitRegularTypeResponse): Unit = {
      returns(StatusCodes.OK, debitRegularTypeResponseParser.stringify(response))
    }
  }

  class AuthorizeCreditCardRequestCtx(request: AuthorizeCreditCardRequest) extends Ctx("/AuthorizeCreditCard") {
    override def verifyContent(entity: HttpEntity): Boolean = {
      val actualRequest = authorizeCreditCardRequestParser.parse(entity.extractAsString)
      request == actualRequest
    }

    def returns(response: AuthorizeCreditCardResponse): Unit = {
      returns(StatusCodes.OK, authorizeCreditCardResponseParser.stringify(response))
    }
  }

  class ConvertToTokenRequestCtx(request: ConvertToTokenRequest) extends Ctx("/ConvertToToken") {
    override def verifyContent(entity: HttpEntity): Boolean = {
      val actualRequest = convertToTokenRequestParser.parse(entity.extractAsString)
      request == actualRequest
    }

    def returns(response: ConvertToTokenResponse): Unit = {
      returns(StatusCodes.OK, convertToTokenResponseParser.stringify(response))
    }
  }
}
