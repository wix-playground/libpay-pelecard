package com.wix.pay.pelecard.model

/**
 * @param StatusCode   @see com.wix.pay.direct.shva.model.StatusCodes
 */
case class AuthorizeCreditCardResponse(StatusCode: String,
                                       ErrorMessage: String,
                                       ResultData: ResultData)
