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
package scala.tools.colladoc
package model
package comment

import model.{Model, User}
import tools.nsc.doc.html.page.Template
import java.io.{ File => JFile }
import tools.colladoc.lib.{LiftPaths, DependencyFactory}
import tools.nsc.doc.model.{MemberEntity, NonTemplateMemberEntity, Package, DocTemplateEntity}
import net.liftweb.http.{SHtml, S}
import net.liftweb.http.jquery.JqSHtml
import net.liftweb.http.js.JE.{Str, JsFunc, JsRaw}
import net.liftweb.http.js.JsCmds.{Run, Replace, SetHtml}
import tools.colladoc.lib.XmlUtils._
import net.liftweb.http.js.jquery.JqJE._
import xml.{Text, Elem, NodeSeq}
import net.liftweb.http.js._
import net.liftweb.http.js.jquery.JqJsCmds._
import java.util.Date

class UpdatableTemplate(tpl: DocTemplateEntity) extends Template(tpl) {

  private def id(mbr: MemberEntity, pos: String) =
    "%s_%s".format(mbr.qualifiedName.replaceAll("[\\.\\#]", "_"), pos)

  override def memberToShortCommentHtml(mbr: MemberEntity, isSelf: Boolean): NodeSeq =
    super.memberToShortCommentHtml(mbr, isSelf) \\% Map("id" -> id(mbr, "shortcomment"))

  override def memberToCommentBodyHtml(mbr: MemberEntity, isSelf: Boolean) =
    <div id={ id(mbr, "comment") }>
      { super.memberToCommentBodyHtml(mbr, isSelf) }
    </div>

  override def signature(mbr: MemberEntity, isSelf: Boolean): NodeSeq = {
    def getSignature(mbr: MemberEntity, isSelf: Boolean): NodeSeq =
      if (User.loggedIn_?)
        super.signature(mbr, isSelf) \\+ edit(mbr, isSelf)
      else
        super.signature(mbr, isSelf)
    mbr match {
      case dte: DocTemplateEntity if isSelf => getSignature(mbr, isSelf) \\+ export(mbr)
      case dte: DocTemplateEntity if mbr.comment.isDefined => super.signature(mbr, isSelf)
      case _ => getSignature(mbr, isSelf) \\+ export(mbr)
    }
  }

  private def edit(mbr: MemberEntity, isSelf: Boolean) = {
    SHtml.a(doEdit(mbr, isSelf) _, Text("Edit"), ("class", "control edit"))
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
      </form>) & markItUp(id(mbr, "text")) & button("button")
  }

  private def markItUp(id: String) =
    JqId(Str(id)) ~> new JsMember { def toJsCmd = "markItUp(markItUpSettings)" }

  private def button(id: String) =
    Jq(Str(id)) ~> new JsMember { def toJsCmd = "button()" }

  private def save(mbr: MemberEntity, isSelf: Boolean) =
    Replace(id(mbr, "form"), memberToCommentBodyHtml(mbr, isSelf)) &
    (if (isSelf)
      JsCmds.Noop
    else
      SetHtml(id(mbr, "shortcomment"), inlineToHtml(mbr.comment.get.short)))

  private def cancel(mbr: MemberEntity, isSelf: Boolean) =
    Replace(id(mbr, "form"), memberToCommentBodyHtml(mbr, isSelf))

  private def update(mbr: MemberEntity, text: String) =
    Model.factory.update(mbr, text)

  private def export(mbr: MemberEntity) =
    SHtml.a(doExport(mbr) _, Text("Export"), ("class", "control"))

  private def doExport(mbr: MemberEntity)(): JsCmd = {
    def memberPath(mbr: MemberEntity) = (mbr match {
        case dte: DocTemplateEntity => templateToPath(tpl).mkString("/").dropRight(5)
        case _ => templateToPath(mbr.inTemplate).mkString("/").dropRight(5) + "/" + mbr.name
      }) + ".xml"
    JsRaw("window.open('%s', 'Export')" format memberPath(mbr))
  }

}