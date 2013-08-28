import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "lesbonneschoses"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    javaCore
    // Add your project dependencies here,
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(

    // Import Prismic helper in templates
    templatesImport += "controllers.Prismic._",

    // Prismic.io Maven repository
    resolvers += "Prismic.io kits" at "https://github.com/prismicio/repository/raw/master/maven/",
    
    // The Java kit
    libraryDependencies += "io.prismic" % "java-kit" % "1.0-SNAPSHOT"
  )

}
