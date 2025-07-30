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
import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.customsservicestatus.models.OutageType.Unplanned

import java.time.Instant
import java.util.UUID

class AdminCustomsServiceStatusRepositoryISpec extends BaseISpec {

  val testController: TestController = app.injector.instanceOf[TestController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(testController.clearAllData(fakeRequest(testRoutes.TestController.clearAllData)))
  }

  private val adminCustomsServiceStatusRepository: AdminCustomsServiceStatusRepository = app.injector.instanceOf[AdminCustomsServiceStatusRepository]

  private val validOutageData: OutageData = OutageData(
    id = UUID.randomUUID(),
    outageType = Unplanned,
    internalReference = InternalReference("Test reference"),
    startDateTime = Instant.now(),
    endDateTime = None,
    details = Details("Test details"),
    publishedDateTime = Instant.now(),
    clsNotes = Some("Notes for CLS users")
  )

  private val anotherValidOutageData: OutageData = validOutageData.copy(id = UUID.randomUUID())

  "submitUnplannedOutage" should {
    "create an entry in the database with a valid request" in {
      val result = await(adminCustomsServiceStatusRepository.submitOutage(validOutageData))
      result.wasAcknowledged() shouldBe true
    }
  }

  "findAll" should {
    "return empty list if no record in the db" in {
      val result = await(adminCustomsServiceStatusRepository.findAll())
      result.size shouldBe 0
    }

    "return all the customsServiceStatus entries in the database" in {
      await(adminCustomsServiceStatusRepository.submitOutage(validOutageData))
      await(adminCustomsServiceStatusRepository.submitOutage(anotherValidOutageData))
      val result = await(adminCustomsServiceStatusRepository.findAll())
      result.size shouldBe 2
    }
  }
}
