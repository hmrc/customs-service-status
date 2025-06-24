package uk.gov.hmrc.customsservicestatus.controllers

import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.services.AdminCustomsStatusService

class AdminCustomsServiceStatusControllerSpec extends BaseSpec {
  val controller = new CustomsServiceStatusController(mockCheckService, stubControllerComponents())
}
