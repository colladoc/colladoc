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
package page {

import model._
import model.Model.factory._
import model.comment.DynamicModelFactory
import lib.Helpers._
import lib.JsCmds._

import net.liftweb.common.Full
import net.liftweb.http.{SHtml, S}
import net.liftweb.http.js._
import net.liftweb.http.js.jquery.JqJE._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._

import tools.nsc.doc.model._
import xml.{NodeSeq, Node, Elem, Text}

class Template(tpl: DocTemplateEntity) extends tools.nsc.doc.html.page.Template(tpl) {

  private def id(mbr: MemberEntity, pos: String) =
    "%s_%s".format(htmlAttributeEncode(mbr.identifier), pos)

  override def memberToShortCommentHtml(mbr: MemberEntity, isSelf: Boolean): NodeSeq =
    super.memberToShortCommentHtml(mbr, isSelf) \\% Map("id" -> id(mbr, "shortcomment"))

  override def memberToCommentBodyHtml(mbr: MemberEntity, isSelf: Boolean) =
    <div id={ id(mbr, "comment") }>
      { content(mbr, isSelf) }
      { controls(mbr, isSelf) }
    </div>

  private def content(mbr: MemberEntity, isSelf: Boolean) =
    <div id={ id(mbr, "content") }>
      { super.memberToCommentBodyHtml(mbr, isSelf) }
    </div>

  private def controls(mbr: MemberEntity, isSelf: Boolean) =
    <div class="controls">
      { select(mbr, isSelf) }
      { if (User.loggedIn_?)
          edit(mbr, isSelf)
      }
      { export(mbr, isSelf) }
    </div>

  private def edit(mbr: MemberEntity, isSelf: Boolean) = {
    SHtml.a(doEdit(mbr, isSelf) _, Text("Edit"), ("class", "button"))
  }

  private def doEdit(mbr: MemberEntity, isSelf: Boolean)(): JsCmd = {
    def getSource(mbr: MemberEntity) = mbr.comment match {
        case Some(c) => c.source.getOrElse("")
        case None => ""
      }
    Replace(id(mbr, "comment"),
      <form id={ id(mbr, "form") } class="edit" method="GET">
        <div class="editor">
          { SHtml.textarea(getSource(mbr), text => update(mbr, text), ("id", id(mbr, "text"))) }
          <div class="buttons">
            { SHtml.ajaxButton(Text("Save"), () => SHtml.submitAjaxForm(id(mbr, "form"), () => save(mbr, isSelf))) }
            { SHtml.a(Text("Cancel"), cancel(mbr, isSelf)) }
          </div>
        </div>
      </form>) &
        JqId(Str(id(mbr, "text"))) ~> Editor() &
        Jq(Str("button")) ~> Button()
  }

  private def save(mbr: MemberEntity, isSelf: Boolean): JsCmd =
    if (Model.reporter.hasWarnings || Model.reporter.hasErrors)
      JqId(Str(id(mbr, "text"))) ~> AddClass("ui-state-error")
    else
      Replace(id(mbr, "form"), memberToCommentBodyHtml(mbr, isSelf)) &
        (if (!isSelf) SetHtml(id(mbr, "shortcomment"), inlineToHtml(mbr.comment.get.short)) else JsCmds.Noop) &
        Jq(Str("#" + id(mbr, "comment") + " .button")) ~> Button() &
        Jq(Str("#" + id(mbr, "comment") + " .select")) ~> SelectMenu()

  private def cancel(mbr: MemberEntity, isSelf: Boolean): JsCmd =
    Replace(id(mbr, "form"), memberToCommentBodyHtml(mbr, isSelf)) &
      Jq(Str("#" + id(mbr, "comment") + " .button")) ~> Button() &
      Jq(Str("#" + id(mbr, "comment") + " .select")) ~> SelectMenu()

  private def update(mbr: MemberEntity, text: String) = Model.synchronized {
    Model.reporter.reset
    Model.factory.update(mbr, text)
  }

  private def select(mbr: MemberEntity, isSelf: Boolean) = {
    def replace(cid: String) = {
      Comment.find(cid) match {
        case Full(c) =>
          val comment = Model.factory.parse(mbr.symbol.get, mbr.template.get, c.comment.is)
          val entity = DynamicModelFactory.createMember(mbr, comment)
          Replace(id(entity, "content"), content(entity, isSelf))&
            (if (!isSelf) SetHtml(id(mbr, "shortcomment"), inlineToHtml(comment.short)) else JsCmds.Noop)
        case _ => JsCmds.Noop
      }
    }
    Comment.select(mbr.qualifiedIdentifier, replace _)
  }

  private def export(mbr: MemberEntity, isSelf: Boolean) = mbr match {
    case tpl: DocTemplateEntity if tpl.isPackage =>
      <xml:group>
        { SHtml.a(doExport(mbr, isSelf, false) _, Text("Export"), ("class", "control")) }
        { SHtml.a(doExport(mbr, isSelf, true) _, Text("Export Recursively"), ("class", "control")) }
      </xml:group>
    case _ =>
      SHtml.a(doExport(mbr, isSelf, false) _, Text("Export"), ("class", "control"))
  }

  private def doExport(mbr: MemberEntity, isSelf: Boolean, recursive: Boolean)(): JsCmd = {
    val path = memberToPath(mbr, isSelf) + ".xml" + (if (recursive) "?recursive=true" else "")
    JsRaw("window.open('%s', 'Export')" format (path))
  }

}

}
}