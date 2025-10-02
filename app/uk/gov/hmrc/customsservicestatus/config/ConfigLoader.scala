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

package uk.gov.hmrc.customsservicestatus.config

import uk.gov.hmrc.customsservicestatus.models.CustomsServiceStatus
import scala.jdk.CollectionConverters.ListHasAsScala


object ConfigLoader {

  implicit lazy val configLoader: play.api.ConfigLoader[List[CustomsServiceStatus]] = play.api.ConfigLoader(_.getConfigList).map(
    _.asScala.toList.map { config =>
      val id = config.getString("id")
      val name = config.getString("name")
      val description = config.getString("description")
      CustomsServiceStatus(id, name, description, None, None, None)
    }
  )
}

