package uk.gov.hmrc.customsservicestatus.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, stubControllerComponents}
import uk.gov.hmrc.customsservicestatus.errorhandlers.AdminCustomsServiceStatusError.GenericError
import uk.gov.hmrc.customsservicestatus.helpers.BaseSpec
import uk.gov.hmrc.customsservicestatus.models.DetailType.*
import uk.gov.hmrc.customsservicestatus.models.UnplannedOutageData

import java.time.Instant
import scala.concurrent.Future

class AdminCustomsServiceStatusControllerSpec extends BaseSpec {
  val controller = new AdminCustomsServiceStatusController(mockAdminCustomsStatusService, stubControllerComponents())

  val validUnplannedOutageData: UnplannedOutageData = UnplannedOutageData(
    InternalReference("Test reference"),
    Preview("Test details"),
    Instant.now(),
    None
  )

  "AdminCustomsServiceStatusController" should {
    "validate a correct request json and call the service with a valid case class instance" in {
      when(mockAdminCustomsStatusService.submitUnplannedOutage(any())).thenReturn(Future.successful(None))
      val result =
        controller.updateWithUnplannedOutage()(FakeRequest().withBody(Json.toJson[UnplannedOutageData](validUnplannedOutageData)))
      status(result) shouldBe OK
    }
    "return a bad request status when the service returns an error" in {
      when(mockAdminCustomsStatusService.submitUnplannedOutage(any())).thenReturn(Future.successful(Some(GenericError)))
      val result =
        controller.updateWithUnplannedOutage()(FakeRequest().withBody(Json.toJson[UnplannedOutageData](validUnplannedOutageData)))
      status(result) shouldBe BAD_REQUEST
    }
  }
}
