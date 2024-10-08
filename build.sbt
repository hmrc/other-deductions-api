/*
 * Copyright 2022 HM Revenue & Customs
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

import sbt.Keys.{baseDirectory, parallelExecution, unmanagedClasspath}
import sbt.*
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings}
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val ItTest = config("it") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    retrieveManaged                 := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    scalaVersion                    := "2.13.12",
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-Wconf:src=routes/.*:silent",
      "-feature",
      "-language:higherKinds",
      "-Xlint:-byname-implicit",
    )
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(majorVersion := 0)
  .settings(CodeCoverageSettings.settings *)
  .settings(defaultSettings() *)
  .configs(ItTest)
  .settings(
    inConfig(ItTest)(Defaults.itSettings ++ headerSettings(ItTest) ++ automateHeaderSettings(ItTest) ++ ScalafmtPlugin.scalafmtConfigSettings),
    ItTest / fork                       := true,
    ItTest / unmanagedSourceDirectories := Seq((ItTest / baseDirectory).value / "it"),
    ItTest / unmanagedClasspath += baseDirectory.value / "resources",
    Runtime / unmanagedClasspath += baseDirectory.value / "resources",
    ItTest / javaOptions += "-Dlogger.resource=logback-test.xml",
    ItTest / parallelExecution := false,
    addTestReportOption(ItTest, directory = "int-test-reports")
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(PlayKeys.playDefaultPort := 7797)

val appName = "other-deductions-api"

dependencyUpdatesFilter -= moduleFilter(name = "bootstrap-backend-play-30")
