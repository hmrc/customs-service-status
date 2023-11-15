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
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model._
import uk.gov.hmrc.customsservicestatus.models.CustomsServiceStatus
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs._
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.http.logging.Mdc

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@Singleton
class CustomsServiceStatusRepository @Inject()(
  mongo:                     MongoComponent
)(implicit executionContext: ExecutionContext)
    extends PlayMongoRepository[CustomsServiceStatus](
      collectionName = "customs-service-status",
      mongoComponent = mongo,
      domainFormat   = CustomsServiceStatus.mongoFormat,
      indexes = Seq(
        IndexModel(ascending("id"), IndexOptions().name("serviceIdIdx").unique(true).sparse(true)),
      ),
      replaceIndexes = true
    ) {

  def updateServiceStatus(customsServiceStatus: CustomsServiceStatus): Future[CustomsServiceStatus] =
    Mdc.preservingMdc(
      collection
        .findOneAndReplace(
          equal("id", customsServiceStatus.id.toBson()),
          customsServiceStatus,
          FindOneAndReplaceOptions()
            .returnDocument(ReturnDocument.AFTER)
            .upsert(true)
        )
        .toFuture()
    )

  def findAll(): Future[List[CustomsServiceStatus]] = Mdc.preservingMdc(collection.find().toFuture()).map(_.toList)
}
