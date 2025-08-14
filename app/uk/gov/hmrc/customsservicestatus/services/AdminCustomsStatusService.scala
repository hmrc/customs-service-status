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

package uk.gov.hmrc.customsservicestatus.services

import com.google.inject.Singleton
import play.api.Configuration
import uk.gov.hmrc.customsservicestatus.errorhandlers.OutageError
import uk.gov.hmrc.customsservicestatus.models.*
import uk.gov.hmrc.customsservicestatus.repositories.AdminCustomsServiceStatusRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminCustomsStatusService @Inject() (
  val config:                          Configuration,
  adminCustomsServiceStatusRepository: AdminCustomsServiceStatusRepository
)(implicit
  val executionContext: ExecutionContext
) {

  def submitOutage(
    outageData: OutageData
  ): Future[Either[OutageError, Unit]] =
    adminCustomsServiceStatusRepository
      .submitOutage(
        outageData
      )
      .map {
        case insert if insert.wasAcknowledged() => Right(())
        case _                                  => Left(OutageError.OutageInsertError)
      }

}
