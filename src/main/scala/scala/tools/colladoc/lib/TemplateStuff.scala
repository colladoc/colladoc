package scala.tools.colladoc.lib

import net.liftweb.sitemap.Loc
import net.liftweb.util.NamedPF
import net.liftweb.common.Full
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest}
import xml.Text

case class TemplateLoc(path: List[String])

object TemplateStuff extends Loc[TemplateLoc] {

  /** The name of the page */
  def name = "template"

  /** The default parameters (used for generating the menu listing) */
  def defaultValue = Full(TemplateLoc(List("template")))

  /** Parameters */
  def params = List.empty

  /** Text of the link */
  val text = new Loc.LinkText((loc: TemplateLoc) => Text("Template"))

  /** Generate a link based on the current page */
  val link = new Loc.Link[TemplateLoc](List("template"))

  override val rewrite: LocRewrite = Full(NamedPF("Template Rewrite") {
    case RewriteRequest(ParsePath(path, "html", _, _), _, _) =>
      (RewriteResponse("template" :: Nil, Map("path" -> path.mkString("/"))), TemplateLoc(path))
  })

}