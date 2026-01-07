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
import uk.gov.hmrc.customsservicestatus.models.OutageType.*
import uk.gov.hmrc.customsservicestatus.TestData.*
import uk.gov.hmrc.customsservicestatus.factories.OutageDataFactory.*

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class OutagesRepositoryISpec extends BaseISpec {

  val testController: TestController = app.injector.instanceOf[TestController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(testController.clearAllData(fakeRequest(testRoutes.TestController.clearAllData)))
  }

  private val outagesRepository: OutagesRepository = app.injector.instanceOf[OutagesRepository]

  private val unplannedOutage: OutageData = fakeOutageData(outageType = Unplanned)
  private val plannedOutage: OutageData =
    fakeOutageData(outageType = Planned, endDateTime = Some(futureTestDate))

  "submitOutage" should {
    "create an unplanned outage in the database with a valid request" in {
      await(outagesRepository.submitOutage(unplannedOutage))
      val result = testController.list()(fakeRequest(testRoutes.TestController.list()))
      contentAsJson(result).as[List[OutageData]] shouldBe List(unplannedOutage)
    }

    "create a planned outage in the database with a valid request" in {
      await(outagesRepository.submitOutage(plannedOutage))
      val result = testController.list()(fakeRequest(testRoutes.TestController.list()))
      contentAsJson(result).as[List[OutageData]] shouldBe List(plannedOutage)
    }
  }

  "findAllPlanned" should {
    "return empty list if no record in the db" in {
      val result = await(outagesRepository.findAllPlanned())
      result.size shouldBe 0
    }

    "return an empty list if there are only unplanned outage entries in the database" in {
      await(outagesRepository.submitOutage(unplannedOutage))
      val result = await(outagesRepository.findAllPlanned())
      result.size shouldBe 0
    }

    "return all the outage entries whose end date is in the future" in {
      await(outagesRepository.submitOutage(plannedOutage))
      await(
        outagesRepository.submitOutage(
          plannedOutage.copy(endDateTime = Some(Instant.now().minus(1, ChronoUnit.DAYS))).copy(id = UUID.randomUUID())
        )
      )

      val result = await(outagesRepository.findAllPlanned())
      result.head shouldBe plannedOutage
      result.size shouldBe 1
    }

    "return no outage entries if end dates are in the past" in {
      await(
        outagesRepository.submitOutage(plannedOutage.copy(endDateTime = Some(Instant.now().minus(1, ChronoUnit.DAYS))))
      )

      val result = await(outagesRepository.findAllPlanned())
      result.size shouldBe 0
    }

    "return all the outage entries sorted by their start date" in {
      plannedWorks.map(plannedWork => await(outagesRepository.submitOutage(plannedWork)))
      val result = await(outagesRepository.findAllPlanned())
      result.map(_.startDateTime) shouldBe sorted
    }

    "return all the customsServiceStatus entries in the database" in {
      await(outagesRepository.submitOutage(plannedOutage))
      await(outagesRepository.submitOutage(unplannedOutage))
      val result = await(outagesRepository.findAll())
      result.size shouldBe 2
    }
  }

  "findAll" should {
    "return all the outage entries" in {
      List(plannedOutage, unplannedOutage).map(plannedWork => await(outagesRepository.submitOutage(plannedWork)))
      val result = await(outagesRepository.findAll())
      result should contain allElementsOf List(plannedOutage, unplannedOutage)
    }
  }

  "find" should {
    "return a matching planned outage" in {
      await(outagesRepository.submitOutage(plannedOutage))
      val result = await(outagesRepository.find(plannedOutage.id))
      result shouldBe Some(plannedOutage)
    }

    "return a matching unplanned outage" in {
      await(outagesRepository.submitOutage(unplannedOutage))
      val result = await(outagesRepository.find(unplannedOutage.id))
      result shouldBe Some(unplannedOutage)
    }
  }

  "delete" should {
    "successfully delete an outage if it exists" in {
      await(outagesRepository.submitOutage(unplannedOutage))
      await(outagesRepository.delete(unplannedOutage.id))
      val result = callRoute(fakeRequest(testRoutes.TestController.list()))
      contentAsJson(result).as[List[OutageData]] shouldBe List.empty
    }
  }
}
