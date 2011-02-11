import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val github = "GitHub" at "http://petrh.github.com/m2/"
  val winstone = "com.github.petrh" % "sbt-winstone-plugin" % "1.0-SNAPSHOT"

  val bryanSwift = "Bryan Swift" at "http://repos.bryanjswift.com/maven2"
 // TODO: Uncomment this line after the jetty-run error caused by selenium is fixed: ExceptionInInitializerError
 // val seleniumServer = "bryanjswift" % "selenium-sbt" % "0.0.4"
}
