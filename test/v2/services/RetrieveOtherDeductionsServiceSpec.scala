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

package v2.services

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.RetrieveOtherDeductionsFixtures.responseBodyModel
import v2.mocks.connectors.MockRetrieveOtherDeductionsConnector
import v2.models.request.retrieveOtherDeductions.RetrieveOtherDeductionsRequestData

import scala.concurrent.Future

class RetrieveOtherDeductionsServiceSpec extends ServiceSpec {

  private val nino    = "AA123456A"
  private val taxYear = "2017-18"

  private val requestData = RetrieveOtherDeductionsRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  trait Test extends MockRetrieveOtherDeductionsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveOtherDeductionsService(
      connector = mockRetrieveOtherDeductionsConnector
    )

  }

  "service" should {
    "return a successful response" when {
      "a successful response is passed through" in new Test {
        MockRetrieveOtherDeductionsConnector
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBodyModel))))

        await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseBodyModel))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveOtherDeductionsConnector
            .retrieve(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("INVALID_CORRELATIONID", InternalError),
        ("NO_DATA_FOUND", NotFoundError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      val extraTysErrors = List(
        ("INVALID_CORRELATION_ID", InternalError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
