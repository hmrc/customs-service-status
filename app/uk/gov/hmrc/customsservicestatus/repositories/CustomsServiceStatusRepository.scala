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
import com.mongodb.client.model.ReturnDocument.AFTER
import com.mongodb.client.model.Updates.set
import org.bson.BsonValue
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model._
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.customsservicestatus.models.CustomsServiceStatus
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs._
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.instantWrites
import uk.gov.hmrc.play.http.logging.Mdc

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@Singleton
class CustomsServiceStatusRepository @Inject() (
  mongo: MongoComponent
)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[CustomsServiceStatus](
      collectionName = "customs-service-status",
      mongoComponent = mongo,
      domainFormat = CustomsServiceStatus.mongoFormat,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("serviceIdIdx").unique(true).sparse(true)),
        IndexModel(ascending("lastUpdated"), IndexOptions().name("lastUpdatedIdx"))
      ),
      replaceIndexes = true
    ) {

  private def findOneAndUpdateOptions = FindOneAndUpdateOptions().returnDocument(AFTER).upsert(true)

  def updateServiceStatus(customsServiceStatus: CustomsServiceStatus): Future[CustomsServiceStatus] =
    Mdc.preservingMdc(
      collection
        .findOneAndUpdate(
          equal("id", customsServiceStatus.id.toBson),
          Seq(
            set("lastUpdated", BsonDateTime(customsServiceStatus.lastUpdated.getOrElse(Instant.now()).toEpochMilli)),
            set("stateChangedAt", stateChangedAtBson(customsServiceStatus)),
            set("state", customsServiceStatus.state.toBson),
            set("name", customsServiceStatus.name.toBson),
            set("description", customsServiceStatus.description.toBson)
          ),
          findOneAndUpdateOptions
        )
        .toFuture()
    )

  // update 'stateChangedAt' only when the 'state' has changed
  // but stores a new value if the field doesn't exists in the record already
  private def stateChangedAtBson(customsServiceStatus: CustomsServiceStatus): BsonValue = {
    val newValue = instantWrites.writes(customsServiceStatus.stateChangedAt.getOrElse(Instant.now()))
    Json
      .obj(
        "$cond" -> Json.obj(
          "if"   -> Json.obj("$ne" -> Json.arr("$state", JsString(customsServiceStatus.state.map(_.value).getOrElse("")))),
          "then" -> newValue,
          "else" ->
            Json.obj("$ifNull" -> Json.arr("$stateChangedAt", newValue))
        )
      )
      .toBson
  }

  def findAll(): Future[List[CustomsServiceStatus]] = Mdc.preservingMdc(collection.find().toFuture()).map(_.toList)
}
