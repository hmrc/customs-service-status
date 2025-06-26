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

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.PlannedWork

import java.time.LocalDateTime

class PlannedWorkRepositorySpec extends BaseSpec with ScalaFutures with DefaultPlayMongoRepositorySupport[PlannedWorkRepository] {
  "PlannedWorkRepository" should {
    "" in {
      val document = PlannedWork((LocalDateTime.of(2022, 2, 23, 2, 22), LocalDateTime.of(2022, 2, 23, 2, 22), "cccc"))
      val result   = await(mockPlannedWorkRepository.insert())

      println(result)
    }
  }
}
