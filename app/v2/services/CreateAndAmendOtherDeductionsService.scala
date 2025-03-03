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

import cats.implicits._
import common.errors.OutsideAmendmentWindowError
import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import v2.connectors.CreateAndAmendOtherDeductionsConnector
import v2.models.request.createAndAmendOtherDeductions.CreateAndAmendOtherDeductionsRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAndAmendOtherDeductionsService @Inject() (connector: CreateAndAmendOtherDeductionsConnector) extends BaseService {

  def createAndAmend(
      request: CreateAndAmendOtherDeductionsRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.createAndAmend(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID"        -> NinoFormatError,
      "INVALID_TAX_YEAR"                 -> TaxYearFormatError,
      "OUTSIDE_AMENDMENT_WINDOW"         -> OutsideAmendmentWindowError,
      "INCOME_SOURCE_NOT_FOUND"          -> NotFoundError,
      "INVALID_PAYLOAD"                  -> InternalError,
      "INVALID_CORRELATIONID"            -> InternalError,
      "BUSINESS_VALIDATION_RULE_FAILURE" -> InternalError,
      "SERVER_ERROR"                     -> InternalError,
      "SERVICE_UNAVAILABLE"              -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
