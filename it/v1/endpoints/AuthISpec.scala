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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{FORBIDDEN, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino    = "AA123456A"
    val taxYear = "2021-22"
    val data    = "someData"

    val ifsResponseBody: JsValue = Json.parse(
      """
        |{
        |   "submittedOn": "2019-04-04T01:01:01.000Z",
        |   "seafarers": [{
        |      "customerReference": "SEAFARERS1234",
        |      "amountDeducted": 2543.32,
        |      "nameOfShip": "Blue Bell",
        |      "fromDate": "2019-04-06",
        |      "toDate": "2020-04-05"
        |   }]
        |}
        |""".stripMargin
    )

    def ifsUri: String = s"/income-tax/deductions/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def uri: String = s"/$nino/$taxYear"

  }

  "Calling the deductions endpoint" when {

    "the NINO cannot be converted to a MTD ID" should {

      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.error(nino, INTERNAL_SERVER_ERROR)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is authorised" should {

      "MTD ID lookup fails with a 403" should {

        "return 403" in new Test {
          override val nino: String = "AA123456A"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            MtdIdLookupStub.error(nino, FORBIDDEN)
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe FORBIDDEN
        }

      }

      "return success status" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, ifsUri, OK, ifsResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT logged in" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe FORBIDDEN
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT authorised" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe FORBIDDEN
      }
    }
  }

}
