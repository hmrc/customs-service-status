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
import play.api.test.Helpers.{contentAsJson, status, stubControllerComponents}
import uk.gov.hmrc.customservicestatus.errorhandlers.CustomsServiceStatusError
import uk.gov.hmrc.customservicestatus.errorhandlers.CustomsServiceStatusError.{LoadServicesConfigError, ServiceNotConfiguredError}
import uk.gov.hmrc.customsservicestatus.controllers.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.State
import uk.gov.hmrc.customsservicestatus.models.db.{CustomsServiceStatus, Status}
import uk.gov.hmrc.customsservicestatus.services.CheckService

import java.time.Instant
import scala.concurrent.Future

class ServiceControllerSpec extends BaseSpec {

  val checkService = mock[CheckService]
  val controller   = new ServiceController(checkService, stubControllerComponents())

  "PUT /services/:service/status" should {

    "return Ok with 'Not Found' in response if service is not configured" in {
      val service = "myService"
      when(checkService.check(service)).thenReturn(EitherT.leftT[Future, CustomsServiceStatus](ServiceNotConfiguredError))
      val result = controller.check(service)(FakeRequest().withBody(Json.toJson("{}")))
      status(result)                  shouldBe NOT_FOUND
      contentAsJson(result).as[State] shouldBe (State("Not Found"))
    }

    "return Ok with with name and description in response if service can be found in config" in {
      val service = "myService"
      when(checkService.check(service))
        .thenReturn(EitherT.rightT[Future, CustomsServiceStatusError](CustomsServiceStatus(service, Status(Some("Ok"), Some(Instant.now)))))
      val result = controller.check(service)(FakeRequest().withBody(Json.toJson("{}")))
      status(result)                  shouldBe OK
      contentAsJson(result).as[State] shouldBe (State("OK"))
    }
  }

}
