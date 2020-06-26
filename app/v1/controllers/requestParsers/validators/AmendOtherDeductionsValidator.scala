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

package v1.controllers.requestParsers.validators

import v1.controllers.requestParsers.validators.validations.{JsonFormatValidation, NinoValidation, TaxYearValidation}
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}

class AmendOtherDeductionsValidator extends Validator[AmendOtherDeductionsRawData] {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendOtherDeductionsRawData => List[List[MtdError]] = (data: AmendOtherDeductionsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidation: AmendOtherDeductionsRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendOtherDeductionsBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyFieldValidation: AmendOtherDeductionsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendOtherDeductionsBody]

    List(flattenErrors(
      List(
        body.socialEnterpriseInvestment.map(_.zipWithIndex.flatMap {
          case (item, i) => validatesocialEnterpriseInvestment(item, i)
        })
      ).map(_.getOrElse(NoValidationErrors).toList)
    ))
  }

  private def validatesocialEnterpriseInvestment(socialEnterpriseInvestmentItem: SocialEnterpriseInvestmentItem, arrayIndex: Int): List[MtdError] = {
    List(
      DateValidation.validateOptional(
        date = socialEnterpriseInvestmentItem.dateOfInvestment,
        path = s"/socialEnterpriseInvestment/$arrayIndex/dateOfInvestment",
        error = DateOfInvestmentFormatError
      ),
      NumberValidation.validateOptional(
        field = socialEnterpriseInvestmentItem.amountInvested,
        path = s"/socialEnterpriseInvestment/$arrayIndex/amountInvested"
      ),
      NumberValidation.validateOptional(
        field = socialEnterpriseInvestmentItem.reliefClaimed,
        path = s"/socialEnterpriseInvestment/$arrayIndex/reliefClaimed"
      ),
    ).flatten
  }


  override def validate(data: AmendReliefInvestmentsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
