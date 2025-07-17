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

package uk.gov.hmrc.customsservicestatus

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

import uk.gov.hmrc.customsservicestatus.models.PlannedWork

import java.time.Instant

trait FakeData {

  val plannedWork: PlannedWork = PlannedWork(Instant.parse("2025-02-08T01:19:31.178Z"), Instant.parse("2025-02-09T01:19:31.178Z"), "details")

  val fakePlannedWorks: List[PlannedWork] = List(
    PlannedWork(Instant.parse("2030-02-04T01:19:31.154Z"), Instant.parse("2030-02-05T01:19:31.154Z"), ""),
    PlannedWork(Instant.parse("2030-02-08T01:19:31.178Z"), Instant.parse("2030-02-09T01:19:31.178Z"), "details")
  )

}
