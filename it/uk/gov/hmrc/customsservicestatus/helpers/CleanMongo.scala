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

package uk.gov.hmrc.customsservicestatus.helpers

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.SingleObservableFuture
import org.scalatest.{BeforeAndAfterAll, TestSuite}
import org.scalatestplus.play.BaseOneAppPerSuite
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.customsservicestatus.repositories.{CustomsServiceStatusRepository, OutagesRepository}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

trait CleanMongo extends BeforeAndAfterAll { this: TestSuite & BaseOneAppPerSuite =>

  override protected def beforeAll(): Unit = {
    lazy val repositories: Seq[PlayMongoRepository[?]] = Seq(
      app.injector.instanceOf[CustomsServiceStatusRepository],
      app.injector.instanceOf[OutagesRepository]
    )

    super.beforeAll()

    val mongoComponent = app.injector.instanceOf[MongoComponent]

    Await.ready(
      Future.traverse(repositories)(_.collection.deleteMany(BsonDocument()).toFuture()),
      20.seconds
    )

    app.injector.instanceOf[ApplicationLifecycle].addStopHook { () =>
      Future(mongoComponent.client.close())
    }
  }
}
