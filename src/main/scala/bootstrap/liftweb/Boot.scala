/*
 * Copyright (c) 2010, Petr Hosek. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 *     the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COLLABORATIVE SCALADOC PROJECT ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COLLABORATIVE SCALADOC
 * PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.util.Helpers._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.http.js.JsCmds._
import _root_.net.liftweb.http.js.JE.JsRaw
import _root_.net.liftweb.http.js.jquery.JQuery14Artifacts
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.mapper.{DB, Schemifier, DefaultConnectionIdentifier, StandardDBVendor}

import tools.colladoc.api.ExportService
import tools.colladoc.lib.sitemap.{HistoryStuff, SearchStuff, IndexStuff, TemplateStuff}
import tools.colladoc.model.mapper.{User, Comment}
import tools.colladoc.lib.js.JqUI._

import xml.{Text, NodeSeq}
import tools.nsc.io.Streamable

/**
 * A class that's instantiated early and run.  It allows the application to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			     Props.get("db.url") openOr "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			     Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // where to search snippet
    LiftRules.addToPackages("scala.tools.colladoc")
    Schemifier.schemify(true, Schemifier.infoF _, User, Comment)

    LiftRules.dispatch.prepend(scaladocResources)

    // Build SiteMap
    def sitemap() = SiteMap(
      Menu(IndexStuff),
      Menu(HistoryStuff),
      Menu(SearchStuff),
      Menu(TemplateStuff)
      )

    LiftRules.setSiteMapFunc(sitemap)
    LiftRules.statelessDispatchTable.append(ExportService)

    // Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.noticesToJsCmd = notices _

    LiftRules.early.append(makeUtf8)
    LiftRules.jsArtifacts = JQuery14Artifacts    

    // Set the doctype to XHTML 1.0 Transitional
    LiftRules.docType.default.set { (req: Req) =>
      req match {
        case _ if S.getDocType._1 => S.getDocType._2
        case _ => Full(DocType.xhtmlTransitional)
      }
    }

    LiftRules.SnippetFailures
    LiftRules.determineContentType = {
      case _ => "text/html"
    }

    LiftRules.exceptionHandler.prepend {
      case (_, r, e) => Error.render(r, e)
    }
  }

  object Error {
    private val fakeSession = new LiftSession("/", "fakeSession", Empty)
    
    def render(req: Req, ex: Throwable): LiftResponse = {
      val xml: NodeSeq = S.init(req, fakeSession) {
        S.runTemplate(List("error")) openOr NodeSeq.Empty
      }
      val out = bind("error", xml,
        "message" -> Text(ex.getMessage),
        "trace" -> Text(ex.getStackTraceString))
      XhtmlResponse(out(0), LiftRules.docType.vend(req), List("Content-Type" -> "text/html; charset=utf-8"), Nil, 500, false)
    }
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

  private def scaladocResources() = new LiftRules.DispatchPF {
    def functionName = "Scaladoc resources"

    def isDefinedAt(req: Req): Boolean = req.path match {
      case ParsePath("lib" :: resource, _, _, _) => true
      case _ => false
    }

    def apply(req: Req): () => Box[LiftResponse] = {
      val path = req.path.partPath.mkString("/", "/", if (req.path.suffix.nonEmpty) "." + req.path.suffix else "")
      val stream = getClass.getResourceAsStream("/scala/tools/nsc/doc/html/resource" + path)
      () => req.path match {
        case ParsePath(_, "css", _, _) =>
          Full(CSSResponse(new Streamable.Chars { val inputStream = stream }.slurp))
        case ParsePath(_, "js", _, _) =>
          Full(JavaScriptResponse(JsRaw(new Streamable.Chars { val inputStream = stream }.slurp)))
        case ParsePath(_, "png", _, _) =>
          val bytes = new Streamable.Bytes { val inputStream = stream }.toByteArray
          Full(InMemoryResponse(bytes, List("Content-Type" -> "image/png", "Content-Length" -> bytes.length.toString), Nil, 200))
        case ParsePath(_, _, _, _) =>
          Full(InMemoryResponse(new Streamable.Bytes { val inputStream = stream }.toByteArray, S.getHeaders(Nil), S.responseCookies, 200))
      }
    }
  }

  private def notices() = {
    import NotificationType._
    
    def cmds(msgs: List[(NodeSeq, Box[String])], _type: Type) =
      msgs.foldLeft(Noop) { (c, m) => c & (m match {
        case (Text(t), Full(h)) => Notify(_type, h)
        case (Text(t), _) => Notify(_type, t)
        case (n, Full(h)) => Notify(_type, n.toString, h, false)
        case (n, _) => Notify(_type, n.toString, hide = false)
      })}

    cmds(S.notices, notice) & cmds(S.warnings, error) & cmds(S.errors, error)
  }
}
