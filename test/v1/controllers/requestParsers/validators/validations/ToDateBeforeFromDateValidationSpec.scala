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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.RangeToDateBeforeFromDateError

class ToDateBeforeFromDateValidationSpec extends UnitSpec {

  val date1 = "2019-03-12"
  val date2 = "2020-01-01"
  val path1 = "/seafarers/1/fromDate"
  val path2 = "/seafarers/1/toDate"

  "validate" should {
    "return no errors" when {
      "fromDate is before toDate" in {
        val validationResult = ToDateBeforeFromDateValidation.validate(date1, date2, path1, path2)

        validationResult.isEmpty shouldBe true

      }
    }
    "return a RangeToDateBeforeFromDateError" when {
      "toDate is before fromDate" in {
        val validationResult = ToDateBeforeFromDateValidation.validate(date2, date1, path1, path2)

        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe RangeToDateBeforeFromDateError.copy(paths = Some(Seq(path1, path2)))

      }
    }
  }

}
