/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, stubControllerComponents}
import uk.gov.hmrc.customsservicestatus.errorhandlers.AdminCustomsServiceStatusInsertError
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.DetailType.*
import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.customsservicestatus.models.OutageType.Unplanned

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

class AdminCustomsServiceStatusControllerSpec extends BaseSpec {
  val controller = new AdminCustomsServiceStatusController(mockAdminCustomsStatusService, stubControllerComponents())

  val validOutageData: OutageData = OutageData(
    id = UUID.randomUUID(),
    outageType = Unplanned,
    internalReference = InternalReference("Test reference"),
    startDateTime = Instant.parse("2025-01-01T00:00:00.000Z"),
    endDateTime = None,
    details = Details("Test details"),
    publishedDateTime = Instant.parse("2025-01-01T00:00:00.000Z"),
    clsNotes = Some("Notes for CLS users")
  )

  "submitUnplannedOutage" should {
    "validate a correct request json and call the service with a valid case class instance" in {
      when(mockAdminCustomsStatusService.submitOutage(any())).thenReturn(Future.successful(Right(())))
      val result =
        controller.updateWithOutageData()(FakeRequest().withBody(Json.toJson[OutageData](validOutageData)))
      status(result) shouldBe OK
    }
    "return an InternalServerError status when the service returns an error" in {
      when(mockAdminCustomsStatusService.submitOutage(any())).thenReturn(Future.successful(Left(AdminCustomsServiceStatusInsertError)))
      val result =
        controller.updateWithOutageData()(FakeRequest().withBody(Json.toJson[OutageData](validOutageData)))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "getLatestOutage" should {
    "return correct json for an UnplannedOutageData" in {
      when(mockAdminCustomsStatusService.getLatestOutage).thenReturn(Future.successful(Some(validOutageData)))
      val result = controller.getLatestOutage()(FakeRequest().withBody(Json.toJson[OutageData](validOutageData)))
      status(result) shouldBe OK
    }
    "return 404 when there is no UnplannedOutageData" in {
      when(mockAdminCustomsStatusService.getLatestOutage).thenReturn(Future.successful(None))
      val result = controller.getLatestOutage()(FakeRequest().withBody(None))
      status(result) shouldBe NOT_FOUND
    }
  }
}
