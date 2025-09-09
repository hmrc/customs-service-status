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

package uk.gov.hmrc.customsservicestatus.controllers.test

import com.google.inject.*
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.customsservicestatus.controllers.BaseCustomsServiceStatusController
import uk.gov.hmrc.customsservicestatus.services.test.TestService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestController @Inject() (
  defaultActionBuilder: DefaultActionBuilder,
  testService:          TestService
)(implicit val ec: ExecutionContext, cc: ControllerComponents)
    extends BaseCustomsServiceStatusController(cc) {

  def clearAllData: Action[AnyContent] = defaultActionBuilder.async {
    withRecover {
      logger.warn("clear all data called")
      testService.clearAllData.map(_ => Ok)
    }
  }

  def list(): Action[AnyContent] = Action.async { _ =>
    testService.listAll.map(result => Ok(Json.toJson(result)))
  }

  def listArchived(): Action[AnyContent] = Action.async { _ =>
    testService.listAllArchived.map(result => Ok(Json.toJson(result)))
  }

  private def withRecover(f: => Future[Result]): Future[Result] = f.recover { case e: Exception =>
    InternalServerError("Failure with message: " + e.toString)
  }
}
