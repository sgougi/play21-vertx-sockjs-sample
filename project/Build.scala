import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play21-vertx-sockjs-sample"
  val appVersion      = "1.0-SNAPSHOT"
  val hazelcastVersion = "2.6"
  val jacksonVersion  = "2.2.2"
  val nettyVersion    = "4.0.0.CR9"
  val vertxVersion    = "2.0.0-CR3"
    
  val appDependencies = Seq(
    // Add your project dependencies here,
   "com.hazelcast" % "hazelcast" % {hazelcastVersion},
   "com.fasterxml.jackson.core" % "jackson-annotations" % {jacksonVersion},   
   "com.fasterxml.jackson.core" % "jackson-core" % {jacksonVersion},   
   "com.fasterxml.jackson.core" % "jackson-databind" % {jacksonVersion},   
   "io.netty" % "netty-all" % {nettyVersion},   
   "io.vertx" % "vertx-core" % {vertxVersion},   
   "io.vertx" % "vertx-platform" % {vertxVersion},   
    javaCore  excludeAll(
      ExclusionRule(organization = "io.netty")
    )
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    javacOptions ++= Seq("-target", "1.7", "-source", "1.7")
  )

}
