import sbt._

class ColladocProject(info: ProjectInfo) extends DefaultWebProject(info) with WinstoneProject {
  val snapshots = ScalaToolsSnapshots

  override def libraryDependencies = Set(
    "net.liftweb" % "lift-webkit_2.8.0" % "2.1-SNAPSHOT" % "compile",
    "net.liftweb" % "lift-mapper_2.8.0" % "2.1-SNAPSHOT" % "compile",
    "net.liftweb" % "lift-widgets_2.8.0" % "2.1-SNAPSHOT" % "compile",
    "com.h2database" % "h2" % "1.2.138" % "runtime",
    "junit" % "junit" % "4.7" % "test->default",
    "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5-SNAPSHOT" % "test->default",
    "org.mortbay.jetty" % "jetty" % "6.1.24" % "test->default"
  ) ++ super.libraryDependencies

  override def localScala = defineScala("2.8.0-local", ("scala" / "build" / "pack").asFile) :: Nil
}
