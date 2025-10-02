import play.sbt.PlayImport.PlayKeys.playDefaultPort
import sbt.Keys.evictionErrorLevel
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

lazy val microservice = Project("customs-service-status", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    scalaVersion := "3.6.4",
    ScoverageKeys.coverageExcludedFiles :=
      "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;" +
        "app.*;.*BuildInfo.*;.*Routes.*;.*repositories.*;.*controllers.test.*;.*services.test.*;.*metrics.*",
    ScoverageKeys.coverageExcludedPackages :=
      ( "uk.gov.hmrc.customsservicestatus.config.*;" +
        "uk.gov.hmrc.customsservicestatus.controllers.test.*;" +
        "uk.gov.hmrc.customsservicestatus.services.test.*;" +
        "uk.gov.hmrc.customsservicestatus.connectors.test.*;"  +
        "uk.gov.hmrc.customsservicestatus.models.*;"
      ),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / unmanagedSourceDirectories := (Test / baseDirectory)(base => Seq(base / "test", base / "test-common")).value,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    routesImport ++= Seq(
      "java.util.UUID",
      "uk.gov.hmrc.customsservicestatus.models._"
    ),
    playDefaultPort := 8991,
    scalafmtOnCompile := true
  )
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    IntegrationTest / unmanagedSourceDirectories :=
      (IntegrationTest / baseDirectory)(base => Seq(base / "it", base / "test-common")).value,
    Test / unmanagedSourceDirectories := (Test / baseDirectory)(base => Seq(base / "test", base / "test-common")).value,
  )
  .settings(
    addCommandAlias("runTestOnly", "run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"),
    addCommandAlias("format", ";scalafmt;test:scalafmt;it:test::scalafmt"),
    addCommandAlias("verify", ";reload;format;test")
  )
  .settings(
    scalacOptions += "-no-indent"
  )

evictionErrorLevel := Level.Warn
