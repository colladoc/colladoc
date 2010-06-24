package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, StandardDBVendor}
import _root_.java.sql.{Connection, DriverManager}
import _root_.scala.tools.colladoc.model._
import tools.colladoc.lib.{IndexStuff, TemplateStuff}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("scala.tools.colladoc")

    // Build SiteMap
    def sitemap() = SiteMap(
      Menu(IndexStuff),
      Menu(TemplateStuff)
      )

    LiftRules.setSiteMapFunc(sitemap)

    // Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

    // Set the doctype to XHTML 1.1
    LiftRules.docType.default.set { (req: Req) =>
      req match {
        case _ if S.getDocType._1 => S.getDocType._2
        case _ => Full(DocType.xhtml11)
      }
    }

    Model.init()
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
