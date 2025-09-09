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
import com.mongodb.client.model.Indexes.{ascending, descending}
import org.mongodb.scala.*
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.*
import uk.gov.hmrc.customsservicestatus.models.*
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.JsonOps
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mdc.Mdc

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OutagesRepository @Inject() (
  mongo: MongoComponent
)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[OutageData](
      collectionName = "outages",
      mongoComponent = mongo,
      domainFormat = OutageData.mongoFormat,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("serviceIdIdx").unique(true).sparse(true)),
        IndexModel(ascending("lastUpdated"), IndexOptions().name("lastUpdatedIdx")),
        IndexModel(descending("endDateTime"), IndexOptions().name("endDateTimeIdx")),
        IndexModel(ascending("startDateTime"), IndexOptions().name("startDateTimeIdx"))
      )
    ) {

  def submitOutage(outage: OutageData): Future[InsertOneResult] =
    Mdc.preservingMdc(
      collection
        .insertOne(outage)
        .toFuture()
    )

  def find(id: UUID): Future[Option[OutageData]] = Mdc.preservingMdc(collection.find(equal("id", id.toBson)).headOption())

  def findAll(): Future[List[OutageData]] = Mdc.preservingMdc(collection.find().toFuture()).map(_.toList)

  def findAllPlanned(): Future[List[OutageData]] = Mdc
    .preservingMdc(
      collection
        .find(filter =
          Filters.and(gte("endDateTime", BsonDateTime(Instant.now().toEpochMilli)), equal("outageType", OutageType.Planned.value.toBson))
        )
        .sort(Sorts.ascending("startDateTime"))
        .toFuture()
    )
    .map(_.toList)

  def getLatest(outageType: OutageType): Future[Option[OutageData]] =
    Mdc.preservingMdc(
      collection
        .find(and(equal("outageType", outageType.toString)))
        .sort(Sorts.descending("publishedDateTime"))
        .limit(1)
        .headOption()
    )

  def delete(id: UUID): Future[DeleteResult] =
    Mdc.preservingMdc(collection.deleteOne(equal("id", id.toBson)).toFuture())
}
