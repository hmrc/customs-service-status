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

import com.mongodb.client.model.Indexes.ascending
import org.mongodb.scala.*
import org.mongodb.scala.bson.{BsonBinary, BsonDateTime}
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.*
import uk.gov.hmrc.customsservicestatus.models.{OutageData, OutageType}
import org.mongodb.scala.result.InsertOneResult
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.JsonOps
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mdc.Mdc

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ArchivedOutagesRepository @Inject() (
  mongo: MongoComponent
)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[OutageData](
      collectionName = "archived-outages",
      mongoComponent = mongo,
      domainFormat = OutageData.mongoFormat,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("serviceIdIdx").unique(true).sparse(true)),
        IndexModel(ascending("lastUpdated"), IndexOptions().name("lastUpdatedIdx"))
      )
    ) {

  def insert(outage: OutageData): Future[InsertOneResult] =
    Mdc.preservingMdc(
      collection
        .insertOne(outage)
        .toFuture()
    )

  def findAll(): Future[List[OutageData]] = Mdc.preservingMdc(collection.find().toFuture()).map(_.toList)

  def findAllPlanned(): Future[List[OutageData]] = Mdc
    .preservingMdc(
      collection.find(filter = gte("endDateTime", BsonDateTime(Instant.now().toEpochMilli))).sort(Sorts.ascending("startDateTime")).toFuture()
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
}
