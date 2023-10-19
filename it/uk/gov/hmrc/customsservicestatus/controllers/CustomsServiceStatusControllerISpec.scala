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
import uk.gov.hmrc.customsservicestatus.controllers.test.TestController
import uk.gov.hmrc.customsservicestatus.helpers.BaseISpec
import uk.gov.hmrc.customsservicestatus.models
import uk.gov.hmrc.customsservicestatus.models.State
import uk.gov.hmrc.customsservicestatus.models.config.Services

class CustomsServiceStatusControllerISpec extends BaseISpec {

  val testController: TestController = app.injector.instanceOf[TestController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(callRoute(fakeRequest(uk.gov.hmrc.customsservicestatus.controllers.test.routes.TestController.clearAllData)))
  }

  "GET /services" should {
    "return Ok with empty list if there are services configured, but no corresponding entries in the db" in {

      val result = callRoute(fakeRequest(routes.CustomsServiceStatusController.list()))
      status(result) shouldBe (OK)
      val servicesStatus = contentAsJson(result).as[models.Services].services
      servicesStatus.size                    shouldBe 1
      servicesStatus.head.name               shouldBe "Haulier"
      servicesStatus.head.status.state       shouldBe Some("UNKNOWN")
      servicesStatus.head.status.lastUpdated shouldBe None
    }

    "return Ok with one service in the response if it is configured and have a corresponding entry in the db" in {
      val insertEntry = callRoute(
        fakeRequest(routes.CustomsServiceStatusController.updateServiceStatus("haulier"))
          .withMethod("PUT")
          .withJsonBody(Json.toJson(State("AVAILABLE"))))
      status(insertEntry) shouldBe (OK)
      val result = callRoute(fakeRequest(routes.CustomsServiceStatusController.list()))
      status(result) shouldBe (OK)
      val servicesStatus = contentAsJson(result).as[models.Services].services
      servicesStatus.size                              shouldBe 1
      servicesStatus.head.name                         shouldBe "Haulier"
      servicesStatus.head.status.state                 shouldBe Some("AVAILABLE")
      servicesStatus.head.status.lastUpdated.isDefined shouldBe true
    }

  }
}
