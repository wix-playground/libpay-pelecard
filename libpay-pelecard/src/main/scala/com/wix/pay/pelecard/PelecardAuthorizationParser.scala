package com.wix.pay.pelecard

trait PelecardAuthorizationParser {
  def parse(authorizationKey: String): PelecardAuthorization
  def stringify(authorization: PelecardAuthorization): String
}
