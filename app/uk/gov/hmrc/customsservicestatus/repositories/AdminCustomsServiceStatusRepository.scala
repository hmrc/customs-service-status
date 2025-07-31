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

package uk.gov.hmrc.customsservicestatus.repositories

import cats.data.OptionT
import com.mongodb.client.model.Indexes.ascending
import org.mongodb.scala.*
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.result.InsertOneResult
import uk.gov.hmrc.customsservicestatus.models.OutageData
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.JsonOps
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.http.logging.Mdc

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminCustomsServiceStatusRepository @Inject() (
  mongo:                     MongoComponent,
  archivedOutagesRepository: ArchivedOutagesRepository
)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[OutageData](
      collectionName = "admin-customs-service-status",
      mongoComponent = mongo,
      domainFormat = OutageData.mongoFormat,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("serviceIdIdx").unique(true).sparse(true)),
        IndexModel(ascending("lastUpdated"), IndexOptions().name("lastUpdatedIdx"))
      )
    ) {

  def submitOutage(outage: OutageData): Future[InsertOneResult] =
    Mdc.preservingMdc(
      collection
        .insertOne(outage)
        .toFuture()
    )

  def findAll(): Future[List[OutageData]] = Mdc.preservingMdc(collection.find().toFuture()).map(_.toList)

  def find(id: UUID): Future[Option[OutageData]] = Mdc.preservingMdc(collection.find(equal("id", id.toBson)).headOption())

  def archive(id: UUID): Future[Option[OutageData]] = {
    val maybeArchivedOutageData: OptionT[Future, OutageData] = for {
      outageData     <- OptionT(find(id))
      archivedResult <- OptionT.liftF(archivedOutagesRepository.addToArchived(outageData))
      if archivedResult.wasAcknowledged() && !archivedResult.getInsertedId.isNull
      deleteResult <- OptionT.liftF(Mdc.preservingMdc(collection.deleteOne(equal("id", id.toBson)).toFuture()))
      if deleteResult.wasAcknowledged() && deleteResult.getDeletedCount == 1
    } yield outageData
    maybeArchivedOutageData.value
  }
}
