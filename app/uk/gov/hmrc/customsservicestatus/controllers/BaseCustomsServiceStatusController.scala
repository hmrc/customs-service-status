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

import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.mvc.{ControllerComponents, Request, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

abstract class BaseCustomsServiceStatusController(cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  implicit val logger: Logger = Logger(this.getClass.getName)

  def validateJson[T](f: T => Future[Result])(implicit request: Request[JsValue], r: Reads[T]): Future[Result] =
    request.body.validate[T] match {
      case JsSuccess(t, _) => f(t)
      case error @ JsError(errors) =>
        logger.warn(
          s"""|Failed to validate JSON from request body: ${error.toLogFormat}""".stripMargin
        )
        val errorMessage = s"${errors.flatMap(_._2).flatMap(_.messages).mkString(", ")}"
        Future(BadRequest(errorMessage))
    }
}
