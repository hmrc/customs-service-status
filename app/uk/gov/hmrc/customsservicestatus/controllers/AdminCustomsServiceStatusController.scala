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
import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.customsservicestatus.models.OutageData.format
import uk.gov.hmrc.customsservicestatus.services.AdminCustomsStatusService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class AdminCustomsServiceStatusController @Inject() (adminCustomsServiceStatusService: AdminCustomsStatusService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BaseCustomsServiceStatusController(cc) {
  def updateWithOutageData(): Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      validateJson[OutageData] { outageData =>
        adminCustomsServiceStatusService
          .submitOutage(outageData)
          .map {
            case Left(error) =>
              logger.error(s"Outage with internal reference ${outageData.internalReference} could not be written to the database")
              InternalServerError
            case Right(_) => Ok
          }
      }
    }

  def getPlannedWork: Action[AnyContent] = Action.async { implicit request =>
    adminCustomsServiceStatusService.getPlannedWork.map(result => Ok(Json.toJson(result)))
  }
}
