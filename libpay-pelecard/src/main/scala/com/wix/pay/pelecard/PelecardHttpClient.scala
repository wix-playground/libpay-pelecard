package com.wix.pay.pelecard

import com.google.api.client.http.{ByteArrayContent, GenericUrl, HttpRequest, HttpRequestFactory}
import com.wix.pay.pelecard.model._

import scala.concurrent.duration.Duration

class PelecardHttpClient(requestFactory: HttpRequestFactory,
                         connectTimeout: Option[Duration] = None,
                         readTimeout: Option[Duration] = None,
                         numberOfRetries: Int = 0,
                         endpointUrl: String = Endpoints.production) {
  private val debitRegularTypeRequestParser = new DebitRegularTypeRequestParser
  private val debitRegularTypeResponseParser = new DebitRegularTypeResponseParser
  private val authorizeCreditCardRequestParser = new AuthorizeCreditCardRequestParser
  private val authorizeCreditCardResponseParser = new AuthorizeCreditCardResponseParser
  private val convertToTokenRequestParser = new ConvertToTokenRequestParser
  private val convertToTokenResponseParser = new ConvertToTokenResponseParser

  def convertToToken(request: ConvertToTokenRequest): ConvertToTokenResponse = {
    val requestJson = convertToTokenRequestParser.stringify(request)
    val responseJson = doJsonRequest("ConvertToToken", requestJson)
    convertToTokenResponseParser.parse(responseJson)
  }

  def debitRegularType(request: DebitRegularTypeRequest): DebitRegularTypeResponse = {
    val requestJson = debitRegularTypeRequestParser.stringify(request)
    val responseJson = doJsonRequest("DebitRegularType", requestJson)
    debitRegularTypeResponseParser.parse(responseJson)
  }

  def authorizeCreditCard(request: AuthorizeCreditCardRequest): AuthorizeCreditCardResponse = {
    val requestJson = authorizeCreditCardRequestParser.stringify(request)
    val responseJson = doJsonRequest("AuthorizeCreditCard", requestJson)
    authorizeCreditCardResponseParser.parse(responseJson)
  }

  private def doJsonRequest(resource: String, requestJson: String): String = {
    val httpRequest = requestFactory.buildPostRequest(
      new GenericUrl(endpointUrl + resource),
      new ByteArrayContent("application/json", requestJson.getBytes("UTF-8"))
    )

    connectTimeout foreach (to => httpRequest.setConnectTimeout(to.toMillis.toInt))
    readTimeout foreach (to => httpRequest.setReadTimeout(to.toMillis.toInt))
    httpRequest.setNumberOfRetries(numberOfRetries)

    executeRequest(httpRequest)
  }

  private def executeRequest(httpRequest: HttpRequest): String = {
    val httpResponse = httpRequest.execute()
    try {
      httpResponse.parseAsString()
    } finally {
      httpResponse.ignore()
    }
  }
}
