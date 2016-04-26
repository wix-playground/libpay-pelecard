package com.wix.pay.pelecard

import com.google.api.client.http.{ByteArrayContent, GenericUrl, HttpRequest, HttpRequestFactory}
import com.wix.pay.pelecard.model._
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

import scala.concurrent.duration.Duration

class PelecardHttpClient(requestFactory: HttpRequestFactory,
                         connectTimeout: Option[Duration] = None,
                         readTimeout: Option[Duration] = None,
                         numberOfRetries: Int = 0,
                         endpointUrl: String = Endpoints.production) {
  private implicit val formats = DefaultFormats

  def convertToToken(request: ConvertToTokenRequest): ConvertToTokenResponse = {
    val requestJson = Serialization.write(request)
    val responseJson = doJsonRequest("ConvertToToken", requestJson)
    Serialization.read[ConvertToTokenResponse](responseJson)
  }

  def debitRegularType(request: DebitRegularTypeRequest): DebitRegularTypeResponse = {
    val requestJson = Serialization.write(request)
    val responseJson = doJsonRequest("DebitRegularType", requestJson)
    Serialization.read[DebitRegularTypeResponse](responseJson)
  }

  def authorizeCreditCard(request: AuthorizeCreditCardRequest): AuthorizeCreditCardResponse = {
    val requestJson = Serialization.write(request)
    val responseJson = doJsonRequest("AuthorizeCreditCard", requestJson)
    Serialization.read[AuthorizeCreditCardResponse](responseJson)
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
