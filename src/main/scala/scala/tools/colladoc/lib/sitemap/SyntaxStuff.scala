package scala.tools.colladoc.lib.sitemap

import net.liftweb.sitemap.Loc
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest}
import net.liftweb.util.NamedPF
import net.liftweb.common.Full
import xml.Text

/** Search location parameter. */
case class SyntaxLoc()

/**
 * Search sitemap location.
 */
object SyntaxStuff extends Loc[SyntaxLoc] {

  /** The name of the page. */
  def name = "syntax"

  /** The default parameters (used for generating the menu listing). */
  def defaultValue = Full(SyntaxLoc())

  /** Parameters. */
  def params = List.empty

  /** Text of the link. */
  val text = new Loc.LinkText((loc: SyntaxLoc) => Text("Syntax"))

  /** Generate a link based on the current page. */
  val link = new Loc.Link[SyntaxLoc](List("syntax"))

  /** Rewrite location. */
   override val rewrite: LocRewrite = Full(NamedPF("Syntax Rewrite") {
    case RewriteRequest(ParsePath("syntax" :: Nil, "html", _, _), _, _) =>
      (RewriteResponse("syntax" :: Nil), SyntaxLoc())
  })
}