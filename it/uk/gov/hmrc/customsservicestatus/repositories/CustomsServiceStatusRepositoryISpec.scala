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

class CustomsServiceStatusRepositoryISpec extends BaseISpec {

  val testController: TestController = app.injector.instanceOf[TestController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(testController.clearAllData(fakeRequest(testRoutes.TestController.clearAllData)))
  }

  val customsServiceStatusRepository: CustomsServiceStatusRepository = app.injector.instanceOf[CustomsServiceStatusRepository]

  "updateServiceStatus" should {
    "update the service with given status and lastUpdated" in {
      val service = "myService"
      val state   = "OK"
      inside(await(customsServiceStatusRepository.updateServiceStatus(service, state))) {
        case result =>
          result.name               shouldBe (service)
          result.status.state       shouldBe (Some("OK"))
          result.status.lastUpdated shouldBe defined
      }
    }
  }

  "findAll" should {
    "return empty list if no record in the db" in {
      val result = await(customsServiceStatusRepository.findAll())
      result.size shouldBe (0)
    }
    "return all the customsServiceStatus entries in the database" in {
      val (service1, service2) = ("service1", "service2")
      val state                = "OK"
      await(customsServiceStatusRepository.updateServiceStatus(service1, state))
      await(customsServiceStatusRepository.updateServiceStatus(service2, state))
      val result = await(customsServiceStatusRepository.findAll())
      result.size shouldBe (2)
    }
  }
}
