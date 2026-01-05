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

package uk.gov.hmrc.customsservicestatus.factories

import uk.gov.hmrc.customsservicestatus.models.DetailType.{CommsText, InternalReference}
import uk.gov.hmrc.customsservicestatus.models.{OutageData, OutageType}
import uk.gov.hmrc.customsservicestatus.TestData.*

import java.time.Instant
import java.util.UUID

object OutageDataFactory {

  def fakeOutageData(
    id:                UUID = UUID.randomUUID(),
    outageType:        OutageType,
    internalReference: InternalReference = InternalReference("Test reference"),
    startDateTime:     Instant = pastTestDate,
    endDateTime:       Option[Instant] = None,
    commsText:         CommsText = CommsText("Test details"),
    publishedDateTime: Instant = pastTestDate,
    clsNotes:          Option[String] = None
  ): OutageData =
    OutageData(
      id,
      outageType,
      internalReference,
      startDateTime,
      endDateTime,
      commsText,
      publishedDateTime,
      clsNotes
    )

}
