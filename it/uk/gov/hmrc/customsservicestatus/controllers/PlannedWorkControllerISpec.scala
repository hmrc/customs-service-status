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

import org.mongodb.scala.SingleObservableFuture
import uk.gov.hmrc.customsservicestatus.controllers.test.TestController
import uk.gov.hmrc.customsservicestatus.helpers.BaseISpec
import uk.gov.hmrc.customsservicestatus.models.PlannedWork
import uk.gov.hmrc.customsservicestatus.repositories.PlannedWorkRepository

class PlannedWorkControllerISpec extends BaseISpec {

  val testController:        TestController        = app.injector.instanceOf[TestController]
  val plannedWorkRepository: PlannedWorkRepository = app.injector.instanceOf[PlannedWorkRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(callRoute(fakeRequest(uk.gov.hmrc.customsservicestatus.controllers.test.routes.TestController.clearAllData)))
  }

  "GET /services/planned-work" should {
    "return Ok with empty list" when {
      "there are no corresponding entries in the db" in {

        val result      = callRoute(fakeRequest(routes.PlannedWorkController.getPlannedWork()))
        val plannedWork = contentAsJson(result).as[List[PlannedWork]]

        status(result) shouldBe OK
        plannedWork    shouldBe List()
      }
    }

    "return Ok with a a non-empty list" when {
      "there are 2 corresponding entries in the db" in {

        await(
          plannedWorkRepository.collection
            .insertMany(fakePlannedWorks)
            .toFuture()
        )

        val result      = callRoute(fakeRequest(routes.PlannedWorkController.getPlannedWork()))
        val plannedWork = contentAsJson(result).as[List[PlannedWork]]

        status(result) shouldBe OK
        plannedWork    shouldBe fakePlannedWorks
      }
    }
  }

}
