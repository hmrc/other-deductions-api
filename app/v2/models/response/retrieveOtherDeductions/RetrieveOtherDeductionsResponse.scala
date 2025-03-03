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

package v2.models.response.retrieveOtherDeductions

import play.api.libs.json.{Json, OFormat}
import shared.config.SharedAppConfig
import shared.hateoas.{HateoasData, HateoasLinksFactory, Link}
import shared.models.domain.Timestamp
import v2.HateoasLinks

case class RetrieveOtherDeductionsResponse(submittedOn: Timestamp, seafarers: Option[Seq[Seafarers]])

object RetrieveOtherDeductionsResponse extends HateoasLinks {
  implicit val format: OFormat[RetrieveOtherDeductionsResponse] = Json.format[RetrieveOtherDeductionsResponse]

  implicit object RetrieveOtherLinksFactory extends HateoasLinksFactory[RetrieveOtherDeductionsResponse, RetrieveOtherDeductionsHateoasData] {

    override def links(appConfig: SharedAppConfig, data: RetrieveOtherDeductionsHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAndAmendOtherDeductions(appConfig, nino, taxYear),
        retrieveOtherDeductions(appConfig, nino, taxYear),
        deleteOtherDeductions(appConfig, nino, taxYear)
      )
    }

  }

}

case class RetrieveOtherDeductionsHateoasData(nino: String, taxYear: String) extends HateoasData
