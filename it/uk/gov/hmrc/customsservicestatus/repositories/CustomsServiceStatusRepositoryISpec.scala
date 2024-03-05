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

import uk.gov.hmrc.customsservicestatus.controllers.test.{TestController, routes => testRoutes}
import uk.gov.hmrc.customsservicestatus.helpers.BaseISpec
import uk.gov.hmrc.customsservicestatus.models.CustomsServiceStatus
import uk.gov.hmrc.customsservicestatus.models.State.{AVAILABLE, UNAVAILABLE}

import java.time.Instant
import java.time.temporal.ChronoUnit._

class CustomsServiceStatusRepositoryISpec extends BaseISpec {

  val testController: TestController = app.injector.instanceOf[TestController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(testController.clearAllData(fakeRequest(testRoutes.TestController.clearAllData)))
  }

  val customsServiceStatusRepository: CustomsServiceStatusRepository = app.injector.instanceOf[CustomsServiceStatusRepository]

  "updateServiceStatus" should {
    "update the service with given status and lastUpdated" in {
      val serviceId = "Haulier"
      val state     = AVAILABLE
      inside(
        await(
          customsServiceStatusRepository.updateServiceStatus(
            CustomsServiceStatus(serviceId, "name", "description", Some(state), Some(Instant.now()), Some(Instant.now()))
          )
        )
      ) { case result =>
        result.id          shouldBe serviceId
        result.state       shouldBe Some(state)
        result.lastUpdated shouldBe defined
      }
    }

    "not update stateChangedAt when the state has not changed" in {
      val serviceId     = "Haulier"
      val state         = AVAILABLE
      val now           = Instant.now()
      val after5Seconds = now.plusSeconds(5)

      await(
        customsServiceStatusRepository.updateServiceStatus(CustomsServiceStatus(serviceId, "name", "description", Some(state), Some(now), Some(now)))
      )

      val updatedRecord = await(
        customsServiceStatusRepository.updateServiceStatus(
          CustomsServiceStatus(serviceId, "name", "description", Some(state), Some(after5Seconds), Some(after5Seconds))
        )
      )

      updatedRecord.stateChangedAt shouldBe Some(now.truncatedTo(MILLIS))
    }

    "update stateChangedAt when the state has changed" in {
      val serviceId     = "Haulier"
      val now           = Instant.now()
      val after5Seconds = now.plusSeconds(5)

      await(
        customsServiceStatusRepository.updateServiceStatus(
          CustomsServiceStatus(serviceId, "name", "description", Some(AVAILABLE), Some(now), Some(now))
        )
      )

      val updatedRecord = await(
        customsServiceStatusRepository.updateServiceStatus(
          CustomsServiceStatus(serviceId, "name", "description", Some(UNAVAILABLE), Some(after5Seconds), Some(after5Seconds))
        )
      )

      updatedRecord.stateChangedAt shouldBe Some(after5Seconds.truncatedTo(MILLIS))
    }
  }

  "findAll" should {
    "return empty list if no record in the db" in {
      val result = await(customsServiceStatusRepository.findAll())
      result.size shouldBe 0
    }

    "return all the customsServiceStatus entries in the database" in {
      val (service1Id, service2Id) = ("Haulier1", "Haulier2")
      val state                    = AVAILABLE
      await(
        customsServiceStatusRepository.updateServiceStatus(
          CustomsServiceStatus(service1Id, "name", "description", Some(state), Some(Instant.now()), Some(Instant.now()))
        )
      )
      await(
        customsServiceStatusRepository.updateServiceStatus(
          CustomsServiceStatus(service2Id, "name", "description", Some(state), Some(Instant.now()), Some(Instant.now()))
        )
      )
      val result = await(customsServiceStatusRepository.findAll())
      result.size shouldBe 2
    }
  }
}
