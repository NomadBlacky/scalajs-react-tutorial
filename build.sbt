ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "com.github.nomadblacky"
ThisBuild / organizationName := "NomadBlacky"

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    name := "scalajs-react-tutorial",
    libraryDependencies ++= Seq(
        "com.github.japgolly.scalajs-react" %%% "core"      % "1.6.0",
        "org.scalatest"                     %%% "scalatest" % "3.1.1" % Test
      ),
    npmDependencies in Compile ++= Seq(
        "react"     -> "16.7.0",
        "react-dom" -> "16.7.0"
      )
  )
