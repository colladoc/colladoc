package scala.tools.colladoc.lib

import java.io.File
import tools.nsc.io.Directory
import net.liftweb.util.Props

object ColladocSettings {

  def title =
    Props.get("site.title") openOr "Colladoc"

  def getSources: List[String] =
    getSources(new File(Props.get("source.path") openOr "."))

  def getSources(file: File): List[String] =
    (new Directory(file)).deepFiles.filter{ _.extension == "scala" }.map{ _.path }.toList

  def getClassPath =
    Props.get("class.path") openOr ""

}