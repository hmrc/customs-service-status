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
import play.api.Logger
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import uk.gov.hmrc.customservicestatus.errorhandlers.CustomsServiceStatusError
import uk.gov.hmrc.customservicestatus.errorhandlers.CustomsServiceStatusError.ServiceNotConfiguredError
import uk.gov.hmrc.customsservicestatus.models
import uk.gov.hmrc.customsservicestatus.models.{CustomsServiceStatus, CustomsServiceStatusWithDesc}
import uk.gov.hmrc.customsservicestatus.models.config.Services
import uk.gov.hmrc.customsservicestatus.repositories.CustomsServiceStatusRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckService @Inject()(customsServiceStatusRepository: CustomsServiceStatusRepository)(implicit val executionContext: ExecutionContext) {

  implicit val logger: Logger = Logger(this.getClass.getName)

  def check(service: String): EitherT[Future, CustomsServiceStatusError, CustomsServiceStatus] = {
    val servicesFromConfig: Services = ConfigSource.default.loadOrThrow[Services]
    if (servicesFromConfig.services.exists(_.name.equalsIgnoreCase(service)))
      EitherT.right[CustomsServiceStatusError](customsServiceStatusRepository.check(service))
    else {
      logger.warn(s"Not recognised $service")
      EitherT.leftT[Future, CustomsServiceStatus](ServiceNotConfiguredError)
    }
  }

  def listAll: Future[models.Services] = {
    val servicesFromConfig: Services = ConfigSource.default.loadOrThrow[Services]
    customsServiceStatusRepository.findAll() map { services =>
      val serviceNames = servicesFromConfig.services.map(_.name)
      models.Services(services.filter(s => serviceNames.contains(s.name)) map (CustomsServiceStatusWithDesc(_)))
    }
  }
}