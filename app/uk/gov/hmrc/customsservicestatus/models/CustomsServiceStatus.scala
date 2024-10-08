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

import play.api.ConfigLoader
import play.api.libs.json.Json.WithDefaultValues
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class CustomsServiceStatus(
  id:             String,
  name:           String,
  description:    String,
  state:          Option[State],
  stateChangedAt: Option[Instant],
  lastUpdated:    Option[Instant]
)

object CustomsServiceStatus {
  import scala.jdk.CollectionConverters.ListHasAsScala

  val mongoFormat: OFormat[CustomsServiceStatus] = {
    implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat
    Json.using[WithDefaultValues].format[CustomsServiceStatus]
  }

  implicit val format: OFormat[CustomsServiceStatus] = Json.using[WithDefaultValues].format[CustomsServiceStatus]

  implicit lazy val configLoader: ConfigLoader[List[CustomsServiceStatus]] = ConfigLoader(_.getConfigList).map(
    _.asScala.toList.map { config =>
      val id          = config.getString("id")
      val name        = config.getString("name")
      val description = config.getString("description")
      CustomsServiceStatus(id, name, description, None, None, None)
    }
  )
}

case class Services(services: List[CustomsServiceStatus])

object Services {
  implicit val format: OFormat[Services] = Json.format[Services]
}
