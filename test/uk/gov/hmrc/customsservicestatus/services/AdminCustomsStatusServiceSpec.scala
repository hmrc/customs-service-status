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

package uk.gov.hmrc.customsservicestatus.services

import com.mongodb.client.result.InsertOneResult
import org.bson.BsonValue
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.customsservicestatus.errorhandlers.AdminCustomsServiceStatusInsertError
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.DetailType.*
import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.customsservicestatus.models.OutageType.Unplanned

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

class AdminCustomsStatusServiceSpec extends BaseSpec {
  trait Setup {
    val service = new AdminCustomsStatusService(mockConfig, mockAdminCustomsServiceStatusRepository)

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

    case class acknowledgedInsertOneResult(isAcknowledged: Boolean = true) extends InsertOneResult {
      override def wasAcknowledged(): Boolean   = isAcknowledged
      override def getInsertedId:     BsonValue = null
    }
  }

  "submitPlannedOutage" should {
    "return a Right containing Unit given valid unplanned outage data" in new Setup {
      when(mockAdminCustomsServiceStatusRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult()))
      val result: Future[Either[AdminCustomsServiceStatusInsertError.type, Unit]] = service.submitOutage(validOutageData)
      result.futureValue shouldBe Right(())
    }

    "return a Left with an error if the insert was not acknowledged" in new Setup {
      when(mockAdminCustomsServiceStatusRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult(false)))
      val result: Future[Either[AdminCustomsServiceStatusInsertError.type, Unit]] = service.submitOutage(validOutageData)
      result.futureValue shouldBe Left(AdminCustomsServiceStatusInsertError)
    }
  }

  "getLatestOutage" should {
    "return the latest UnplannedOutageData" in new Setup {
      when(mockAdminCustomsServiceStatusRepository.getLatest(outageType = Unplanned)).thenReturn(Future.successful(validOutageData))
      val result: Future[Option[OutageData]] = service.getLatestOutage(outageType = Unplanned)
      result.futureValue shouldBe validOutageData
    }
    "return 404 where there is no UnplannedOutageData" in new Setup {
      when(mockAdminCustomsServiceStatusRepository.getLatest(outageType = Unplanned)).thenReturn(Future.successful(None))
      val result: Future[Option[OutageData]] = service.getLatestOutage(outageType = Unplanned)
      result.futureValue shouldBe None
    }
  }
}
