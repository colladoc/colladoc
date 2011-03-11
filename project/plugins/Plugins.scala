import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val github = "GitHub" at "http://petrh.github.com/m2/"
  val winstone = "com.github.petrh" % "sbt-winstone-plugin" % "1.0-SNAPSHOT"
  
  val scctRepo = "scct-repo" at "http://mtkopone.github.com/scct/maven-repo/"
  lazy val scctPlugin = "reaktor" % "sbt-scct-for-2.8" % "0.1-SNAPSHOT"
}
