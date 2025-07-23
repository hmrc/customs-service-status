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

package uk.gov.hmrc.customsservicestatus.controllers

import cats.data.EitherT
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, status, stubControllerComponents}
import uk.gov.hmrc.customsservicestatus.errorhandlers.CustomsServiceStatusError
import uk.gov.hmrc.customsservicestatus.errorhandlers.CustomsServiceStatusError.ServiceNotConfiguredError
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.State.AVAILABLE
import uk.gov.hmrc.customsservicestatus.models.{CustomsServiceStatus, State}

import java.time.Instant
import scala.concurrent.Future

class CustomsServiceStatusControllerSpec extends BaseSpec {

  val controller = new CustomsServiceStatusController(mockCheckService, stubControllerComponents())

  "updateServiceStatus" should {

    "return NOT_FOUND if service is not configured" in {
      val serviceId = "myService"
      val state     = AVAILABLE
      when(mockCheckService.updateServiceStatus(serviceId, state)).thenReturn(EitherT.leftT[Future, CustomsServiceStatus](ServiceNotConfiguredError))
      val result = controller.updateServiceStatus(serviceId)(FakeRequest().withBody(Json.toJson[State](state)))
      status(result)          shouldBe NOT_FOUND
      contentAsString(result) shouldBe s"Service with name $serviceId not configured"
    }

    "return BAD_REQUEST if invalid json state supplied" in {
      val serviceId = "myService"
      when(mockCheckService.updateServiceStatus(serviceId, State.AVAILABLE))
        .thenReturn(EitherT.leftT[Future, CustomsServiceStatus](ServiceNotConfiguredError))
      val result = controller.updateServiceStatus(serviceId)(FakeRequest().withBody(Json.toJson("OK")))
      status(result)          shouldBe BAD_REQUEST
      contentAsString(result) shouldBe "invalid value: OK for State type"
    }

    "return OK with with id, name and description in response if service can be found in config" in {
      val serviceId = "myService"
      val state     = AVAILABLE
      when(mockCheckService.updateServiceStatus(serviceId, state))
        .thenReturn(
          EitherT.rightT[Future, CustomsServiceStatusError](
            CustomsServiceStatus(serviceId, "name", "description", Some(state), Some(Instant.now), Some(Instant.now))
          )
        )
      val result = controller.updateServiceStatus(serviceId)(FakeRequest().withBody(Json.toJson[State](state)))
      status(result) shouldBe OK
    }
  }

}
