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
import cats.implicits._
import com.google.inject.Singleton
import play.api.{Configuration, Logging}
import uk.gov.hmrc.customsservicestatus.errorhandlers.CustomsServiceStatusError
import uk.gov.hmrc.customsservicestatus.errorhandlers.CustomsServiceStatusError.ServiceNotConfiguredError
import uk.gov.hmrc.customsservicestatus.models.State.UNKNOWN
import uk.gov.hmrc.customsservicestatus.models._
import uk.gov.hmrc.customsservicestatus.models.CustomsServiceStatus._
import uk.gov.hmrc.customsservicestatus.repositories.CustomsServiceStatusRepository

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsServiceStatusService @Inject() (val config: Configuration, customsServiceStatusRepository: CustomsServiceStatusRepository)(implicit
  val executionContext: ExecutionContext
) extends Logging {

  private val knownServices: Services = Services(config.get[List[CustomsServiceStatus]]("services"))

  def updateServiceStatus(serviceId: String, state: State): EitherT[Future, CustomsServiceStatusError, CustomsServiceStatus] = {
    val configuredService = knownServices.services.find(_.id == serviceId)
    configuredService match {
      case Some(service) =>
        val toUpdate = CustomsServiceStatus(serviceId, service.name, service.description, Some(state), Some(Instant.now()), Some(Instant.now()))
        EitherT.right[CustomsServiceStatusError](customsServiceStatusRepository.updateServiceStatus(toUpdate))
      case None =>
        logger.warn(s"Service with id $serviceId not configured")
        EitherT.leftT[Future, CustomsServiceStatus](ServiceNotConfiguredError)
    }
  }

  def listAll: Future[Services] =
    customsServiceStatusRepository.findAll().map { servicesFromDB =>
      val services = knownServices.services.map { configuredService =>
        val matchedService = servicesFromDB.find(_.name == configuredService.name)
        val state          = matchedService.flatMap(_.state).orElse(Some(UNKNOWN))
        val lastUpdated    = matchedService.flatMap(_.lastUpdated)
        val stateChangedAt = matchedService.flatMap(_.stateChangedAt)
        CustomsServiceStatus(configuredService.id, configuredService.name, configuredService.description, state, stateChangedAt, lastUpdated)
      }

      Services(services)
    }
}
