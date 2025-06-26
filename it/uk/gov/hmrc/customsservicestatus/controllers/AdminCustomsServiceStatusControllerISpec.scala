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
import uk.gov.hmrc.customsservicestatus.models.UnplannedOutageData
import uk.gov.hmrc.customsservicestatus.models.DetailType.*

import java.time.Instant

class AdminCustomsServiceStatusControllerISpec extends BaseISpec {

  val testController: TestController = app.injector.instanceOf[TestController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(callRoute(fakeRequest(uk.gov.hmrc.customsservicestatus.controllers.test.routes.TestController.clearAllData)))
  }

  private val validUnplannedOutageData: UnplannedOutageData = UnplannedOutageData(
    InternalReference("Testing reference"),
    Preview("Testing additional details"),
    Instant.parse("2025-01-01T00:00:00.000Z"),
    None
  )

  "POST /services/messages" should {
    "return None if the information insert to the database was acknowledged" in {
      val result =
        await(
          callRoute(
            fakeRequest(routes.AdminCustomsServiceStatusController.updateWithUnplannedOutage()).withBody(Json.toJson(validUnplannedOutageData))
          )
        )

      val findResult = callRoute(fakeRequest(routes.AdminCustomsServiceStatusController.list()))

      result.header.status                                    shouldBe OK
      status(findResult)                                      shouldBe OK
      contentAsJson(findResult).as[List[UnplannedOutageData]] shouldBe List(validUnplannedOutageData)
    }

    "return a 400 if the information insert was unsuccessful" in {
      val result = callRoute(
        fakeRequest(routes.AdminCustomsServiceStatusController.updateWithUnplannedOutage()).withBody(
          Json.obj(
            "invalid" -> "object"
          )
        )
      )
      status(result) shouldBe BAD_REQUEST
    }

  }
}
