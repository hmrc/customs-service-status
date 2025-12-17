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
import play.api.test.Helpers.*
import uk.gov.hmrc.customsservicestatus.TestData.*
import uk.gov.hmrc.customsservicestatus.errorhandlers.OutageError.*
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.OutageType.*
import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.customsservicestatus.errorhandlers.OutageError
import uk.gov.hmrc.customsservicestatus.factories.OutageDataFactory.*

import scala.concurrent.Future

class AdminCustomsServiceStatusControllerSpec extends BaseSpec {

  val controller = new AdminCustomsServiceStatusController(mockAdminCustomsStatusService, stubControllerComponents())

  private val fakeUnplannedOutage: OutageData = fakeOutageData(outageType = Unplanned)

  "submitOutage" should {
    "validate a correct request json and call the service" when {
      "a valid unplanned outage instance parsed" in {
        when(mockAdminCustomsStatusService.submitOutage(any())).thenReturn(Future.successful(Right(())))
        val result = controller.updateWithOutageData()(FakeRequest().withBody(Json.toJson[OutageData](fakeOutageData(outageType = Unplanned))))
        status(result) shouldBe OK
      }

      "a valid planned outage instance parsed" in {
        when(mockAdminCustomsStatusService.submitOutage(any())).thenReturn(Future.successful(Right(())))
        val result = controller.updateWithOutageData()(
          FakeRequest().withBody(Json.toJson[OutageData](fakeOutageData(outageType = Planned, endDateTime = Some(fakeDate))))
        )
        status(result) shouldBe OK
      }
    }

    "return an InternalServerError status when the service returns an error" when {
      "a valid unplanned outage instance parsed" in {
        when(mockAdminCustomsStatusService.submitOutage(any())).thenReturn(Future.successful(Left(OutageError.OutageInsertError)))
        val result = controller.updateWithOutageData()(FakeRequest().withBody(Json.toJson[OutageData](fakeOutageData(outageType = Unplanned))))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "a valid planned outage instance parsed" in {
        when(mockAdminCustomsStatusService.submitOutage(any())).thenReturn(Future.successful(Left(OutageError.OutageInsertError)))
        val result = controller.updateWithOutageData()(
          FakeRequest().withBody(Json.toJson[OutageData](fakeOutageData(outageType = Planned, endDateTime = Some(fakeDate))))
        )
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "getAllPlannedWorks" should {
    "return OK and call the PlannedWorkService" in {
      when(mockAdminCustomsStatusService.getAllPlannedWorks).thenReturn(Future[List[OutageData]](fakePlannedWorks))
      val result = controller.getAllPlannedWorks(FakeRequest())
      status(result)                             shouldBe OK
      contentAsJson(result).as[List[OutageData]] shouldBe fakePlannedWorks
    }
  }

  "findAllOutages" should {
    "return OK and a list of outages" in {
      when(mockAdminCustomsStatusService.findAllOutages()).thenReturn(Future[List[OutageData]](fakePlannedWorks ++ fakeUnplannedWorks))
      val result = controller.findAllOutages()(FakeRequest())
      contentAsJson(result).as[List[OutageData]] shouldBe fakePlannedWorks ++ fakeUnplannedWorks
      status(result)                             shouldBe OK
    }
  }

  "getLatestOutage" should {
    "return correct json for an UnplannedOutageData" in {
      when(mockAdminCustomsStatusService.getLatestOutage(outageType = Unplanned)).thenReturn(Future.successful(Some(fakeUnplannedOutage)))
      val result = controller.getLatestOutage(outageType = Unplanned)(FakeRequest().withBody(Json.toJson[OutageData](fakeUnplannedOutage)))
      status(result) shouldBe OK
    }
    "return 404 when there is no UnplannedOutageData" in {
      when(mockAdminCustomsStatusService.getLatestOutage(outageType = Unplanned)).thenReturn(Future.successful(None))
      val result = controller.getLatestOutage(outageType = Unplanned)(FakeRequest().withBody(None))
      status(result) shouldBe NOT_FOUND
    }
  }

  "findOutage" should {
    "return OK and the matching outage if found" in {
      when(mockAdminCustomsStatusService.findOutage(fakePlannedWorks.head.id)).thenReturn(Future[Option[OutageData]](Some(fakePlannedWorks.head)))
      val result = controller.findOutage(fakePlannedWorks.head.id)(fakeRequest)
      status(result)                          shouldBe OK
      contentAsJson(result).asOpt[OutageData] shouldBe Some(fakePlannedWorks.head)
    }
    "return OK and the matching outage if not found" in {
      when(mockAdminCustomsStatusService.findOutage(fakePlannedWorks.head.id)).thenReturn(Future[Option[OutageData]](None))
      val result = controller.findOutage(fakePlannedWorks.head.id)(fakeRequest)
      status(result)                          shouldBe OK
      contentAsJson(result).asOpt[OutageData] shouldBe None
    }
  }

  "archiveOutage" should {
    "return OK and the outage related to the id that was archived" in {
      when(mockAdminCustomsStatusService.archiveOutage(fakeUnplannedWorks.head.id))
        .thenReturn(Future(Right(fakeUnplannedWorks.head)))
      val result = controller.archiveOutage(fakeUnplannedWorks.head.id)(fakeRequest)
      status(result)                       shouldBe OK
      contentAsJson(result).as[OutageData] shouldBe fakeUnplannedWorks.head
    }
    "return a 500 if the outage was not found or could not be added to the archived collection" in {
      when(mockAdminCustomsStatusService.archiveOutage(fakeUnplannedWorks.head.id)).thenReturn(Future(Left(OutageArchiveError)))
      val result = controller.archiveOutage(fakeUnplannedWorks.head.id)(fakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
