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

package v1.controllers.requestParsers.validators

import config.AppConfig
import javax.inject.Inject
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import v1.models.request.amendOtherDeductions.{AmendOtherDeductionsBody, AmendOtherDeductionsRawData, Seafarers}

class AmendOtherDeductionsValidator @Inject()(implicit appConfig: AppConfig)
  extends Validator[AmendOtherDeductionsRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidation, bodyFieldFormatValidation, dateRangeValidation)

  private def parameterFormatValidation: AmendOtherDeductionsRawData => List[List[MtdError]] = (data: AmendOtherDeductionsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: AmendOtherDeductionsRawData => List[List[MtdError]] = (data: AmendOtherDeductionsRawData) => {
    List(
      MtdTaxYearValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidation: AmendOtherDeductionsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendOtherDeductionsBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyFieldFormatValidation: AmendOtherDeductionsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendOtherDeductionsBody]

    List(flattenErrors(
      List(
        body.seafarers.map(_.zipWithIndex.flatMap {
          case (item, i) => validateSeafarers(item, i)
        })
      ).map(_.getOrElse(NoValidationErrors).toList)
    ))
  }

  private def validateSeafarers(seafarers: Seafarers, arrayIndex: Int): List[MtdError] = {
    List(
      CustomerReferenceValidation.validateOptional(
        field = seafarers.customerReference,
        path = s"/seafarers/$arrayIndex/customerReference"
      ),
      AmountValidation.validate(
        field = seafarers.amountDeducted,
        path = s"/seafarers/$arrayIndex/amountDeducted"
      ),
      NameOfShipValidation.validate(
        field = seafarers.nameOfShip,
        path = s"/seafarers/$arrayIndex/nameOfShip"
      ),
      DateValidation.validate(
        field = seafarers.fromDate,
        path = s"/seafarers/$arrayIndex/fromDate"
      ),
      DateValidation.validate(
        field = seafarers.toDate,
        path = s"/seafarers/$arrayIndex/toDate"
      )
    ).flatten
  }

  private def dateRangeValidation: AmendOtherDeductionsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendOtherDeductionsBody]

    List(flattenErrors(
      List(
        body.seafarers.map(_.zipWithIndex.flatMap {
          case (item, i) => validateToDateBeforeFromDate(item, i)
        })
      ).map(_.getOrElse(NoValidationErrors).toList)
    ))
  }

  private def validateToDateBeforeFromDate(seafarers: Seafarers, arrayIndex: Int): List[MtdError] = {
    List(
      ToDateBeforeFromDateValidation.validate(
        from = seafarers.fromDate,
        to = seafarers.toDate,
        fromPath = s"/seafarers/$arrayIndex/fromDate",
        toPath = s"/seafarers/$arrayIndex/toDate"
      )
    ).flatten
  }

  override def validate(data: AmendOtherDeductionsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
