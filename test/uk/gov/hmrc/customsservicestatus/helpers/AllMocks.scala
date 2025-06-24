package uk.gov.hmrc.customsservicestatus.helpers

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.customsservicestatus.services.{AdminCustomsStatusService, CustomsServiceStatusService}

trait AllMocks extends MockitoSugar { me: BeforeAndAfterEach =>
  val mockAdminCustomsStatusService: AdminCustomsStatusService   = mock[AdminCustomsStatusService]
  val mockCheckService:              CustomsServiceStatusService = mock[CustomsServiceStatusService]

  override protected def beforeEach(): Unit =
    Seq[Any](
      mockAdminCustomsStatusService,
      mockCheckService
    ).foreach(Mockito.reset(_))
}
