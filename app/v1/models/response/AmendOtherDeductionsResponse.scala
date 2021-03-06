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

package v1.models.response

import config.AppConfig
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

object AmendOtherDeductionsResponse extends HateoasLinks {

  implicit object AmendOtherLinksFactory extends HateoasLinksFactory[Unit, AmendOtherDeductionsHateoasData] {
    override def links(appConfig: AppConfig, data: AmendOtherDeductionsHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAndAmendOtherDeductions(appConfig, nino, taxYear),
        retrieveOtherDeductions(appConfig, nino, taxYear),
        deleteOtherDeductions(appConfig, nino, taxYear)
      )
    }
  }
}


case class AmendOtherDeductionsHateoasData(nino: String, taxYear: String) extends HateoasData