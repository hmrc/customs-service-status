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

package uk.gov.hmrc.customsservicestatus.repositories

import com.mongodb.client.model.Indexes.{ascending, descending}
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.ReturnDocument.AFTER
import com.mongodb.client.model.Updates
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions}
import uk.gov.hmrc.customsservicestatus.models.PlannedWork
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.http.logging.Mdc
import org.mongodb.scala.*
import org.mongodb.scala.model.Aggregates.{filter, project, sort}
import org.mongodb.scala.model.Filters.and
import org.mongodb.scala.result.InsertOneResult
import uk.gov.hmrc.customsservicestatus.models

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PlannedWorkRepository @Inject() (
  mongo: MongoComponent
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[PlannedWork](
      collectionName = "customs-service-status-planned-work",
      mongoComponent = mongo,
      domainFormat = PlannedWork.mongoFormat,
      indexes = Seq(
        IndexModel(ascending("dateFrom"), IndexOptions().name("dateFromIdx")),
        IndexModel(ascending("dateTo"), IndexOptions().name("dateToIdx"))
      ),
      replaceIndexes = true
    ) {

  def findAll(): Future[List[PlannedWork]] = Mdc.preservingMdc(collection.find().toFuture()).map(_.toList)

//  def insert(): Future[InsertOneResult] =
//    Mdc.preservingMdc(
//      collection.insertOne(PlannedWork(LocalDateTime.of(2022, 2, 23, 2, 22), LocalDateTime.of(2022, 2, 23, 2, 22), "cccc")).toFuture()
//    )

}
