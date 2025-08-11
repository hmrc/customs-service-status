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

package uk.gov.hmrc.customsservicestatus.helpers

import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.customsservicestatus.repositories.CustomsServiceStatusRepository
import uk.gov.hmrc.customsservicestatus.repositories.AdminCustomsServiceStatusRepository
import uk.gov.hmrc.customsservicestatus.services.{AdminCustomsStatusService, CustomsServiceStatusService}
import uk.gov.hmrc.customsservicestatus.repositories.{CustomsServiceStatusRepository, PlannedWorkRepository}
import uk.gov.hmrc.customsservicestatus.services.{CustomsServiceStatusService, PlannedWorkService}

trait AllMocks extends MockitoSugar { me: BeforeAndAfterEach =>
  val mockAdminCustomsStatusService:           AdminCustomsStatusService           = mock[AdminCustomsStatusService]
  val mockCheckService:                        CustomsServiceStatusService         = mock[CustomsServiceStatusService]
  val mockCustomsServiceStatusRepository:      CustomsServiceStatusRepository      = mock[CustomsServiceStatusRepository]
  val mockPlannedWorkService:                  PlannedWorkService                  = mock[PlannedWorkService]
  val mockConfig:                              Configuration                       = mock[Configuration]
  val mockAdminCustomsServiceStatusRepository: AdminCustomsServiceStatusRepository = mock[AdminCustomsServiceStatusRepository]
  val mockPlannedWorkRepository:               PlannedWorkRepository               = mock[PlannedWorkRepository]

  override protected def beforeEach(): Unit =
    Seq[Any](
      mockAdminCustomsStatusService,
      mockCheckService,
      mockPlannedWorkService,
      mockConfig,
      mockCustomsServiceStatusRepository,
      mockAdminCustomsServiceStatusRepository,
      mockPlannedWorkRepository
    ).foreach(Mockito.reset(_))
}
