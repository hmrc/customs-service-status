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

import play.api.{Application, Mode}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import uk.gov.hmrc.customsservicestatus.controllers.test.TestController
import uk.gov.hmrc.customsservicestatus.helpers.BaseISpec
import uk.gov.hmrc.customsservicestatus.models.config.Services

class ServiceControllerISpec extends BaseISpec {

  val testController: TestController = app.injector.instanceOf[TestController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(callRoute(fakeRequest(uk.gov.hmrc.customsservicestatus.controllers.test.routes.TestController.clearAllData)))
  }

  "GET /services" should {
    "return Ok with empty list if there are services configured, but no corresponding entries in the db" in {

      val result = callRoute(fakeRequest(routes.ServiceController.list()))
      status(result)                                   shouldBe (OK)
      contentAsJson(result).as[Services].services.size shouldBe (0)
    }

    "return Ok with one service in the response if it is configured and have a corresponding entry in the db" in {
      val insertEntry = callRoute(fakeRequest(routes.ServiceController.check("haulier")).withMethod("PUT"))
      status(insertEntry) shouldBe (OK)
      val result = callRoute(fakeRequest(routes.ServiceController.list()))
      status(result)                                   shouldBe (OK)
      contentAsJson(result).as[Services].services.size shouldBe (1)
    }

  }
}
