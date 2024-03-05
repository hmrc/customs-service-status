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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.customsservicestatus.errorhandlers.CustomsServiceStatusError.ServiceNotConfiguredError
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.State.AVAILABLE
import uk.gov.hmrc.customsservicestatus.models.{CustomsServiceStatus, State}
import uk.gov.hmrc.customsservicestatus.repositories.CustomsServiceStatusRepository

import java.time.Instant
import scala.concurrent.Future

class CustomsServiceStatusServiceSpec extends BaseSpec {
  val repo: CustomsServiceStatusRepository = mock[CustomsServiceStatusRepository]
  val service = new CustomsServiceStatusService(repo)

  "updateServiceStatus" should {
    "return Left ServiceNotConfiguredError if service is not configured" in {
      val serviceId = "myService"
      service.updateServiceStatus(serviceId, State.AVAILABLE).value.futureValue shouldBe Left(ServiceNotConfiguredError)
    }

    "return Right with CustomsServiceStatus if service is configured" in {
      val serviceId            = "haulier" // this is configured in application.conf
      val state                = AVAILABLE
      val customsServiceStatus = CustomsServiceStatus(serviceId, "name", "description", Some(state), Some(Instant.now), Some(Instant.now))
      when(repo.updateServiceStatus(any())).thenReturn(Future.successful(customsServiceStatus))
      service.updateServiceStatus(serviceId, state).value.futureValue shouldBe Right(customsServiceStatus)
    }
  }
}
