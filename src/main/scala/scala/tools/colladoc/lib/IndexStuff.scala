package scala.tools.colladoc.lib

import net.liftweb.sitemap.Loc
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest}
import net.liftweb.util.NamedPF
import xml.Text
import net.liftweb.common.Full

case class IndexLoc(path: List[String])

object IndexStuff extends Loc[IndexLoc] {

  /** The name of the page */
  def name = "index"

  /** The default parameters (used for generating the menu listing) */
  def defaultValue = Full(IndexLoc(List("index")))

  /** Parameters */
  def params = List.empty

  /** Text of the link */
  val text = new Loc.LinkText((loc: IndexLoc) => Text("Index"))

  /** Generate a link based on the current page */
  val link = new Loc.Link[IndexLoc](List("index"))

  override val rewrite: LocRewrite = Full(NamedPF("Index Rewrite") {
    case RewriteRequest(ParsePath("index" :: Nil, "html", _, _), _, _) =>
      (RewriteResponse("index" :: Nil, Map("path" -> "/")), IndexLoc("index" :: Nil))
  })

}