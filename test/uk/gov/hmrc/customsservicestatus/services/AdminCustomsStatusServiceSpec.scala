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
import uk.gov.hmrc.customsservicestatus.TestData.*
import uk.gov.hmrc.customsservicestatus.errorhandlers.OutageError
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.customsservicestatus.models.OutageType.*
import uk.gov.hmrc.customsservicestatus.factories.OutageDataFactory.*

import java.util.UUID
import scala.concurrent.Future

class AdminCustomsStatusServiceSpec extends BaseSpec {
  trait Setup {
    val service = new AdminCustomsStatusService(mockConfig, mockOutagesRepository, mockArchivedOutagesRepository)

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
        when(mockOutagesRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult()))
        val result: Future[Either[OutageError, Unit]] = service.submitOutage(fakeOutageData(outageType = Unplanned))
        result.futureValue shouldBe Right(())
      }

      "valid planned outage data parsed" in new Setup {
        when(mockOutagesRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult()))
        val result: Future[Either[OutageError, Unit]] = service.submitOutage(fakeOutageData(outageType = Planned, endDateTime = Some(fakeDate)))
        result.futureValue shouldBe Right(())
      }
    }

    "return a Left with an error if the insert was not acknowledged" when {
      "valid unplanned outage data parsed" in new Setup {
        when(mockOutagesRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult(false)))
        val result: Future[Either[OutageError, Unit]] = service.submitOutage(fakeOutageData(outageType = Unplanned))
        result.futureValue shouldBe Left(OutageError.OutageInsertError)
      }

      "valid planned outage data parsed" in new Setup {
        when(mockOutagesRepository.submitOutage(any())).thenReturn(Future.successful(acknowledgedInsertOneResult(false)))
        val result: Future[Either[OutageError, Unit]] = service.submitOutage(fakeOutageData(outageType = Planned, endDateTime = Some(fakeDate)))
        result.futureValue shouldBe Left(OutageError.OutageInsertError)
      }
    }
  }

  "getLatestOutage" should {
    "return the latest unplanned outage" in new Setup {
      val validOutageData = fakeOutageData(outageType = Unplanned)
      when(mockOutagesRepository.getLatest(outageType = Unplanned)).thenReturn(Future.successful(validOutageData))
      val result: Future[Option[OutageData]] = service.getLatestOutage(outageType = Unplanned)
      result.futureValue shouldBe validOutageData
    }

    "return the latest planned outage" in new Setup {
      val validOutageData = fakeOutageData(outageType = Planned)
      when(mockOutagesRepository.getLatest(outageType = Planned)).thenReturn(Future.successful(validOutageData))
      val result: Future[Option[OutageData]] = service.getLatestOutage(outageType = Planned)
      result.futureValue shouldBe validOutageData
    }

    "return 404 where there is no unplanned outage" in new Setup {
      when(mockOutagesRepository.getLatest(outageType = Unplanned)).thenReturn(Future.successful(None))
      val result: Future[Option[OutageData]] = service.getLatestOutage(outageType = Unplanned)
      result.futureValue shouldBe None
    }

    "return 404 where there is no planned outage" in new Setup {
      when(mockOutagesRepository.getLatest(outageType = Planned)).thenReturn(Future.successful(None))
      val result: Future[Option[OutageData]] = service.getLatestOutage(outageType = Planned)
      result.futureValue shouldBe None
    }
  }

  "getAllPlannedWorks" should {
    "return a list of planned outages" in new Setup {
      when(mockOutagesRepository.findAllPlanned()).thenReturn(Future.successful(fakePlannedWorks))
      val result: Future[Seq[OutageData]] = service.getAllPlannedWorks
      result.futureValue shouldBe fakePlannedWorks
    }
  }

  "findAllOutages" should {
    "return a list of outages" when {
      "the database has a mix of valid outage data" in new Setup {
        when(mockOutagesRepository.findAll()).thenReturn(Future.successful(fakePlannedWorks ++ fakeUnplannedWorks))
        val result: Future[List[OutageData]] = service.findAllOutages()
        result.futureValue shouldBe fakePlannedWorks ++ fakeUnplannedWorks
      }

      "the database is empty" in new Setup {
        when(mockOutagesRepository.findAll()).thenReturn(Future.successful(List.empty))
        val result: Future[List[OutageData]] = service.findAllOutages()
        result.futureValue shouldBe List.empty
      }
    }
  }

  "findOutage" should {
    "return the correct outage" when {
      "given a matching id" in new Setup {
        val outage: OutageData = fakeOutageData(outageType = Unplanned).copy(id = UUID.randomUUID())
        when(mockOutagesRepository.find(any())).thenReturn(Future.successful(Some(outage)))
        val result: Future[Option[OutageData]] = service.findOutage(outage.id)
        result.futureValue shouldBe Some(outage)
      }
    }
    "return None" when {
      "given a non-matching id" in new Setup {
        when(mockOutagesRepository.find(any())).thenReturn(Future.successful(None))
        val result: Future[Option[OutageData]] = service.findOutage(UUID.randomUUID())
        result.futureValue shouldBe None
      }
    }
  }

  "archiveOutage" should {
    "return the relevant outage after archiving it when given a matching id" in new Setup {
      val outage: OutageData = fakePlannedWorks.head.copy(id = UUID.randomUUID())
      when(mockOutagesRepository.find(any())).thenReturn(Future.successful(Some(outage)))
      when(mockArchivedOutagesRepository.insert(any())).thenReturn(Future.successful(acknowledgedInsertOneResult()))
      when(mockOutagesRepository.delete(any())).thenReturn(Future.successful(acknowledgedDeleteResult()))
      val result: Future[Either[OutageError, OutageData]] = service.archiveOutage(outage.id)
      result.futureValue shouldBe Right(outage)
    }

    "return an error if the outage does not exist in the database" in new Setup {
      val outage: OutageData = fakePlannedWorks.head.copy(id = UUID.randomUUID())
      when(mockOutagesRepository.find(any())).thenReturn(Future.successful(None))
      when(mockArchivedOutagesRepository.insert(any())).thenReturn(Future.successful(acknowledgedInsertOneResult()))
      when(mockOutagesRepository.delete(any())).thenReturn(Future.successful(acknowledgedDeleteResult()))
      val result = service.archiveOutage(outage.id)
      result.futureValue shouldBe Left(OutageError.OutageArchiveError)
    }

    "return an error if the outage was not inserted into the archive repository" in new Setup {
      val outage: OutageData = fakePlannedWorks.head.copy(id = UUID.randomUUID())
      when(mockOutagesRepository.find(any())).thenReturn(Future.successful(Some(outage)))
      when(mockArchivedOutagesRepository.insert(any())).thenReturn(Future.successful(acknowledgedInsertOneResult(false)))
      val result = service.archiveOutage(outage.id)
      result.futureValue shouldBe Left(OutageError.OutageArchiveInsertError)
    }
  }
}
