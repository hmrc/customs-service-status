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

import cats.data.{EitherT, NonEmptyMap}
import cats.implicits.*
import com.google.inject.Singleton
import play.api.{Configuration, Logging}
import uk.gov.hmrc.customsservicestatus.errorhandlers.AdminCustomsServiceStatusError.*
import uk.gov.hmrc.customsservicestatus.errorhandlers.{AdminCustomsServiceStatusError, CustomsServiceStatusError}
import uk.gov.hmrc.customsservicestatus.models.*
import uk.gov.hmrc.customsservicestatus.models.CustomsServiceStatus.*
import uk.gov.hmrc.customsservicestatus.models.State.UNKNOWN
import uk.gov.hmrc.customsservicestatus.repositories.AdminCustomsServiceStatusRepository

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminCustomsServiceStatusService @Inject() (
  val config:                          Configuration,
  adminCustomsServiceStatusRepository: AdminCustomsServiceStatusRepository
)(implicit
  val executionContext: ExecutionContext
) {

  def submitUnplannedOutage(
    unplannedOutageRequestData: UnplannedOutageRequestData
  ): Future[Option[AdminCustomsServiceStatusError]] =
    adminCustomsServiceStatusRepository
      .submitUnplannedOutage(
        AdminCustomsServiceStatus(
          unplannedOutageRequestData.internalReference,
          unplannedOutageRequestData.additionalDetails,
          unplannedOutageRequestData.lastUpdated,
          unplannedOutageRequestData.notesForClsUsers
        )
      )
      .map {
        case insert if insert.wasAcknowledged() => None
        case _                                  => Some(GenericError)
      }

  def listAll: Future[List[AdminCustomsServiceStatus]] =
    adminCustomsServiceStatusRepository.findAll()
}
