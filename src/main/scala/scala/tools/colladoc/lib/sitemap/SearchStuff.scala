package scala.tools.colladoc.lib.sitemap

import net.liftweb.sitemap.Loc
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest}
import net.liftweb.util.NamedPF
import net.liftweb.common.Full
import xml.Text

/** Search location parameter. */
case class SearchLoc()

/**
 * Search sitemap location.
 */
object SearchStuff extends Loc[SearchLoc] {

  /** The name of the page. */
  def name = "search"

  /** The default parameters (used for generating the menu listing). */
  def defaultValue = Full(SearchLoc())

  /** Parameters. */
  def params = List.empty

  /** Text of the link. */
  val text = new Loc.LinkText((loc: SearchLoc) => Text("Search"))

  /** Generate a link based on the current page. */
  val link = new Loc.Link[SearchLoc](List("search"))

  /** Rewrite location. */
  override val rewrite: LocRewrite = Full(NamedPF("Search Rewrite") {
    case RewriteRequest(ParsePath("search" :: Nil, "html", _, _), _, _) =>
      (RewriteResponse("search" :: Nil), SearchLoc())
  })

}