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

package v2.models.request.createAndAmendOtherDeductions

import play.api.libs.json.Json
import shared.utils.UnitSpec

class SeafarersSpec extends UnitSpec {
  val seafarers = Seafarers(Some("myRef"), 2000.99, "Blue Bell", "2018-04-06", "2019-04-06")

  val noRefSeafares = Seafarers(None, 2000.99, "Blue Bell", "2018-04-06", "2019-04-06")

  val json = Json.parse("""
      |{
      |   "customerReference": "myRef",
      |   "amountDeducted": 2000.99,
      |   "nameOfShip": "Blue Bell",
      |   "fromDate": "2018-04-06",
      |   "toDate": "2019-04-06"
      |}""".stripMargin)

  val noRefJson = Json.parse("""
      |{
      |   "amountDeducted": 2000.99,
      |   "nameOfShip": "Blue Bell",
      |   "fromDate": "2018-04-06",
      |   "toDate": "2019-04-06"
      |}""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        json.as[Seafarers] shouldBe seafarers
      }
    }
  }

  "reads from a JSON with no reference" when {
    "passed a JSON with no customer reference" should {
      "return a model with no customer reference " in {
        noRefJson.as[Seafarers] shouldBe noRefSeafares
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(seafarers) shouldBe json
      }
    }
  }

  "writes from a model with no reference" when {
    "passed a model with no customer reference" should {
      "return a JSON with no customer reference" in {
        Json.toJson(noRefSeafares) shouldBe noRefJson
      }
    }
  }

}
