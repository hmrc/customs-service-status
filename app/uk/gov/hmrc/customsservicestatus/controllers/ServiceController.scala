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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.customservicestatus.errorhandlers.CustomsServiceStatusError.{LoadServicesConfigError, ServiceNotConfiguredError}
import uk.gov.hmrc.customsservicestatus.models.State
import uk.gov.hmrc.customsservicestatus.models.State._
import uk.gov.hmrc.customsservicestatus.models.db.Services._
import uk.gov.hmrc.customsservicestatus.services.CheckService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class ServiceController @Inject()(checkService: CheckService, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BaseCustomsServiceStatusController(cc) {

  def check(service: String): Action[AnyContent] = Action.async { implicit request =>
    checkService
      .check(service)
      .fold(
        error =>
          error match {
            case LoadServicesConfigError   => ServiceUnavailable
            case ServiceNotConfiguredError => NotFound(Json.toJson(State("Not Found")))
        },
        _ => Ok(Json.toJson(State("OK")))
      )
  }

  def list(): Action[AnyContent] = Action.async { implicit request =>
    checkService.listAll map (result => Ok(Json.toJson(result)))
  }
}
