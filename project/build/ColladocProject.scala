import sbt._

class ColladocProject(info: ProjectInfo) extends DefaultWebProject(info) with WinstoneProject {
  val snapshots = ScalaToolsSnapshots

  val liftMapper = "net.liftweb" % "lift-mapper_2.8.0" % "2.2-SNAPSHOT" % "compile"
  val liftWidgets = "net.liftweb" % "lift-widgets_2.8.0" % "2.2-SNAPSHOT" % "compile"
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.24" % "test->default"
  val h2 = "com.h2database" % "h2" % "1.2.138" % "runtime"
  val postgresql = "postgresql" % "postgresql" % "8.4-701.jdbc4"
  val junit = "junit" % "junit" % "4.7" % "test->default"
  val specs = "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5" % "test->default"

  override def localScala = defineScala("2.8.0-local", ("scala" / "build" / "pack").asFile) :: Nil

  override def managedStyle = ManagedStyle.Maven
  override def jettyWebappPath = webappPath
  override def scanDirectories = Nil 
}
