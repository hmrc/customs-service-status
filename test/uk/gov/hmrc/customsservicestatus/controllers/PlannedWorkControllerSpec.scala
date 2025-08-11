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

import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, stubControllerComponents}
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.PlannedWork

import scala.concurrent.Future

class PlannedWorkControllerSpec extends BaseSpec {

  val plannedWorkController = new PlannedWorkController(mockPlannedWorkService, stubControllerComponents())

  "getPlannedWork" should {
    "return OK and call the PlannedWorkService" in {

      when(mockPlannedWorkService.getPlannedWork).thenReturn(Future[List[PlannedWork]](fakePlannedWorks))

      val result = plannedWorkController.getPlannedWork(FakeRequest())
      status(result) shouldBe OK
    }
  }

}
