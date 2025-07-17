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

package uk.gov.hmrc.customsservicestatus.repositories

import org.mongodb.scala.SingleObservableFuture
import org.mongodb.scala.model.Sorts
import uk.gov.hmrc.customsservicestatus.controllers.test.TestController
import uk.gov.hmrc.customsservicestatus.helpers.BaseISpec

class PlannedWorkRepositoryISpec extends BaseISpec {

  val testController:        TestController        = app.injector.instanceOf[TestController]
  val plannedWorkRepository: PlannedWorkRepository = app.injector.instanceOf[PlannedWorkRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(callRoute(fakeRequest(uk.gov.hmrc.customsservicestatus.controllers.test.routes.TestController.clearAllData)))
  }

  "findAll" should {
    "return empty list if no record in the db" in {
      val result = await(plannedWorkRepository.findAll(None))
      result.size shouldBe 0
    }

    "return all the entries in the database" in {
      await(
        plannedWorkRepository.collection
          .insertMany(fakePlannedWorks)
          .toFuture()
      )

      val result = await(plannedWorkRepository.findAll(Some(Sorts.descending("dateFrom"))))
      result.size shouldBe 2
    }
  }

}
