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

package v1.controllers

import api.controllers._
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.validators.CreateAndAmendOtherDeductionsValidatorFactory
import v1.models.response.createAndAmendOtherDeductions.CreateAndAmendOtherDeductionsHateoasData
import v1.models.response.createAndAmendOtherDeductions.CreateAndAmendOtherDeductionsResponse.CreateAndAmendOtherLinksFactory
import v1.services.CreateAndAmendOtherDeductionsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAndAmendOtherDeductionsController @Inject() (val authService: EnrolmentsAuthService,
                                                         val lookupService: MtdIdLookupService,
                                                         validatorFactory: CreateAndAmendOtherDeductionsValidatorFactory,
                                                         service: CreateAndAmendOtherDeductionsService,
                                                         auditService: AuditService,
                                                         hateoasFactory: HateoasFactory,
                                                         cc: ControllerComponents,
                                                         idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc)
    with V1Controller
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateAndAmendOtherDeductionsController", endpointName = "createAndAmendOtherDeductions")

  def handleRequest(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, request.body)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.createAndAmend)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "CreateAmendOtherDeductions",
          transactionName = "create-amend-other-deductions",
          apiVersion = apiVersion,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = Some(request.body),
          includeResponse = true
        ))
        .withHateoasResult(hateoasFactory)(CreateAndAmendOtherDeductionsHateoasData(nino, taxYear))

      requestHandler.handleRequest()
    }

}
