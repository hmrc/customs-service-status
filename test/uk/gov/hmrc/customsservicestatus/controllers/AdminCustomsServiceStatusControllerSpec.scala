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
import uk.gov.hmrc.customsservicestatus.errorhandlers.AdminCustomsServiceStatusError.GenericError
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.DetailType.*
import uk.gov.hmrc.customsservicestatus.models.UnplannedOutageData

import java.time.Instant
import scala.concurrent.Future

class AdminCustomsServiceStatusControllerSpec extends BaseSpec {
  val controller = new AdminCustomsServiceStatusController(mockAdminCustomsStatusService, stubControllerComponents())

  val validUnplannedOutageData: UnplannedOutageData = UnplannedOutageData(
    InternalReference("Test reference"),
    Preview("Test details"),
    Instant.now(),
    None
  )

  "AdminCustomsServiceStatusController" should {
    "validate a correct request json and call the service with a valid case class instance" in {
      when(mockAdminCustomsStatusService.submitUnplannedOutage(any())).thenReturn(Future.successful(None))
      val result =
        controller.updateWithUnplannedOutage()(FakeRequest().withBody(Json.toJson[UnplannedOutageData](validUnplannedOutageData)))
      status(result) shouldBe OK
    }
    "return a bad request status when the service returns an error" in {
      when(mockAdminCustomsStatusService.submitUnplannedOutage(any())).thenReturn(Future.successful(Some(GenericError)))
      val result =
        controller.updateWithUnplannedOutage()(FakeRequest().withBody(Json.toJson[UnplannedOutageData](validUnplannedOutageData)))
      status(result) shouldBe BAD_REQUEST
    }
  }
}
