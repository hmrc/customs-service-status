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

import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.customsservicestatus.models.DetailType
import uk.gov.hmrc.customsservicestatus.models.DetailType.*
import uk.gov.hmrc.customsservicestatus.models.OutageType
import uk.gov.hmrc.customsservicestatus.models.OutageType.*

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

trait FakeObjects {

  val fakeDate: Instant = Instant.parse("2020-01-01T00:00:00.000Z")

  def fakeOutageData(outageType: OutageType, endDateTime: Option[Instant], startDateTime: Instant = fakeDate): OutageData =
    OutageData(
      id = UUID.randomUUID(),
      outageType = outageType,
      internalReference = InternalReference("Test reference"),
      startDateTime = startDateTime,
      endDateTime = endDateTime,
      commsText = CommsText("Test details"),
      publishedDateTime = fakeDate,
      clsNotes = Some("Notes")
    )

  val fakePlannedWorks: List[OutageData] = List(
    fakeOutageData(Planned, Some(Instant.now().plus(10, ChronoUnit.DAYS)), Instant.now().plus(6, ChronoUnit.DAYS)),
    fakeOutageData(Planned, Some(Instant.now().plus(5, ChronoUnit.DAYS)), Instant.now().plus(2, ChronoUnit.DAYS)),
    fakeOutageData(Planned, Some(Instant.now().plus(1, ChronoUnit.DAYS))),
    fakeOutageData(Planned, Some(Instant.now().minus(2, ChronoUnit.DAYS)), Instant.now().minus(3, ChronoUnit.DAYS))
  )

  val fakeUnplannedWorks: List[OutageData] = List(
    fakeOutageData(Unplanned, None),
    fakeOutageData(Unplanned, None)
  )

}
