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

import cats.data.EitherT
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.customsservicestatus.models.OutageData.format
import uk.gov.hmrc.customsservicestatus.models.OutageType
import uk.gov.hmrc.customsservicestatus.services.AdminCustomsStatusService

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class AdminCustomsServiceStatusController @Inject() (adminCustomsServiceStatusService: AdminCustomsStatusService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BaseCustomsServiceStatusController(cc) {
  def updateWithOutageData(): Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      validateJson[OutageData] { outageData =>
        EitherT(
          adminCustomsServiceStatusService
            .submitOutage(outageData)
        )
          .fold(
            error => {
              logger.error(s"Outage with internal reference ${outageData.internalReference.text} could not be written to the database")
              InternalServerError
            },
            _ => Ok
          )
      }
    }

  def getAllPlannedWorks: Action[AnyContent] = Action.async { implicit request =>
    adminCustomsServiceStatusService.getAllPlannedWorks.map(result => Ok(Json.toJson(result)))
  }

  def getLatestOutage(outageType: OutageType): Action[AnyContent] = Action.async { _ =>
    adminCustomsServiceStatusService.getLatestOutage(outageType).map {
      case Some(result) => Ok(Json.toJson(result))
      case None         => NotFound
    }
  }

  def findAllOutages(): Action[AnyContent] = Action.async { implicit request =>
    adminCustomsServiceStatusService.findAllOutages().map(result => Ok(Json.toJson(result)))
  }

  def findOutage(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    adminCustomsServiceStatusService.findOutage(id).map(outage => Ok(Json.toJson(outage)))
  }

  def archiveOutage(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    EitherT(adminCustomsServiceStatusService.archiveOutage(id)).fold(_ => InternalServerError, outage => Ok(Json.toJson(outage)))
  }
}
