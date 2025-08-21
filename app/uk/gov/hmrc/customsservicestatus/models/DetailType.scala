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

import play.api.libs.json.{Json, OFormat}

sealed trait DetailType

object DetailType {
  final case class InternalReference(text: String) extends DetailType
  final case class CommsText(html: String) extends DetailType

  object InternalReference {
    implicit val format: OFormat[InternalReference] = Json.format[InternalReference]
  }

  object CommsText {
    implicit val format: OFormat[CommsText] = Json.format[CommsText]
  }
}
