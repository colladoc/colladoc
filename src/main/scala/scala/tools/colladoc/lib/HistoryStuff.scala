package scala.tools.colladoc.lib

import net.liftweb.sitemap.Loc
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest}
import net.liftweb.util.NamedPF
import xml.Text
import net.liftweb.common.Full

case class HistoryLoc(path: List[String])

object HistoryStuff extends Loc[IndexLoc] {

  /** The name of the page */
  def name = "history"

  /** The default parameters (used for generating the menu listing) */
  def defaultValue = Full(IndexLoc(List("history")))

  /** Parameters */
  def params = List.empty

  /** Text of the link */
  val text = new Loc.LinkText((loc: IndexLoc) => Text("History"))

  /** Generate a link based on the current page */
  val link = new Loc.Link[IndexLoc](List("history"))

  override val rewrite: LocRewrite = Full(NamedPF("History Rewrite") {
    case RewriteRequest(ParsePath("history" :: Nil, "html", _, _), _, _) =>
      (RewriteResponse("history" :: Nil, Map("path" -> "/")), IndexLoc("history" :: Nil))
  })

}