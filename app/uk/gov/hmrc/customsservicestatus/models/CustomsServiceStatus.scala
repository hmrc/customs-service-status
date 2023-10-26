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

import play.api.Logger
import play.api.libs.json._
import uk.gov.hmrc.customsservicestatus.models.config.{Service => ServiceFromConfig}

import java.time.Instant

case class Status(state: Option[String], lastUpdated: Option[Instant])

case class CustomsServiceStatus(name:         String, status: Status)
case class CustomsServiceStatusWithDesc(name: String, status: Status, description: String)

case class Services(services: List[CustomsServiceStatusWithDesc])

object Status {
  implicit def format(implicit instantFormat: Format[Instant]): OFormat[Status] = Json.using[Json.WithDefaultValues].format[Status]
}

object CustomsServiceStatus {
  implicit def format(implicit instantFormat: Format[Instant]): OFormat[CustomsServiceStatus] = Json.format[CustomsServiceStatus]
}

object CustomsServiceStatusWithDesc {
  implicit val logger: Logger = Logger(this.getClass.getName)
  implicit val format = Json.format[CustomsServiceStatusWithDesc]

  def apply(serviceStatuses: List[CustomsServiceStatus], serviceFromConfig: ServiceFromConfig): CustomsServiceStatusWithDesc = {

    val status: Status = serviceStatuses.find(_.name.equalsIgnoreCase(serviceFromConfig.name)) match {
      case None                => Status(Some("UNKNOWN"), None)
      case Some(serviceStatus) => serviceStatus.status
    }
    CustomsServiceStatusWithDesc(serviceFromConfig.name, status, serviceFromConfig.description)
  }
}

object Services {
  implicit val format = Json.format[Services]
}
