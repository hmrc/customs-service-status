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

package uk.gov.hmrc.customsservicestatus.errorhandlers

sealed trait ErrorResponse {
  val message: String
}

object ErrorResponse {

  case object UpstreamError extends ErrorResponse {
    override val message: String = "Upstream service unavailable"
  }
  case class UnrecognisedServiceError(serviceName: String) extends ErrorResponse {
    override val message: String = s"Service with name $serviceName not configured"
  }
  case object InvalidStateError extends ErrorResponse {
    override val message: String = "Invalid service state"
  }
}
