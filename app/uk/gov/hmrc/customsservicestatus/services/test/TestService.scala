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

package uk.gov.hmrc.customsservicestatus.services.test

import uk.gov.hmrc.customsservicestatus.repositories.{AdminCustomsServiceStatusRepository, CustomsServiceStatusRepository}
import uk.gov.hmrc.play.http.logging.Mdc
import org.mongodb.scala.SingleObservableFuture
import uk.gov.hmrc.customsservicestatus.models.UnplannedOutageData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestService @Inject() (
  customsServiceStatusRepository:      CustomsServiceStatusRepository,
  adminCustomsServiceStatusRepository: AdminCustomsServiceStatusRepository
)(implicit ec: ExecutionContext) {

  def listAll: Future[List[UnplannedOutageData]] = adminCustomsServiceStatusRepository.findAll()

  def clearAllData: Future[Unit] =
    for {
      _ <- Mdc.preservingMdc(customsServiceStatusRepository.collection.drop().toFuture())
      _ <- Mdc.preservingMdc(adminCustomsServiceStatusRepository.collection.drop().toFuture())
      _ <- Mdc.preservingMdc(adminCustomsServiceStatusRepository.ensureIndexes())
      _ <- Mdc.preservingMdc(customsServiceStatusRepository.ensureIndexes())
    } yield ()
}
