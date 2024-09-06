import play.sbt.PlayImport.PlayKeys.playDefaultPort
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{integrationTestSettings, scalaSettings}
import sbt.Keys.evictionErrorLevel

lazy val microservice = Project("customs-service-status", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    scalaVersion := "3.4.2",
    ScoverageKeys.coverageExcludedFiles :=
      "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;" +
        "app.*;.*BuildInfo.*;.*Routes.*;.*repositories.*;.*controllers.test.*;.*services.test.*;.*metrics.*",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    playDefaultPort := 8991,
    scalafmtOnCompile := true
  )
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    addCommandAlias("runTestOnly", "run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"),
    addCommandAlias("format", ";scalafmt;test:scalafmt;it:test::scalafmt"),
    addCommandAlias("verify", ";reload;format;test")
  )

evictionErrorLevel := Level.Warn
