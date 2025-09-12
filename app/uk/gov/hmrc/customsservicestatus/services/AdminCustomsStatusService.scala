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

import cats.data.EitherT
import com.google.inject.Singleton
import play.api.Configuration
import uk.gov.hmrc.customsservicestatus.errorhandlers.OutageError
import uk.gov.hmrc.customsservicestatus.errorhandlers.OutageError.*
import uk.gov.hmrc.customsservicestatus.models.*
import uk.gov.hmrc.customsservicestatus.repositories.{ArchivedOutagesRepository, OutagesRepository}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminCustomsStatusService @Inject() (
  val config:                Configuration,
  outagesRepository:         OutagesRepository,
  archivedOutagesRepository: ArchivedOutagesRepository
)(implicit
  val executionContext: ExecutionContext
) {

  def submitOutage(
    outage: OutageData
  ): Future[Either[OutageError, Unit]] =
    outagesRepository
      .submitOutage(
        outage
      )
      .map {
        case insert if insert.wasAcknowledged() => Right(())
        case _                                  => Left(OutageInsertError)
      }

  def getAllPlannedWorks: Future[Seq[OutageData]] =
    outagesRepository.findAllPlanned()

  def findAllOutages(): Future[List[OutageData]] =
    outagesRepository.findAll()

  def getLatestOutage(outageType: OutageType): Future[Option[OutageData]] = outagesRepository.getLatest(outageType)

  def findOutage(id: UUID): Future[Option[OutageData]] =
    outagesRepository.find(id)

  def archiveOutage(id: UUID): Future[Either[OutageError, OutageData]] =
    outagesRepository.find(id).flatMap {
      case Some(outage) =>
        for {
          insertResult <- archivedOutagesRepository.insert(outage)
          result <- if (insertResult.wasAcknowledged()) {
                      outagesRepository.delete(id).map(_ => Right(outage))
                    } else Future.successful(Left(OutageArchiveInsertError))
        } yield result
      case None => Future.successful(Left(OutageArchiveError))
    }
}
