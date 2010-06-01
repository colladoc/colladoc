import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val githubRepo = "github" at "http://mpeltonen.github.com/maven/"
  val idea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.1-SNAPSHOT"
}
