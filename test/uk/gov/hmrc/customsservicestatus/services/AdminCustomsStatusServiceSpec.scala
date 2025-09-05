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

import com.mongodb.client.result.{DeleteResult, InsertOneResult}
import org.bson.BsonValue
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.customsservicestatus.errorhandlers.OutageError
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.customsservicestatus.models.OutageType.*

import java.util.UUID
import scala.concurrent.Future

class AdminCustomsStatusServiceSpec extends BaseSpec {
  trait Setup {
    val service = new AdminCustomsStatusService(mockConfig, mockAdminCustomsServiceStatusRepository, mockArchivedOutagesRepository)

    case class acknowledgedInsertOneResult(isAcknowledged: Boolean = true) extends InsertOneResult {
      override def wasAcknowledged(): Boolean = isAcknowledged

      override def getInsertedId: BsonValue = null
    }

    case class acknowledgedDeleteResult(isAcknowledged: Boolean = true) extends DeleteResult {
      override def wasAcknowledged(): Boolean = isAcknowledged

      override def getDeletedCount: Long = 0
    }
  }

  "submitOutage" should {
    "return a Right containing Unit" when {
      "valid unplanned outage data parsed" in new Setup {
        when(mockAdminCustomsServiceStatusRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult()))
        val result: Future[Either[OutageError, Unit]] = service.submitOutage(fakeOutageData(Unplanned, None))
        result.futureValue shouldBe Right(())
      }

      "valid planned outage data parsed" in new Setup {
        when(mockAdminCustomsServiceStatusRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult()))
        val result: Future[Either[OutageError, Unit]] = service.submitOutage(fakeOutageData(Planned, Some(fakeDate)))
        result.futureValue shouldBe Right(())
      }
    }

    "return a Left with an error if the insert was not acknowledged" when {
      "valid unplanned outage data parsed" in new Setup {
        when(mockAdminCustomsServiceStatusRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult(false)))
        val result: Future[Either[OutageError, Unit]] = service.submitOutage(fakeOutageData(Unplanned, None))
        result.futureValue shouldBe Left(OutageError.OutageInsertError)
      }

      "valid planned outage data parsed" in new Setup {
        when(mockAdminCustomsServiceStatusRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult(false)))
        val result: Future[Either[OutageError, Unit]] = service.submitOutage(fakeOutageData(Planned, Some(fakeDate)))
        result.futureValue shouldBe Left(OutageError.OutageInsertError)
      }
    }
  }

  "getAllPlannedWorks" should {
    "return a list of planned outages" in new Setup {
      when(mockAdminCustomsServiceStatusRepository.findAllPlanned()).thenReturn(Future.successful(fakePlannedWorks))
      val result: Future[Seq[OutageData]] = service.getAllPlannedWorks
      result.futureValue shouldBe fakePlannedWorks
    }
  }

  "findAllOutages" should {
    "return a list of outages" when {
      "the database has a mix of valid outage data" in new Setup {
        when(mockAdminCustomsServiceStatusRepository.findAll()).thenReturn(Future.successful(fakePlannedWorks ++ fakeUnplannedWorks))
        val result: Future[List[OutageData]] = service.findAllOutages()
        result.futureValue shouldBe fakePlannedWorks ++ fakeUnplannedWorks
      }

      "the database is empty" in new Setup {
        when(mockAdminCustomsServiceStatusRepository.findAll()).thenReturn(Future.successful(List.empty))
        val result: Future[List[OutageData]] = service.findAllOutages()
        result.futureValue shouldBe List.empty
      }
    }
  }

  "findOutage" should {
    "return the correct outage" when {
      "given a matching id" in new Setup {
        val outage: OutageData = fakeOutageData(Unplanned, None).copy(id = UUID.randomUUID())
        when(mockAdminCustomsServiceStatusRepository.find(any())).thenReturn(Future.successful(Some(outage)))
        val result: Future[Option[OutageData]] = service.findOutage(outage.id)
        result.futureValue shouldBe Some(outage)
      }
    }
    "return None" when {
      "given a non-matching id" in new Setup {
        when(mockAdminCustomsServiceStatusRepository.find(any())).thenReturn(Future.successful(None))
        val result: Future[Option[OutageData]] = service.findOutage(UUID.randomUUID())
        result.futureValue shouldBe None
      }
    }
  }

  "archiveOutage" should {
    "return the relevant outage after archiving it" when {
      "given a matching id" in new Setup {
        val outage: OutageData = fakePlannedWorks.head.copy(id = UUID.randomUUID())
        when(mockAdminCustomsServiceStatusRepository.find(any())).thenReturn(Future.successful(Some(outage)))
        when(mockArchivedOutagesRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult()))
        when(mockAdminCustomsServiceStatusRepository.delete(any())).thenReturn(Future.successful(acknowledgedDeleteResult()))
        val result: Future[Option[OutageData]] = service.archiveOutage(outage.id)
        result.futureValue shouldBe Some(outage)
      }
    }
    "return None if the outage does not exist in the database" in new Setup {
      val outage: OutageData = fakePlannedWorks.head.copy(id = UUID.randomUUID())
      when(mockAdminCustomsServiceStatusRepository.find(any())).thenReturn(Future.successful(None))
      when(mockArchivedOutagesRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult()))
      when(mockAdminCustomsServiceStatusRepository.delete(any())).thenReturn(Future.successful(acknowledgedDeleteResult()))
      val result: Future[Option[OutageData]] = service.archiveOutage(outage.id)
      result.futureValue shouldBe None
    }

    "return None if the outage was not inserted into the archive repository" in new Setup {
      val outage: OutageData = fakePlannedWorks.head.copy(id = UUID.randomUUID())
      when(mockAdminCustomsServiceStatusRepository.find(any())).thenReturn(Future.successful(Some(outage)))
      when(mockArchivedOutagesRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult(false)))
      val result: Future[Option[OutageData]] = service.archiveOutage(outage.id)
      result.futureValue shouldBe None
    }
  }
}
