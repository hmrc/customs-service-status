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

import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.customsservicestatus.models.UnplannedOutageData
import uk.gov.hmrc.customsservicestatus.services.AdminCustomsStatusService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class AdminCustomsServiceStatusController @Inject() (adminCustomsServiceStatusService: AdminCustomsStatusService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BaseCustomsServiceStatusController(cc) {
  def updateWithUnplannedOutage(): Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      validateJson[UnplannedOutageData] { unplannedOutageData =>
        adminCustomsServiceStatusService
          .submitUnplannedOutage(unplannedOutageData)
          .map {
            case Left(error) =>
              logger.error(s"Unplanned outage with internal reference ${unplannedOutageData.internalReference} could not be written to the database")
              InternalServerError
            case Right(_) => Ok
          }
      }
    }
}
