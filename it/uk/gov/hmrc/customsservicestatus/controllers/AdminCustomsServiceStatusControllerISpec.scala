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
import uk.gov.hmrc.customsservicestatus.controllers.test.routes as testRoutes
import uk.gov.hmrc.customsservicestatus.controllers.test.TestController
import uk.gov.hmrc.customsservicestatus.helpers.BaseISpec
import uk.gov.hmrc.customsservicestatus.models.{OutageData, OutageType}
import uk.gov.hmrc.customsservicestatus.models.OutageType.*
import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.customsservicestatus.models.DetailType.*

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AdminCustomsServiceStatusControllerISpec extends BaseISpec {

  val testController: TestController = app.injector.instanceOf[TestController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(callRoute(fakeRequest(testRoutes.TestController.clearAllData)))
  }

  private val fakeUnplannedOutage = fakeOutageData(Unplanned, None)
  private val fakePlannedOutage   = fakeOutageData(Planned, Some(fakeDate))

  "POST /services/messages" should {
    "return None if the information insert to the database was acknowledged (unplanned)" in {
      val result =
        await(
          callRoute(
            fakeRequest(routes.AdminCustomsServiceStatusController.updateWithOutageData()).withBody(Json.toJson(fakeUnplannedOutage))
          )
        )

      val findResult = callRoute(fakeRequest(testRoutes.TestController.list()))

      result.header.status                                shouldBe OK
      status(findResult)                                  shouldBe OK
      contentAsJson(findResult).as[List[OutageData]].head shouldBe fakeUnplannedOutage
    }

    "return None if the information insert to the database was acknowledged (planned)" in {
      val result =
        await(
          callRoute(
            fakeRequest(routes.AdminCustomsServiceStatusController.updateWithOutageData())
              .withBody(Json.toJson(fakePlannedOutage))
          )
        )

      val findResult = callRoute(fakeRequest(testRoutes.TestController.list()))

      result.header.status                                shouldBe OK
      status(findResult)                                  shouldBe OK
      contentAsJson(findResult).as[List[OutageData]].head shouldBe fakePlannedOutage
    }

    "return a 400 if the information insert was unsuccessful" in {
      val result = callRoute(
        fakeRequest(routes.AdminCustomsServiceStatusController.updateWithOutageData()).withBody(
          Json.obj(
            "invalid" -> "object"
          )
        )
      )
      status(result) shouldBe BAD_REQUEST
    }
  }

  "GET /services/planned-work" should {
    "return Ok with empty list" when {
      "there are no corresponding entries in the db" in {

        val result      = callRoute(fakeRequest(routes.AdminCustomsServiceStatusController.getAllPlannedWorks()))
        val plannedWork = contentAsJson(result).as[List[OutageData]]

        status(result) shouldBe OK
        plannedWork    shouldBe List()
      }
    }

    "return Ok with a list of 2 valid planned outages" when {
      "given three outages and an end date in the past" in {
        fakePlannedWorks.map(o =>
          await(callRoute(fakeRequest(routes.AdminCustomsServiceStatusController.updateWithOutageData()).withBody(Json.toJson(o))))
        )

        val result      = callRoute(fakeRequest(routes.AdminCustomsServiceStatusController.getAllPlannedWorks()))
        val plannedWork = contentAsJson(result).as[List[OutageData]]
        val allEntries  = testController.list()(fakeRequest(testRoutes.TestController.list()))

        status(result)                                      shouldBe OK
        contentAsJson(allEntries).as[List[OutageData]].size shouldBe 4
        plannedWork.size                                    shouldBe 3
      }
    }
  }
}
