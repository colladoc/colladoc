import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val github = "GitHub" at "http://petrh.github.com/m2/"
  val winstone = "com.github.petrh" % "sbt-winstone-plugin" % "1.0-SNAPSHOT"
}
