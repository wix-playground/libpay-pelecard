package com.wix.pay.pelecard

import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class JsonPelecardAuthorizationParser() extends PelecardAuthorizationParser {
  private implicit val formats = DefaultFormats

  override def parse(authorizationKey: String): PelecardAuthorization = {
    Serialization.read[PelecardAuthorization](authorizationKey)
  }

  override def stringify(authorization: PelecardAuthorization): String = {
    Serialization.write(authorization)
  }
}
