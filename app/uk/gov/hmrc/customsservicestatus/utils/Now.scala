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

package uk.gov.hmrc.customsservicestatus.utils

import org.joda.time.{DateTime, DateTimeZone}

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, LocalDateTime}
trait Now[T] {
  def apply(): T
}
object Now {
  def apply[T: Now]: Now[T] = implicitly

  implicit object InstantNow extends Now[Instant] {
    override def apply(): Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)
  }

  implicit object LocalDateNow extends Now[LocalDate] {
    override def apply(): LocalDate = LocalDate.now()
  }

  implicit object UTCDateTimeNow extends Now[DateTime] {
    override def apply(): DateTime = DateTime.now(DateTimeZone.UTC)
  }

  implicit object LocalDateTimeNow extends Now[LocalDateTime] {
    override def apply(): LocalDateTime = LocalDateTime.now()
  }
  def of(instant: Instant): Now[Instant] = new Now[Instant] {
    override def apply(): Instant = instant
  }
  def of(dateTime: DateTime): Now[DateTime] = new Now[DateTime] {
    override def apply(): DateTime = dateTime
  }

  def of(localDateTime: LocalDateTime): Now[LocalDateTime] = new Now[LocalDateTime] {
    override def apply(): LocalDateTime = localDateTime
  }
}
