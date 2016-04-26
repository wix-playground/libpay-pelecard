package com.wix.pay.pelecard


import com.wix.pay.pelecard.PelecardMatchers._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class JsonPelecardMerchantParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val merchantParser: PelecardMerchantParser = new JsonPelecardMerchantParser
  }


  "stringify and then parse" should {
    "yield a merchant similar to the original one" in new Ctx {
      val someMerchant = PelecardMerchant(
        terminalNumber = "some terminal number",
        user = "some user",
        password = "some password",
        shopNumber = "some shop number"
      )

      val merchantKey = merchantParser.stringify(someMerchant)
      merchantParser.parse(merchantKey) must beMerchant(
        terminalNumber = ===(someMerchant.terminalNumber),
        user = ===(someMerchant.user),
        password = ===(someMerchant.password),
        shopNumber = ===(someMerchant.shopNumber)
      )
    }
  }
}
