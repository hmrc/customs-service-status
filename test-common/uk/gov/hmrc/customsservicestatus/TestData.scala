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

package uk.gov.hmrc.customsservicestatus

import uk.gov.hmrc.customsservicestatus.models.DetailType.*
import uk.gov.hmrc.customsservicestatus.models.OutageType.*
import uk.gov.hmrc.customsservicestatus.models.*
import uk.gov.hmrc.customsservicestatus.factories.OutageDataFactory.*

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

object TestData {

  val now: Instant = Instant.now()

  val fakeDate: Instant = Instant.parse("2020-01-01T00:00:00.000Z")

  val fakePlannedWorks: List[OutageData] = List(
    fakeOutageData(outageType = Planned, startDateTime = now.plus(6, ChronoUnit.DAYS), endDateTime = Some(now.plus(10, ChronoUnit.DAYS))),
    fakeOutageData(outageType = Planned, startDateTime = now.plus(2, ChronoUnit.DAYS), endDateTime = Some(now.plus(5, ChronoUnit.DAYS))),
    fakeOutageData(outageType = Planned, endDateTime = Some(now.plus(1, ChronoUnit.DAYS))),
    fakeOutageData(outageType = Planned, startDateTime = now.minus(3, ChronoUnit.DAYS), endDateTime = Some(now.minus(2, ChronoUnit.DAYS)))
  )

  val fakeUnplannedWorks: List[OutageData] = List(
    fakeOutageData(outageType = Unplanned),
    fakeOutageData(outageType = Unplanned)
  )
}
