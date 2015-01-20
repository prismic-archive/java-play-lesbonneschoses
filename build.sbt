import play.twirl.sbt.Import._

name := "lesbonneschoses"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies += "io.prismic" % "java-kit" % "1.1.1"

libraryDependencies += javaWs

TwirlKeys.templateImports += "controllers.Prismic._"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

