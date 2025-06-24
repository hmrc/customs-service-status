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

package uk.gov.hmrc.customsservicestatus.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.customsservicestatus.errorhandlers.CustomsServiceStatusError.ServiceNotConfiguredError
import uk.gov.hmrc.customsservicestatus.errorhandlers.ErrorResponse.UnrecognisedServiceError
import uk.gov.hmrc.customsservicestatus.models.State
import uk.gov.hmrc.customsservicestatus.services.CustomsServiceStatusService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class CustomsServiceStatusController @Inject() (customsServiceStatusService: CustomsServiceStatusService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BaseCustomsServiceStatusController(cc) {

  def updateServiceStatus(serviceId: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    validateJson[State] { state =>
      customsServiceStatusService
        .updateServiceStatus(serviceId, state)
        .fold(
          { case ServiceNotConfiguredError =>
            NotFound(UnrecognisedServiceError(serviceId).message)
          },
          _ => Ok
        )
    }
  }

  def list(): Action[AnyContent] = Action.async { _ =>
    customsServiceStatusService.listAll.map(result => Ok(Json.toJson(result)))
  }
}
