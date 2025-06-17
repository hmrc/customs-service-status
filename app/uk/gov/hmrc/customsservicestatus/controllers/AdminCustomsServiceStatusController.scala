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
import uk.gov.hmrc.customsservicestatus.models.{State, UnplannedOutageRequestData}
import uk.gov.hmrc.customsservicestatus.services.AdminCustomsServiceStatusService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class AdminCustomsServiceStatusController @Inject() (adminCustomsServiceStatusService: AdminCustomsServiceStatusService, cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends BaseCustomsServiceStatusController(cc) {

  def updateWithUnplannedOutage(): Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      validateJson[UnplannedOutageRequestData] { unplannedOutageData =>
        adminCustomsServiceStatusService
          .submitUnplannedOutage(unplannedOutageData)
          .map {
            case Some(error) => BadRequest
            case None        => Ok
          }
      }
    }

  def list(): Action[AnyContent] = Action.async { _ =>
    adminCustomsServiceStatusService.listAll.map(result => Ok(Json.toJson(result)))
  }
}
