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
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class Status(state: Option[String], lastUpdated: Option[Instant])

case class CustomsServiceStatus(name:         String, status: Status)
case class CustomsServiceStatusWithDesc(name: String, status: Status, description: String)

case class Services(services: List[CustomsServiceStatusWithDesc])

object Status {
  val mongoFormat: OFormat[Status] = {
    implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

    val read: Reads[Status] = (
      (JsPath \ "state").readNullable[String].orElse(Reads.pure(None)) and
        (JsPath \ "lastUpdated").readNullable[Instant]
    )(Status.apply _)

    OFormat[Status](read, Json.writes[Status])
  }
  implicit val format: OFormat[Status] = Json.format[Status]
}

object CustomsServiceStatus {

  val mongoFormat: OFormat[CustomsServiceStatus] = {
    val read: Reads[CustomsServiceStatus] = (
      (JsPath \ "name").read[String] and
        (JsPath \ "status").read[Status](Status.mongoFormat)
    )(CustomsServiceStatus.apply _)

    OFormat[CustomsServiceStatus](read, Json.writes[CustomsServiceStatus])
  }

  implicit val format: OFormat[CustomsServiceStatus] = Json.format[CustomsServiceStatus]
}

object CustomsServiceStatusWithDesc {
  implicit val logger: Logger = Logger(this.getClass.getName)
  implicit val format = Json.format[CustomsServiceStatusWithDesc]

  def apply(customsServiceStatus: CustomsServiceStatus): CustomsServiceStatusWithDesc =
    ConfigSource.default.load[config.Services] match {
      case Right(services) =>
        services.services.find(_.name == customsServiceStatus.name) match {
          case Some(service) => CustomsServiceStatusWithDesc(customsServiceStatus.name, customsServiceStatus.status, service.description)
          case None          => CustomsServiceStatusWithDesc(customsServiceStatus.name, customsServiceStatus.status, "")
        }
      case Left(_) =>
        logger.warn("Failed to load services config")
        CustomsServiceStatusWithDesc(customsServiceStatus.name, customsServiceStatus.status, "")
    }
}
object Services {
  implicit val format = Json.format[Services]
}

