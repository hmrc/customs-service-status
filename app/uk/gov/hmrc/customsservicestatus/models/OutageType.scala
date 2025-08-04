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

package uk.gov.hmrc.customsservicestatus.models

import play.api.i18n.Messages
import play.api.libs.json.*
import play.api.mvc.{PathBindable, QueryStringBindable}

enum OutageType {
  case Unplanned
  case Planned

  val value: String = toString

}

object OutageType {

  def apply(value: String): Option[OutageType] =
    values.find(_.value == value)

  def unapply(outageType: OutageType): String =
    outageType.value

  implicit val outageTypeFormat: Format[OutageType] = new Format[OutageType] {
    override def reads(json: JsValue): JsResult[OutageType] =
      try json.validate[String] map OutageType.valueOf
      catch case e: IllegalArgumentException => JsError("Invalid OutageType")

    override def writes(o: OutageType): JsValue = JsString(o.toString)
  }

  implicit lazy val pathBindable: PathBindable[OutageType] = new PathBindable[OutageType] {

    override def bind(key: String, value: String): Either[String, OutageType] =
      implicitly[PathBindable[String]].bind(key, value).map(OutageType.valueOf)

    override def unbind(key: String, outageType: OutageType): String =
      outageType.value
  }

  implicit def queryParamBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[OutageType] =
    stringBinder.transform(OutageType.valueOf, _.value)
}
