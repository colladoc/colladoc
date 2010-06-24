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

import tools.colladoc.model.Model
import tools.nsc.doc.html.page.Template
import java.io.{ File => JFile }
import tools.colladoc.lib.{LiftPaths, DependencyFactory}
import tools.nsc.doc.model.{MemberEntity, NonTemplateMemberEntity, Package, DocTemplateEntity}
import net.liftweb.http.{SHtml, S}
import net.liftweb.http.jquery.JqSHtml
import net.liftweb.http.js.jquery.JqJE.JqId
import net.liftweb.http.js.JE.{Str, JsFunc, JsRaw}
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http.js.JsCmds.{Run, Replace, SetHtml}
import net.liftweb.http.js.jquery.JqJsCmds
import tools.colladoc.lib.XmlUtils._
import xml._

class UpdatableTemplate(tpl: DocTemplateEntity) extends Template(tpl) {

  def id(mbr: MemberEntity, pos: String) =
    "%s_%s".format(mbr.qualifiedName.replaceAll("[\\.\\#]", "_"), pos)

  override def memberToShortCommentHtml(mbr: MemberEntity, isSelf: Boolean): NodeSeq =
    super.memberToShortCommentHtml(mbr, isSelf) theSeq match {
      case Seq(elem: Elem, rest @ _*) => elem % Map("id" -> id(mbr, "shortcomment"))
    }

  override def memberToCommentBodyHtml(mbr: MemberEntity, isSelf: Boolean) =
    <div id={ id(mbr, "comment") }>
      { SHtml.a(doEdit(mbr, isSelf) _, Text("Edit"), ("class", "edit")) }
      { super.memberToCommentBodyHtml(mbr, isSelf) }
    </div>

  def doEdit(mbr: MemberEntity, isSelf: Boolean)(): JsCmd = {
    Replace(id(mbr, "comment"),
      <form id={ id(mbr, "form") } class="edit" method="GET">
        <div class="editor">
          { SHtml.textarea(mbr.comment.get.source.getOrElse(""), text => update(mbr, text)) }<br/>
          <div class="buttons">
            { SHtml.a(() => SHtml.submitAjaxForm(id(mbr, "form"), () => save(mbr, isSelf)), Text("Save")) }
            { SHtml.a(() => cancel(mbr, isSelf), Text("Cancel")) }
          </div>
        </div>
      </form>)
  }

  def save(mbr: MemberEntity, isSelf: Boolean) =
    Replace(id(mbr, "form"), memberToCommentBodyHtml(mbr, isSelf)) &
    (if (isSelf)
      JsCmds.Noop
    else
      SetHtml(id(mbr, "shortcomment"), inlineToHtml(mbr.comment.get.short)))

  def cancel(mbr: MemberEntity, isSelf: Boolean) =
    Replace(id(mbr, "form"), memberToCommentBodyHtml(mbr, isSelf))

  def update(mbr: MemberEntity, text: String) =
    Model.factory.update(mbr, text)

}