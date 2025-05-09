/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v2.models.response.retrieveOtherDeductions

import play.api.libs.json.{JsValue, Json}
import shared.config.MockSharedAppConfig
import shared.models.domain.Timestamp
import shared.utils.UnitSpec
import v2.fixtures.RetrieveOtherDeductionsFixtures._

class RetrieveOtherDeductionsResponseSpec extends UnitSpec with MockSharedAppConfig {

  val multipleSeafarersRetrieveOtherDeductionsResponse: RetrieveOtherDeductionsResponse = RetrieveOtherDeductionsResponse(
    submittedOn = Timestamp("2019-04-04T01:01:01.000Z"),
    seafarers = Some(Seq(seafarersModel, seafarersModel))
  )

  val noRefRetrieveOtherDeductionsResponse: RetrieveOtherDeductionsResponse = RetrieveOtherDeductionsResponse(
    Timestamp("2019-04-04T01:01:01Z"),
    Some(Seq(seafarersModel.copy(customerReference = None)))
  )

  val jsonMultipleSeafarers: JsValue = Json.parse(
    s"""{
      | "submittedOn": "2019-04-04T01:01:01.000Z",
      | "seafarers": [$seafarersJson, $seafarersJson]
      |}""".stripMargin
  )

  val jsonNoRef: JsValue = Json.parse("""{
      | "submittedOn": "2019-04-04T01:01:01.000Z",
      | "seafarers": [{
      |   "amountDeducted": 2000.99,
      |   "nameOfShip": "Blue Bell",
      |   "fromDate": "2018-04-06",
      |   "toDate": "2019-04-06"
      |   }]
      |}""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        responseBodyJson.as[RetrieveOtherDeductionsResponse] shouldBe responseBodyModel
      }
    }
    "passed a JSON with multiple seafarers" should {
      "return a valid model with multiple seafarers" in {
        jsonMultipleSeafarers.as[RetrieveOtherDeductionsResponse] shouldBe multipleSeafarersRetrieveOtherDeductionsResponse
      }
    }
    "passed JSON with no customer reference" should {
      "return a model with no customer reference" in {
        jsonNoRef.as[RetrieveOtherDeductionsResponse] shouldBe noRefRetrieveOtherDeductionsResponse
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(responseBodyModel) shouldBe responseBodyJson
      }
    }
    "passed a model with multiple seafarers" should {
      "return a JSON with multiple seafarers" in {
        Json.toJson(multipleSeafarersRetrieveOtherDeductionsResponse) shouldBe jsonMultipleSeafarers
      }
    }
    "passed a body with no customer reference" should {
      "return a JSON with no customer reference" in {
        Json.toJson(noRefRetrieveOtherDeductionsResponse) shouldBe jsonNoRef
      }
    }
  }

}
