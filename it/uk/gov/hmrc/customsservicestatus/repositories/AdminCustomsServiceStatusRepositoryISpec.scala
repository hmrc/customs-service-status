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

package uk.gov.hmrc.customsservicestatus.repositories

import uk.gov.hmrc.customsservicestatus.controllers.test.{TestController, routes as testRoutes}
import uk.gov.hmrc.customsservicestatus.helpers.BaseISpec
import uk.gov.hmrc.customsservicestatus.models.DetailType.*
import uk.gov.hmrc.customsservicestatus.models.{OutageData, OutageType}
import uk.gov.hmrc.customsservicestatus.models.OutageType.{Planned, Unplanned}

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AdminCustomsServiceStatusRepositoryISpec extends BaseISpec {

  val testController: TestController = app.injector.instanceOf[TestController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(testController.clearAllData(fakeRequest(testRoutes.TestController.clearAllData)))
  }

  val adminCustomsServiceStatusRepository: AdminCustomsServiceStatusRepository = app.injector.instanceOf[AdminCustomsServiceStatusRepository]

  private val fakeUnplannedOutage: OutageData = fakeOutageData(Unplanned, None)

  "submitOutage" should {
    "create an unplanned outage in the database with a valid request" in {
      await(adminCustomsServiceStatusRepository.submitOutage(fakePlannedWorks.head))
      val result = testController.list()(fakeRequest(testRoutes.TestController.list()))
      contentAsJson(result).as[List[OutageData]].size shouldBe 1
    }

    "create a planned outage in the database with a valid request" in {
      await(adminCustomsServiceStatusRepository.submitOutage(fakeOutageData(Planned, Some(fakeDate))))
      val result = testController.list()(fakeRequest(testRoutes.TestController.list()))
      contentAsJson(result).as[List[OutageData]].size shouldBe 1
    }
  }

  "findAllPlanned" should {
    "return empty list if no record in the db" in {
      val result = await(adminCustomsServiceStatusRepository.findAllPlanned())
      result.size shouldBe 0
    }

    "return an empty list if there are only unplanned outage entries in the database" in {
      await(adminCustomsServiceStatusRepository.submitOutage(fakeUnplannedOutage))
      val result = await(adminCustomsServiceStatusRepository.findAllPlanned())
      result.size shouldBe 0
    }

    "return all the outage entries whose end date is in the future" in {
      await(adminCustomsServiceStatusRepository.submitOutage(fakeOutageData(Planned, Some(Instant.now().minus(1, ChronoUnit.DAYS)))))
      await(adminCustomsServiceStatusRepository.submitOutage(fakeOutageData(Planned, Some(Instant.now().plus(1, ChronoUnit.DAYS)))))
      await(adminCustomsServiceStatusRepository.submitOutage(fakeUnplannedOutage))
      val result = await(adminCustomsServiceStatusRepository.findAllPlanned())
      result.size shouldBe 1
    }

    "return all the outage entries sorted by their start date" in {
      await(
        adminCustomsServiceStatusRepository.submitOutage(
          fakeOutageData(Planned, Some(Instant.now().plus(7, ChronoUnit.DAYS)), Instant.now().minus(2, ChronoUnit.DAYS))
        )
      )
      await(
        adminCustomsServiceStatusRepository.submitOutage(
          fakeOutageData(Planned, Some(Instant.now().plus(7, ChronoUnit.DAYS)), Instant.now().minus(3, ChronoUnit.DAYS))
        )
      )
      await(
        adminCustomsServiceStatusRepository.submitOutage(
          fakeOutageData(Planned, Some(Instant.now().plus(7, ChronoUnit.DAYS)), Instant.now().minus(1, ChronoUnit.DAYS))
        )
      )
      val result = await(adminCustomsServiceStatusRepository.findAllPlanned())
      result.map(_.startDateTime) shouldBe sorted
    }
  }
}
