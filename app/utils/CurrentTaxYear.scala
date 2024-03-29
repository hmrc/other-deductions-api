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

package utils

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import javax.inject.Singleton

@Singleton
class CurrentTaxYear {

  private lazy val expectedDateFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  def getCurrentTaxYear(date: DateTime): Int = {

    lazy val taxYearStartDate: DateTime = DateTime.parse(s"${date.getYear}-04-06", expectedDateFormat)

    if (date.isBefore(taxYearStartDate)) date.getYear else date.getYear + 1
  }

}
