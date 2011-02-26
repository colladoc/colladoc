// TODO: Uncomment this line after the jetty-run error caused by selenium is fixed: ExceptionInInitializerError
// import bryanjswift.selenium.SeleniumPlugin
import sbt._

class ColladocProject(info: ProjectInfo) extends DefaultWebProject(info)
with WinstoneProject
// TODO: Uncomment this line after the jetty-run error caused by selenium is fixed: ExceptionInInitializerError
//with SeleniumPlugin
  {
  val snapshots = ScalaToolsSnapshots

  val liftMapper = "net.liftweb" % "lift-mapper_2.8.1" % "2.2" % "compile"
  val liftWidgets = "net.liftweb" % "lift-widgets_2.8.1" % "2.2" % "compile"
  //val lucene = "org.apache.lucene" % "lucene-core" % "3.0.3" % "compile"
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.24" % "test->default"
  val h2 = "com.h2database" % "h2" % "1.2.144" % "runtime"
  val postgresql = "postgresql" % "postgresql" % "8.4-701.jdbc4"
  val junit = "junit" % "junit" % "4.8.2" % "test->default"
  val jmock_junit4 = "org.jmock" % "jmock-junit4" % "2.5.1" % "test->default"
  // We need all mock_classes_ext libraries inorder to use ClassMocker class for mocking classes
  val mock_classes_ext1 = "cglib" % "cglib" %"2.1_3" % "test->default"
  val mock_classes_ext2 = "org.objenesis" % "objenesis" %"1.0" % "test->default"
  val mock_classes_ext3 = "org.jmock" % "jmock-legacy" %"2.5.1" % "test->default"
  val specs = "org.scala-tools.testing" % "specs_2.8.1" % "1.6.6" % "test->default"
 // TODO: Uncomment this line after the jetty-run error caused by selenium is fixed: ExceptionInInitializerError
 /*val selenium = "org.seleniumhq.selenium" % "selenium" % "2.0b1" % "test->default"
 val seleniumRemote = "org.seleniumhq.selenium" % "selenium-remote-control" % "1.0.1" % "test->default" */

  override def localScala = defineScala("2.8.1-local", ("scala" / "build" / "pack").asFile) :: Nil

  override def managedStyle = ManagedStyle.Maven

  override def jettyWebappPath = webappPath

  override def scanDirectories = Nil
}
