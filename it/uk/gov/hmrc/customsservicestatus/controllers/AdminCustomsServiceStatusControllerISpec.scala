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
import uk.gov.hmrc.customsservicestatus.models.DetailType.*

import java.time.Instant
import java.util.UUID

class AdminCustomsServiceStatusControllerISpec extends BaseISpec {

  val testController: TestController = app.injector.instanceOf[TestController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(callRoute(fakeRequest(testRoutes.TestController.clearAllData)))
  }

  def fakeOutage(outageType: OutageType, endDateTime: Option[Instant]): OutageData = OutageData(
    id = fakeId,
    outageType = outageType,
    internalReference = InternalReference("Test reference"),
    startDateTime = Instant.parse("2025-01-01T00:00:00.000Z"),
    endDateTime = endDateTime,
    details = Details("Test details"),
    publishedDateTime = Instant.parse("2025-01-01T00:00:00.000Z"),
    clsNotes = Some("Notes for CLS users")
  )

  val fakeId: UUID = UUID.randomUUID()

  val fakeDate: Instant = Instant.parse("2027-01-01T00:00:00.000Z")

  "POST /services/messages" should {
    "return None if the information insert to the database was acknowledged (unplanned)" in {
      val result =
        await(
          callRoute(
            fakeRequest(routes.AdminCustomsServiceStatusController.updateWithOutageData()).withBody(Json.toJson(fakeOutage(Unplanned, None)))
          )
        )

      val findResult = callRoute(fakeRequest(testRoutes.TestController.list()))

      result.header.status                           shouldBe OK
      status(findResult)                             shouldBe OK
      contentAsJson(findResult).as[List[OutageData]] shouldBe List(fakeOutage(Unplanned, None))
    }

    "return None if the information insert to the database was acknowledged (planned)" in {
      val result =
        await(
          callRoute(
            fakeRequest(routes.AdminCustomsServiceStatusController.updateWithOutageData()).withBody(Json.toJson(fakeOutage(Planned, Some(fakeDate))))
          )
        )

      val findResult = callRoute(fakeRequest(testRoutes.TestController.list()))

      result.header.status                           shouldBe OK
      status(findResult)                             shouldBe OK
      contentAsJson(findResult).as[List[OutageData]] shouldBe List(fakeOutage(Planned, Some(fakeDate)))
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
}
