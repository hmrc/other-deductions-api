/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class RetrieveOtherDeductionsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val taxYear = "2019-20"

    val responseBody = Json.parse(
      s"""
         |{
         |   "seafarers": [{
         |      "customerReference": "SEAFARERS1234",
         |      "amountDeducted": 2543.32,
         |      "nameOfShip": "Blue Bell",
         |      "fromDate": "2019-04-06",
         |      "toDate": "2020-04-05"
         |   }],
         |   "links":[
         |      {
         |         "href":"/individuals/deductions/other/$nino/$taxYear",
         |         "method":"PUT",
         |         "rel":"amend-deductions-other"
         |      },
         |      {
         |         "href":"/individuals/deductions/other/$nino/$taxYear",
         |         "method":"GET",
         |         "rel":"self"
         |      },
         |      {
         |         "href":"/individuals/deductions/other/$nino/$taxYear",
         |         "method":"DELETE",
         |         "rel":"delete-deductions-other"
         |      }
         |   ]
         |}
         |""".stripMargin)

    val desResponseBody = Json.parse(
      s"""
         |{
         |   "seafarers": [{
         |      "customerReference": "SEAFARERS1234",
         |      "amountDeducted": 2543.32,
         |      "nameOfShip": "Blue Bell",
         |      "fromDate": "2019-04-06",
         |      "toDate": "2020-04-05"
         |   }]
         |}
         |""".stripMargin)

    def uri: String = s"/$nino/$taxYear"

    def desUri: String = s"/deductions/other/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "calling the retrieve endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Status.OK, desResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
  }
}
