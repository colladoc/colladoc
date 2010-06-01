import sbt._

class ColladocProject(info: ProjectInfo) extends DefaultWebProject(info) with IdeaPlugin {
  val snapshots = ScalaToolsSnapshots
  
  val lift = "net.liftweb" % "lift-core" % "2.0-scala280-SNAPSHOT" % "compile"
  val h2 = "com.h2database" % "h2" % "1.2.134" % "runtime"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  val junit = "junit" % "junit" % "4.7" % "test"
  val specs = "org.scala-tools.testing" % "specs_2.8.0.Beta1" % "1.6.3" % "test"
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.24" % "test"

  //val smackRepo = "m2-repository-smack" at "http://maven.reucon.com/public"
  //val nexusRepo = "nexus" at "https://nexus.griddynamics.net/nexus/content/groups/public"

  override def localScala = defineScala("2.8.0-local", ("lib" / "scala" / "build" / "pack").asFile) :: Nil
}
