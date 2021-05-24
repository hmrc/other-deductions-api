/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendOtherDeductionsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2021-22"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
      s"""
         |{
         |  "seafarers":[
         |    {
         |      "customerReference": "myRef",
         |      "amountDeducted": 2342.22,
         |      "nameOfShip": "Blue Bell",
         |      "fromDate": "2018-08-17",
         |      "toDate":"2018-10-02"
         |    }
         |  ]
         |}
    """.stripMargin)

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/deductions/other/$nino/$taxYear",
         |         "method":"PUT",
         |         "rel":"create-and-amend-deductions-other"
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

    def setupStubs(): StubMapping

    def uri: String = s"/$nino/$taxYear"

    def desUri: String = s"/income-tax/deductions/$nino/$taxYear"

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

  "Calling the amend other deductions endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }
    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new Test {

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |  "seafarers":[
            |    {
            |      "customerReference": "myRasefgsevfssfcgvqwertyuioplasdfghjkzxcvbnmhbjhgfdzxjxgfghjkhgnbfkhdfghjhgfndbdxgfcvhjghfmdfnhgchffxgcvhgbcef",
            |      "amountDeducted": -2342.22,
            |      "nameOfShip": "myRasefgsevfssfcgvqwertyuioplasdfghjkzxcvbnmhbjhgfdzxjxgfghjkhgnbfkhdfghjhgfndbdxgfcvhjghfmdfnhgchffxgcvhgbcef",
            |      "fromDate": "201-08-17",
            |      "toDate":"201-10-02"
            |    }
            |  ]
            |}
            |""".stripMargin)

                val allInvalidValueRequestError: List[MtdError] = List(
                  NameOfShipFormatError.copy(
                    message = "The provided name of ship is not valid",
                    paths = Some(List(
                      "/seafarers/0/nameOfShip"
                    ))
                  ),
                  CustomerReferenceFormatError.copy(
                    message = "The provided customer reference is not valid",
                    paths = Some(List(
                      "/seafarers/0/customerReference"
                    ))
                  ),
                  ValueFormatError.copy(
                    message = "The field should be between 0 and 99999999999.99",
                    paths = Some(List(
                      "/seafarers/0/amountDeducted"
                    ))
                  ),
                  DateFormatError.copy(
                    message = "The field should be in the format YYYY-MM-DD",
                    paths =Some(List(
                      "/seafarers/0/fromDate",
                      "/seafarers/0/toDate"
                    ))
                  )
                )

                val wrappedErrors: ErrorWrapper = ErrorWrapper(
                  correlationId = correlationId,
                  error = BadRequestError,
                  errors = Some(allInvalidValueRequestError)
                )

                override def setupStubs(): StubMapping = {
                  AuditStub.audit()
                  AuthStub.authorised()
                  MtdIdLookupStub.ninoFound(nino)
                }

                val response: WSResponse = await(request().put(allInvalidValueRequestBodyJson))
                response.status shouldBe BAD_REQUEST
                response.json shouldBe Json.toJson(wrappedErrors)
      }

      "return an error according to spec" when {

        val validRequestBodyJson = Json.parse(
          """
            |{
            |  "seafarers":[
            |    {
            |      "customerReference": "myRef",
            |      "amountDeducted": 2342.22,
            |      "nameOfShip": "Blue Bell",
            |      "fromDate": "2018-08-17",
            |      "toDate":"2018-10-02"
            |    },
            |    {
            |      "customerReference": "myOtherRef",
            |      "amountDeducted": 2872.16,
            |      "nameOfShip": "Blue Bell 2",
            |      "fromDate": "2019-06-17",
            |      "toDate":"2020-05-02"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val RangeToDateBeforeFromDateJson = Json.parse(
          """
            |{
            |  "seafarers":[
            |    {
            |      "customerReference": "myRef",
            |      "amountDeducted": 2342.22,
            |      "nameOfShip": "Blue Bell",
            |      "fromDate": "2019-08-17",
            |      "toDate":"2018-10-02"
            |    },
            |    {
            |      "customerReference": "myOtherRef",
            |      "amountDeducted": 2872.16,
            |      "nameOfShip": "Blue Bell 2",
            |      "fromDate": "2022-06-17",
            |      "toDate":"2020-05-02"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val allInvalidValueFormatRequestBodyJson = Json.parse(
          """
            |{
            |  "seafarers":[
            |    {
            |      "customerReference": "myRef",
            |      "amountDeducted": 2342.223653,
            |      "nameOfShip": "Blue Bell",
            |      "fromDate": "2018-08-17",
            |      "toDate":"2018-10-02"
            |    },
            |    {
            |      "customerReference": "myOtherRef",
            |      "amountDeducted": -2872.16,
            |      "nameOfShip": "Blue Bell 2",
            |      "fromDate": "2019-06-17",
            |      "toDate":"2020-05-02"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val allDatesInvalidRequestBodyJson = Json.parse(
          """
            |{
            |  "seafarers":[
            |    {
            |      "customerReference": "myRef",
            |      "amountDeducted": 2342.22,
            |      "nameOfShip": "Blue Bell",
            |      "fromDate": "08-17-2018",
            |      "toDate":"2018-20-20"
            |    },
            |    {
            |      "customerReference": "myOtherRef",
            |      "amountDeducted": 2872.16,
            |      "nameOfShip": "Blue Bell 2",
            |      "fromDate": "20-06-17",
            |      "toDate":"2020-02"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val allCustomerReferencesInvalidRequestBodyJson = Json.parse(
          """
            |{
            |  "seafarers":[
            |    {
            |      "customerReference": "bnmqwertyuioplkjhgfdsazxcvbnmqwertyuioplkjhgfdsazxcvbnmqwertyuioplkjhgfdsazxcvbnmqwertyuiop",
            |      "amountDeducted": 2342.22,
            |      "nameOfShip": "Blue Bell",
            |      "fromDate": "2018-08-17",
            |      "toDate":"2018-10-02"
            |    },
            |    {
            |      "customerReference": "myO+++=therRef",
            |      "amountDeducted": 2872.16,
            |      "nameOfShip": "Blue Bell 2",
            |      "fromDate": "2019-06-17",
            |      "toDate":"2020-05-02"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val allNamesOfShipsInvalidRequestBodyJson = Json.parse(
          """
            |{
            |  "seafarers":[
            |    {
            |      "customerReference": "myRef",
            |      "amountDeducted": 2342.22,
            |      "nameOfShip": "sqwertyuioplkjhgfdsazxcvbnjkmnbvcqwertyuioplkjhgfdsazxcvbnjkmnbvcqwertyuioplkjhgfdsazxcvbnjkmnbvcqwerqwety",
            |      "fromDate": "2018-08-17",
            |      "toDate":"2018-10-02"
            |    },
            |    {
            |      "customerReference": "myOtherRef",
            |      "amountDeducted": 2872.16,
            |      "nameOfShip": "Blsqwertyuioplkjhgfdsazxcvbnjkmnbvcqwertyuioplkjhgfdsazxcvbnjkmnbvcqwertyuioplkjhgfdsazxcvbnjkmnbvcqwerqwety2",
            |      "fromDate": "2019-06-17",
            |      "toDate":"2020-05-02"
            |    }
            |  ]
            |}
            |""".stripMargin)

        val allValueFormatError: MtdError = ValueFormatError.copy(
          message = "The field should be between 0 and 99999999999.99",
          paths = Some(Seq(
            "/seafarers/0/amountDeducted",
            "/seafarers/1/amountDeducted"
          ))
        )

        val allDateFormatError: MtdError = DateFormatError.copy(
          message = "The field should be in the format YYYY-MM-DD",
          paths = Some(List(
            "/seafarers/0/fromDate",
            "/seafarers/0/toDate",
            "/seafarers/1/fromDate",
            "/seafarers/1/toDate"
          ))
        )

        val allCustomerReferenceFormatErrors: MtdError = CustomerReferenceFormatError.copy(
          message = "The provided customer reference is not valid",
          paths = Some(List(
            "/seafarers/0/customerReference",
            "/seafarers/1/customerReference"
          ))
        )

        val allNamesOfShipsFormatErrors: MtdError = NameOfShipFormatError.copy(
          message = "The provided name of ship is not valid",
          paths = Some(List(
            "/seafarers/0/nameOfShip",
            "/seafarers/1/nameOfShip"
          ))
        )

        val aRangeToDateBeforeFromDateError: MtdError = RangeToDateBeforeFromDateError.copy(
          message = "The To date is before the From date",
          paths = Some(List(
            "/seafarers/0/fromDate","/seafarers/0/toDate",
            "/seafarers/1/fromDate","/seafarers/1/toDate"
          ))
        )

        "validation error" when {
          def validationErrorTest(requestNino: String, requestTaxYear: String, requestBody: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
            s"validation fails with ${expectedBody.code} error" in new Test {

              override val nino: String = requestNino
              override val taxYear: String = requestTaxYear
              override val requestBodyJson: JsValue = requestBody

              override def setupStubs(): StubMapping = {
                AuditStub.audit()
                AuthStub.authorised()
                MtdIdLookupStub.ninoFound(nino)
              }

              val response: WSResponse = await(request().put(requestBodyJson))
              response.status shouldBe expectedStatus
              response.json shouldBe Json.toJson(expectedBody)
            }
          }

          val input = Seq(
            ("AA1123A", "2019-20", validRequestBodyJson, BAD_REQUEST, NinoFormatError),
            ("AA123456A", "20177", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError),
            ("AA123456A", "2017-18", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
            ("AA123456A", "2019-20", RangeToDateBeforeFromDateJson, BAD_REQUEST, aRangeToDateBeforeFromDateError),
            ("AA123456A", "2019-20", allInvalidValueFormatRequestBodyJson, BAD_REQUEST, allValueFormatError),
            ("AA123456A", "2019-20", allDatesInvalidRequestBodyJson, BAD_REQUEST, allDateFormatError),
            ("AA123456A", "2019-20", allCustomerReferencesInvalidRequestBodyJson, BAD_REQUEST, allCustomerReferenceFormatErrors),
            ("AA123456A", "2019-20", allNamesOfShipsInvalidRequestBodyJson, BAD_REQUEST, allNamesOfShipsFormatErrors)
          )

          input.foreach(args => (validationErrorTest _).tupled(args))
        }

        "des service error" when {
          def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
            s"des returns an $desCode error and status $desStatus" in new Test {

              override def setupStubs(): StubMapping = {
                AuditStub.audit()
                AuthStub.authorised()
                MtdIdLookupStub.ninoFound(nino)
                DesStub.onError(DesStub.PUT, desUri, desStatus, errorBody(desCode))
              }

              val response: WSResponse = await(request().put(requestBodyJson))
              response.status shouldBe expectedStatus
              response.json shouldBe Json.toJson(expectedBody)
            }
          }

          val input = Seq(
            (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
            (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
            (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
            (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError))

          input.foreach(args => (serviceErrorTest _).tupled(args))
        }
      }
    }
  }
}
