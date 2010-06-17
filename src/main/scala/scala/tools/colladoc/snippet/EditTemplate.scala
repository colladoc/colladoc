package scala.tools.colladoc.snippet

import tools.colladoc.model.Model
import tools.nsc.doc.html.page.Template
import tools.nsc.io.File
import reflect.NameTransformer
import java.io.{ File => JFile }
import tools.colladoc.lib.{LiftPaths, DependencyFactory}
import tools.nsc.doc.model.{MemberEntity, NonTemplateMemberEntity, Package, DocTemplateEntity}
import net.liftweb.http.{SHtml, S}
import net.liftweb.http.jquery.JqSHtml
import xml.{Text, NodeSeq}
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.{Replace, SetHtml}

class EditTemplate(tpl: DocTemplateEntity) extends Template(tpl) {

  override def memberToCommentBodyHtml(mbr: MemberEntity, isSelf: Boolean) =
    <xml:group>
      <div id={ mbr.qualifiedName.toLowerCase }>
        { SHtml.a(doEdit(mbr, isSelf) _, Text("Edit"), ("class", "edit")) }
        { super.memberToCommentBodyHtml(mbr, isSelf) }
      </div>
    </xml:group>

  def doEdit(mbr: MemberEntity, isSelf: Boolean)(): JsCmd = {
    Replace(mbr.qualifiedName.toLowerCase,
      <form id="editfrm" class="edit" method="GET">
        { SHtml.textarea(mbr.comment.get.toString, text => Console.println(Model.factory.modelFactory.parse(text).toString)) }<br/>
        <div class="buttons">
          { SHtml.a(() => SHtml.submitAjaxForm("editfrm", () => Replace("editfrm", <p></p>)), Text("Save")) }
          { SHtml.a(() => Replace("editfrm", memberToCommentBodyHtml(mbr, isSelf)), Text("Cancel")) }
        </div>
      </form>)
  }

}