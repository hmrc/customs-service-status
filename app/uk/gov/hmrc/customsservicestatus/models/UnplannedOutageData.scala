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

package uk.gov.hmrc.customsservicestatus.models

import play.api.libs.json.*
import play.api.libs.json.Json.WithDefaultValues
import uk.gov.hmrc.customsservicestatus.models.DetailType.*
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class UnplannedOutageData(
                                internalReference: InternalReference,
                                preview: Preview,
                                lastUpdated:       Instant,
                                notesForClsUsers:  Option[String]
)

object UnplannedOutageData {

  val mongoFormat: OFormat[UnplannedOutageData] = {
    implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat
    Json.format[UnplannedOutageData]
  }

  implicit val format: OFormat[UnplannedOutageData] = Json.format[UnplannedOutageData]
}
