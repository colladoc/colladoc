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
package scala.tools.colladoc {
package lib {
package sitemap {

import net.liftweb.sitemap.Loc
import net.liftweb.util.NamedPF
import net.liftweb.common.Full
import net.liftweb.http.{S, RewriteResponse, ParsePath, RewriteRequest}

import xml.Text

/** Template location parameter. */
case class TemplateLoc(path: List[String])

/**
 * Template sitemap location.
 */
object TemplateStuff extends Loc[TemplateLoc] {

  /** The name of the page. */
  def name = "template"

  /** The default parameters (used for generating the menu listing). */
  def defaultValue = Full(TemplateLoc(List("template")))

  /** Parameters. */
  def params = List.empty

  /** Text of the link. */
  val text = new Loc.LinkText((loc: TemplateLoc) => Text("Template"))

  /** Generate a link based on the current page. */
  val link = new Loc.Link[TemplateLoc](List("template"))

  /** Rewrite location. */
  override val rewrite: LocRewrite = Full(NamedPF("Template Rewrite") {
    case RewriteRequest(ParsePath(path, "html", _, _), _, _) =>
      (RewriteResponse("template" :: Nil, Map("path" -> path.mkString("/"))), TemplateLoc(path))
  })

  /** Snippets */
  override val snippets: SnippetTest = {
    case ("template", Full(TemplateLoc(path))) =>
      DependencyFactory.path.doWith(path.toArray) { S.locateSnippet("template").open_! }
  }

}

}
}
}