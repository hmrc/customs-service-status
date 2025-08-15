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
import uk.gov.hmrc.customsservicestatus.errorhandlers.OutageError
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.OutageType.*
import uk.gov.hmrc.customsservicestatus.models.OutageData

import scala.concurrent.Future

class AdminCustomsServiceStatusControllerSpec extends BaseSpec {

  val controller = new AdminCustomsServiceStatusController(mockAdminCustomsStatusService, stubControllerComponents())

  "submitOutage" should {
    "validate a correct request json and call the service" when {
      "a valid unplanned outage instance parsed" in {
        when(mockAdminCustomsStatusService.submitOutage(any())).thenReturn(Future.successful(Right(())))
        val result = controller.updateWithOutageData()(FakeRequest().withBody(Json.toJson[OutageData](fakeOutageData(Unplanned, None))))
        status(result) shouldBe OK
      }

      "a valid planned outage instance parsed" in {
        when(mockAdminCustomsStatusService.submitOutage(any())).thenReturn(Future.successful(Right(())))
        val result = controller.updateWithOutageData()(
          FakeRequest().withBody(Json.toJson[OutageData](fakeOutageData(Planned, Some(fakeDate))))
        )
        status(result) shouldBe OK
      }
    }

    "return an InternalServerError status when the service returns an error" when {
      "a valid unplanned outage instance parsed" in {
        when(mockAdminCustomsStatusService.submitOutage(any())).thenReturn(Future.successful(Left(OutageError.OutageInsertError)))
        val result = controller.updateWithOutageData()(FakeRequest().withBody(Json.toJson[OutageData](fakeOutageData(Unplanned, None))))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "a valid planned outage instance parsed" in {
        when(mockAdminCustomsStatusService.submitOutage(any())).thenReturn(Future.successful(Left(OutageError.OutageInsertError)))
        val result = controller.updateWithOutageData()(FakeRequest().withBody(Json.toJson[OutageData](fakeOutageData(Planned, Some(fakeDate)))))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "getAllPlannedWorks" should {
    "return OK and call the PlannedWorkService" in {

      when(mockAdminCustomsStatusService.getAllPlannedWorks).thenReturn(Future[List[OutageData]](fakePlannedWorks))

      val result = controller.getAllPlannedWorks(FakeRequest())
      status(result) shouldBe OK
    }
  }
}
