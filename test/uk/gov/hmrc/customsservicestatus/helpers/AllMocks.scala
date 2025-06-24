package uk.gov.hmrc.customsservicestatus.helpers

import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.customsservicestatus.repositories.CustomsServiceStatusRepository
import uk.gov.hmrc.customsservicestatus.repositories.AdminCustomsServiceStatusRepository
import uk.gov.hmrc.customsservicestatus.services.{AdminCustomsStatusService, CustomsServiceStatusService}

trait AllMocks extends MockitoSugar { me: BeforeAndAfterEach =>
  val mockAdminCustomsStatusService:           AdminCustomsStatusService           = mock[AdminCustomsStatusService]
  val mockCheckService:                        CustomsServiceStatusService         = mock[CustomsServiceStatusService]
  val mockCustomsServiceStatusRepository:      CustomsServiceStatusRepository      = mock[CustomsServiceStatusRepository]
  val mockConfig:                              Configuration                       = mock[Configuration]
  val mockAdminCustomsServiceStatusRepository: AdminCustomsServiceStatusRepository = mock[AdminCustomsServiceStatusRepository]

  override protected def beforeEach(): Unit =
    Seq[Any](
      mockAdminCustomsStatusService,
      mockCheckService,
      mockConfig,
      mockCustomsServiceStatusRepository,
      mockAdminCustomsServiceStatusRepository
    ).foreach(Mockito.reset(_))
}
