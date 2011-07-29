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

import lib.util.Helpers._
import lib.util.NameUtils._
import lib.js.JqJsCmds._
import lib.js.JqUI._
import lib.widgets.Editor
import model.Model
import model.Model.factory._
import model.mapper._

import net.liftweb.common._
import net.liftweb.http.{S, SHtml}
import net.liftweb.http.js._
import net.liftweb.http.js.jquery.JqJE._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._

import tools.nsc.doc.model._
import xml.{NodeSeq, Text}
import lib.DependencyFactory._
import tools.nsc.util.NoPosition
import net.liftweb.widgets.gravatar.Gravatar

/**
 * Page containing template entity documentation and user controls.
 * @author Petr Hosek
 */
class Template(tpl: DocTemplateEntity) extends tools.nsc.doc.html.page.Template(tpl) {
  /**
   * Create unique identifier for given entity and position.
   * @param mbr member entity
   * @param pos identifier position
   * @return unique positional identifier
   */
  private def id(mbr: MemberEntity, pos: String) =
    idAttrEncode(hash(mbr.identifier + System.identityHashCode(mbr) + pos))

  /**
   * Create unique identifier for given name.
   * @param name identifier position
   * @return unique positional identifier
   */
  private def id(name: String) =
    idAttrEncode(hash(name))

  override def body =
    <body class={ if (tpl.isTrait || tpl.isClass || tpl.qualifiedName == "scala.AnyRef") "type" else "value" } onload="windowTitle();">

      { if (tpl.isRootPackage || tpl.inTemplate.isRootPackage)
          NodeSeq.Empty
        else
          <p id="owner">{ templatesToHtml(tpl.inTemplate.toRoot.reverse.tail, xml.Text(".")) }</p>
      }

      <div id="definition">
        <img src={ relativeLinkTo(List(docEntityKindToBigImage(tpl), "lib")) }/>
        <h1>{ if (tpl.isRootPackage) "root package" else tpl.name }</h1>
      </div>

      { signature(tpl, true) }
      { memberToCommentHtml(tpl, true) }

      <div id="template">

        <div id="mbrsel">
          <div id='textfilter'><span class='pre'/><span class='input'><input type='text' accesskey='/'/></span><span class='post'/></div>
          { if (tpl.linearization.isEmpty) NodeSeq.Empty else
              <div id="order">
                <span class="filtertype">Ordering</span>
                <ol><li class="alpha in">Alphabetic</li><li class="inherit out">By inheritance</li></ol>
              </div>
          }
          { if (tpl.linearization.isEmpty) NodeSeq.Empty else
              <div id="ancestors">
                <span class="filtertype">Inherited</span>
                <ol><li class="hideall">Hide All</li><li class="showall">Show all</li></ol>
                <ol id="linearization">{ (tpl :: tpl.linearizationTemplates) map { wte => <li class="in" name={ wte.qualifiedName }>{ wte.name }</li> } }</ol>
              </div>
          }
          {
            <div id="visbl">
              <span class="filtertype">Visibility</span>
              <ol><li class="public in">Public</li><li class="all out">All</li></ol>
            </div>
          }
          {
            <div id="impl">
              <span class="filtertype">Impl.</span>
              <ol><li class="concrete in">Concrete</li><li class="abstract in">Abstract</li></ol>
            </div>
          }
        </div>

        { discussions }

        { if (constructors.isEmpty) NodeSeq.Empty else
            <div id="constructors" class="members">
              <h3>Instance constructors</h3>
              <ol>{ constructors map (memberToHtml(_)) }</ol>
            </div>
        }

        { if (typeMembers.isEmpty) NodeSeq.Empty else
            <div id="types" class="types members">
              <h3>Type Members</h3>
              <ol>{ typeMembers map (memberToHtml(_)) }</ol>
            </div>
        }

        { if (valueMembers.isEmpty) NodeSeq.Empty else
            <div id="values" class="values members">
              <h3>Value Members</h3>
              <ol>{ valueMembers map (memberToHtml(_)) }</ol>
            </div>
        }

        {
          NodeSeq fromSeq (for ((superTpl, superType) <- tpl.linearization) yield
            <div class="parent" name={ superTpl.qualifiedName }>
              <h3>Inherited from {
                if (tpl.universe.settings.useStupidTypes.value)
                  superTpl match {
                    case dtpl: DocTemplateEntity =>
                      val sig = signature(dtpl, false, true) \ "_"
                      sig
                    case tpl: TemplateEntity =>
                      tpl.name
                  }
                else
                  typeToHtml(superType, true)
              }</h3>
            </div>
          )
        }

      </div>

      <div id="tooltip" ></div>

    </body>

  private def discussions =
    <div id="discussions">
      <h3 id="discussions_header" class="header">Discussions</h3>
      <div id="discussions_wrapper">
        { categories }
      </div>
    </div>

  /** Render discussion block. */
  private def categories: NodeSeq =
    Category.all map categoryToHtmlWithToggle _

  private def categoryToHtmlWithToggle(c: Category) = {
    val toggle =
      """
        $(document).ready(function() {
          $('#%s').live('click', function(){
            $('#%s').slideToggle(100);
          });
        });
      """ format (id(c.name.is + "header"), id(c.name.is + "wrapper"))

    <xml:group>
      { categoryToHtml(c) }
      <script type="text/javascript">
        { toggle }
      </script>
    </xml:group>
  }

  private def categoryToHtml(c: Category) =
    <div id={ id(c.name.is) } class="category">
      <h3 id={ id(c.name.is + "header") } class="header">{ c.name }</h3>
      <div id={ id(c.name.is + "wrapper") } class="wrapper">
        <ul class="discussion_thread">
        {
          Discussion.topLevelComments(c, tpl.qualifiedName) map {
            d => discussionToHtmlWithActions(d, 0) }
        }
      </ul>
        { if (!User.banned_?) discussionCommentAddButton(c) }
      </div>
    </div>

  /** Render discussion comment. */
  def discussionToHtml(d: Discussion, level: Int = 0, withReplies: Boolean = false) = {
    val replies = Discussion.replies(Category.get(d), d)

    <xml:group>
      {
        if (d.valid) {
          <li id={"discussion_comment_" + d.id} class={"discussion_comment discussion_level_" + level}>
            <div class="discussion_avatar">{Gravatar(d.authorEmail, 20)}</div>
            <div class="discussion_content">{bodyToHtml(parseWiki(d.comment.is, NoPosition))}</div>
            <div class="discussion_info">
              <span class="datetime" title={d.atomDateTime}>{d.humanDateTime}</span>
              by
              <span class="author">{d.authorProfileHyperlink}</span>
              <discussion_comment:link />
              <div class="discussion_comment_actions">
                <discussion_comment:reply />
                <discussion_comment:edit />
                <discussion_comment:delete />
              </div>
            </div>
            <div id={"reply_for_" + d.id} />

          </li>
        } else
          if (replies.filter(d => d.valid).length > 0)
            <li class={"discussion_comment discussion_level_" + level}>
              <div class="discussion_content discussion_deleted">Comment deleted</div>
            </li>
      }
      {
        if (withReplies)
          replies.map(d => discussionToHtmlWithActions(d, if (level < 3) level + 1 else 4))
      }
    </xml:group>
  }

  /** Render discussion comment with actions. */
  private def discussionToHtmlWithActions(d: Discussion, level: Int = 0): NodeSeq = bind("discussion_comment",
    discussionToHtml(d, level, true),
    "edit"   -> { if (User.validSuperUser_?) editDiscussionButton(d)   else NodeSeq.Empty },
    "delete" -> { if (User.validSuperUser_?) deleteDiscussionButton(d) else NodeSeq.Empty },
    "reply"  -> { if (!User.banned_?)   replyDiscussionButton(d)  else NodeSeq.Empty }
  )

  /** Render add comment button. */
  private def discussionCommentAddButton(category: Category) = {
    SHtml.ajaxButton(Text("Add comment"), discussionEditor(category, None) _, ("class", "button"), ("id", id(category.name.is + "add_discussion_button")))
  }

  /** Render editor. */
  private def discussionEditor(category: Category, maybe: Option[Discussion] = None)(): JsCmd = {
    maybe match {
      case Some(d) =>
        Editor.editorObj(d.comment.is, preview _, updateDiscussionComment(d) _) match {
          case (n, j) =>
            Replace("discussion_comment_" + d.id,
              <form id={"edit_form_" + d.id} class="edit" method="GET">
                <div class="editor">
                  { n }
                  <div class="buttons">
                    { SHtml.ajaxButton(Text("Save"), () => SHtml.submitAjaxForm("edit_form_" + d.id, () => reloadDiscussion(category))) }
                    { SHtml.a(Text("Cancel"),
                        Replace("edit_form_" + d.id, discussionToHtmlWithActions(d)) &
                        PrettyDate &
                        Jq(Str("button")) ~> Button(),
                        ("class", "button"))
                    }
                  </div>
                </div>
              </form>) & j & Jq(Str("button")) ~> Button()
          case _ => JsCmds.Noop
      }
      case None =>
        Editor.editorObj("", preview _, text => saveDiscussionComment(category, text)) match {
          case (n, j) =>
            val formId = id(category.name.is + "discussion_form")
            Replace(id(category.name.is + "add_discussion_button"),
              <form id={ formId } class="edit" method="GET">
                <div class="editor">
                  { n }
                  <div class="buttons">
                    { SHtml.ajaxButton(Text("Save"), () => SHtml.submitAjaxForm(formId, () => reloadDiscussion(category))) }
                    { SHtml.a(Text("Cancel"),
                        Replace(formId, discussionCommentAddButton(category)) &
                        PrettyDate &
                        Jq(Str("button")) ~> Button(),
                        ("class", "button"))
                    }
                  </div>
                </div>
              </form>) & j & Jq(Str("button")) ~> Button()
          case _ => JsCmds.Noop
      }
    }
  }

  /** Reload discussion block after new comment adding. */
  private def reloadDiscussion(category: Category) =
    Replace(id(category.name.is), categoryToHtml(category)) &
    JsRaw("$('#" + id(category.name.is + "wrapper") + "').toggle();") &
    PrettyDate &
    Jq(Str("button")) ~> Button()

  /** Save discussion comment to database. */
  private def saveDiscussionComment(category: Category, text: String, parent: Option[Discussion] = None) {
    val d = Discussion.create.category(category).qualifiedName(tpl.qualifiedName).comment(text).dateTime(now).user(User.currentUser.open_!).valid(true)
    parent match {
      case Some(p) => d.parent(p)
      case _ =>
    }
    d.save
  }

  /** Parse input string to show comment preview. */
  private def preview(text: String) = {
    <html>
      <head>
        <link href="/lib/template.css" media="screen" type="text/css" rel="stylesheet" />
        <link href="/copreview.css" media="screen" type="text/css" rel="stylesheet" />
      </head>
      <body>
        <div id="preview">
          { bodyToHtml(parseWiki(text, NoPosition)) }
        </div>
      </body>
    </html>
  }

  /** Render delete button for discussion comment. */
  def deleteDiscussionButton(d: Discussion) = SHtml.a(
    ColladocConfirm("Confirm delete"), () => {d.valid(false).save; reloadDiscussion(Category.get(d))}, Text("Delete"))

  /** Render delete button for discussion comment. */
  def editDiscussionButton(d: Discussion) = SHtml.a(discussionEditor(Category.get(d), Some(d)) _, Text("Edit"))

  /** Render reply button for discussion comment. */
  def replyDiscussionButton(d: Discussion) = SHtml.a(replyEditor(d) _, Text("Reply"))

  def replyEditor(parent: Discussion)() =
    Editor.editorObj("", preview _, text => { saveDiscussionComment(Category.get(parent), text, Some(parent)) }) match {
      case (n, j) =>
        Replace("reply_for_" + parent.id,
          <form id={"reply_form_for_" + parent.id} class="edit" method="GET">
            <div class="editor">
              { n }
              <div class="buttons">
              { SHtml.ajaxButton(Text("Save"), () => SHtml.submitAjaxForm("reply_form_for_" + parent.id, () => reloadDiscussion(Category.get(parent)))) }
              { SHtml.a(Text("Cancel"),
                  Replace("reply_form_for_" + parent.id, <div id={"reply_for_" + parent.id} />) &
                  PrettyDate &
                  Jq(Str("button")) ~> Button(),
                  ("class", "button"))
              }
            </div>
            </div>
          </form>) & j & Jq(Str("button")) ~> Button()
      case _ => JsCmds.Noop
    }

  /** Update discussion comment record in database. */
  private def updateDiscussionComment(d: Discussion)(text: String) {
    d.comment(text).save
  }
  
  override def memberToHtml(mbr: MemberEntity): NodeSeq =
    super.memberToHtml(mbr) \% Map("data-istype" -> (mbr.isAbstractType || mbr.isAliasType).toString)

  override def memberToShortCommentHtml(mbr: MemberEntity, isSelf: Boolean): NodeSeq =
    super.memberToShortCommentHtml(mbr, isSelf) \% Map("id" -> id(mbr, "short"))

  override def memberToInlineCommentHtml(mbr: MemberEntity, isSelf: Boolean) =
    <xml:group>
      { memberToShortCommentHtml(mbr, isSelf) }
      <div class="fullcomment">{ memberToUseCaseCommentHtml(mbr, isSelf) }{ memberToCommentBodyHtml(mbr, isSelf) }</div>
    </xml:group>

  override def memberToCommentBodyHtml(mbr: MemberEntity, isSelf: Boolean, isReduced: Boolean = false) =
    <div id={ id(mbr, "full") }>
      { content(mbr, isSelf, isReduced) }
      { controls(mbr, isSelf) }
    </div>

  /** Render member entity content with controls. */
  private def content(mbr: MemberEntity, isSelf: Boolean, isReduced: Boolean) =
    <div id={ id(mbr, "content") }>
      { super.memberToCommentBodyHtml(mbr, isSelf, isReduced) }
    </div>

  /** Render member entity control. */
  private def controls(mbr: MemberEntity, isSelf: Boolean) =
    <div class="controls">
      { select(mbr, isSelf) }
      { if (User.validSuperUser_?)
          edit(mbr, isSelf)
      }
      { if (User.validSuperUser_?) delete(mbr, isSelf) }
      { if (User.validSuperUser_?) selectDefault(mbr, isSelf) }
      { if (User.validSuperUser_?) propagateToPredecessors(mbr, isSelf) }
      { export(mbr, isSelf) }
    </div>

  /** Propagate comment from member through the hierarchy of predecessors. */
  private def propagateToPredecessors(mbr: MemberEntity, isSelf: Boolean) = currentComment(mbr) match {
      case Some(comment) =>
        def move(name: String) = {
          if (name != ".push") { // TODO: remove '.push' tag
            val newQualifiedName = mbr.qualifiedName.replace(mbr.inTemplate.qualifiedName, name)
            val usr = User.currentUser.open_!

            comment.qualifiedName(newQualifiedName).user(usr).dateTime(now).changeSet(now).active(false).save

            val (cmt, c) = defaultCommentFromDb(mbr) match {
              case Some(cmt) =>
                Comment.deactivateAll(mbr.uniqueName)
                cmt.active(true).save
                mbr.comment.get.update("" + cmt.comment.is)
                (Model.factory.parse(mbr, cmt.comment.is), cmt)
              case None =>
                (mbr.comment.get.original.get, "source")
              // TODO: mbr.comment.update(original)
            }
            index.vend.reindexEntityComment(mbr)
            val m = Model.factory.copyMember(mbr, cmt)(c)
            Replace(id(mbr, "full"), memberToCommentBodyHtml(m, isSelf)) & Run("reinit('#" + id(m, "full") + "')") &
            (if (!isSelf) JqId(Str(id(mbr, "short"))) ~> JqHtml(inlineToHtml(cmt.short)) ~> JqAttr("id", id(m, "short")) else Noop)
          } else
            Noop
        }
        val defs = (".push", "Push to predecessor") ::
                mbr.inDefinitionTemplates.filter(x => x != mbr.inTemplate).map(x => (x.qualifiedName, x.qualifiedName))
        if (defs.length > 1)
          SHtml.ajaxSelect(defs, Empty, ColladocConfirm("Confirm propagate"), move _, ("class", "select"))
      case _ =>
    }

  /**
   * Current comment for member.
   * If tag is empty try to load data from database.
   */
  private def currentComment(mbr: MemberEntity): Option[Comment] = mbr.tag match {
    case comment: Comment => Some(comment)
    case _ => Comment.default(mbr.uniqueName) match {
      case None => Comment.latest(mbr.uniqueName)
      case c => c
    }
  }

  /** Default value for select with changesets. */
  private def defaultComment(mbr: MemberEntity) = mbr.tag match {
    case cmt: Comment => Full(cmt.id.is.toString)
    case id: String => Full(id)
    case _ => Comment.default(mbr.uniqueName) match {
      case Some(c) => Full(c.id.is.toString)
      case None => Empty
    }
  }

  /** Render revision selection for member entity. */
  private def select(mbr: MemberEntity, isSelf: Boolean) = {
    def replace(cid: String) = {
      val (cmt, c) = Comment.find(cid) match {
        case Full(c) => (Model.factory.parse(mbr, c.comment.is), c)
        case _ => (mbr.comment.get.original.get, "source")
      }
      val m = Model.factory.copyMember(mbr, cmt)(c)
      
      Replace(id(mbr, "full"), memberToCommentBodyHtml(m, isSelf)) & Run("reinit('#" + id(m, "full") + "')") &
      (if (!isSelf) JqId(Str(id(mbr, "short"))) ~> JqHtml(inlineToHtml(cmt.short)) ~> JqAttr("id", id(m, "short")) else JsCmds.Noop)
    }
    val revs = Comment.revisions(mbr.uniqueName) ::: ("source", "Source Comment") :: Nil
    SHtml.ajaxSelect(revs, defaultComment(mbr), replace _, ("class", "select"))
  }

  /** Render revision selection for member entity. */
  private def selectDefault(mbr: MemberEntity, isSelf: Boolean) = {
    def replace(cid: String) = {
      Comment.find(cid) match {
        case Full(c) => activate(mbr, c)
        case _ =>
      }

      Replace(id(mbr, "full"), memberToCommentBodyHtml(mbr, isSelf)) & Run("reinit('#" + id(mbr, "full") + "')") &
      (if (!isSelf) SetHtml(id(mbr, "short"), inlineToHtml(mbr.comment.get.short)) else JsCmds.Noop)
    }
    val revs = ("source", "Select default") :: Comment.revisions(mbr.uniqueName)
    SHtml.ajaxSelect(revs, Empty, ColladocConfirm("Set as default value?"), replace _, ("class", "select"))
  }

  /** Render edit button for member entity. */
  private def edit(mbr: MemberEntity, isSelf: Boolean) = {
    SHtml.a(doEdit(mbr, isSelf) _, Text("Edit"), ("class", "button"))
  }

  /** Provide edit button logic replacing part of the page with comment editor. */
  private def doEdit(mbr: MemberEntity, isSelf: Boolean)(): JsCmd = {
    def getSource(mbr: MemberEntity) = mbr.comment match {
        case Some(c) => c.source.getOrElse("")
        case None => ""
      }
    Editor.editorObj(getSource(mbr), parse(mbr, isSelf) _, text => update(mbr, text)) match {
      case (n, j) =>
        Replace(id(mbr, "full"),
          <form id={ id(mbr, "form") } class="edit" method="GET">
            <div class="editor">
              { n }
              <div class="buttons">
                { SHtml.ajaxButton(Text("Save"), () => SHtml.submitAjaxForm(id(mbr, "form"), () => save(mbr, isSelf))) }
                { SHtml.a(Text("Cancel"), cancel(mbr, isSelf)) }
              </div>
            </div>
          </form>) & j & Jq(Str("button")) ~> Button()
      case _ => JsCmds.Noop
    }
  }

  /** Render delete button for member entity. */
  private def delete(mbr: MemberEntity, isSelf: Boolean) = {
    def doDelete(mbr: MemberEntity, isSelf: Boolean)(): JsCmd = {
      def replace(mbr: MemberEntity, isSelf: Boolean) = {
        val (cmt, c) = defaultCommentFromDb(mbr) match {
          case Some(cmt) =>
            Comment.deactivateAll(mbr.uniqueName)
            cmt.active(true).save
            mbr.comment.get.update("" + cmt.comment.is)
            (Model.factory.parse(mbr, cmt.comment.is), cmt)
          case None =>
            (mbr.comment.get.original.get, "source")
            // TODO: mbr.comment.update(original)
        }
        val m = Model.factory.copyMember(mbr, cmt)(c)
        Replace(id(mbr, "full"), memberToCommentBodyHtml(m, isSelf)) & Run("reinit('#" + id(m, "full") + "')") &
        (if (!isSelf) JqId(Str(id(mbr, "short"))) ~> JqHtml(inlineToHtml(cmt.short)) ~> JqAttr("id", id(m, "short")) else JsCmds.Noop)
      }

      currentComment(mbr) match {
        case Some(cmt) => cmt.valid(false).active(false).save; replace(mbr, isSelf)
        case None      => Noop
      }
    }

    if (Comment.revisions(mbr.uniqueName).length > 0)
      SHtml.a(ColladocConfirm("Confirm delete"), doDelete(mbr, isSelf) _, Text("Delete"), ("class", "button"))
  }

  /** Get default comment from database. */
  private def defaultCommentFromDb(mbr: MemberEntity) = Comment.default(mbr.uniqueName) match {
    case None => Comment.latest(mbr.uniqueName)
    case c => c
  }

  /** Parse documentation string input to show comment preview. */
  private def parse(mbr: MemberEntity, isSelf: Boolean)(docStr: String) = {
    val cmt = Model.factory.parse(mbr, docStr)
    <html>
      <head>
        <link href="/lib/template.css" media="screen" type="text/css" rel="stylesheet" />
        <link href="/copreview.css" media="screen" type="text/css" rel="stylesheet" />
      </head>
      <body>
        <div id="comment" class="fullcomment">
          { super.memberToCommentBodyHtml(Model.factory.copyMember(mbr, cmt)(), isSelf) }
        </div>
      </body>
    </html>
  }

  /** Save modified member entity comment. */
  private def save(mbr: MemberEntity, isSelf: Boolean): JsCmd =
    if (Model.reporter.hasWarnings)
      JqId(Str(id(mbr, "text"))) ~> AddClass("ui-state-error")
    else
      Replace(id(mbr, "form"), memberToCommentBodyHtml(mbr, isSelf)) & Run("reinit('#" + id(mbr, "full") + "')") &
      (if (!isSelf) SetHtml(id(mbr, "short"), inlineToHtml(mbr.comment.get.short)) else JsCmds.Noop)

  /** Cancel member entity comment modifications. */
  private def cancel(mbr: MemberEntity, isSelf: Boolean): JsCmd =
    Replace(id(mbr, "form"), memberToCommentBodyHtml(mbr, isSelf)) & Run("reinit('#" + id(mbr, "full") + "')")

  /** Update member entity after comment has been changed. */
  private def update(mbr: MemberEntity, docStr: String) = Model.synchronized {
    Model.reporter.reset
    def doSave() = {
      val usr = User.currentUser.open_!
      Comment.deactivateAll(mbr.uniqueName)
      val cmt = Comment.create.qualifiedName(mbr.uniqueName).comment(docStr).dateTime(now).user(usr).active(true)
      Comment.findAll(By(Comment.qualifiedName, mbr.uniqueName), By(Comment.user, usr), By(Comment.valid, true),
          OrderBy(Comment.dateTime, Descending), MaxRows(1)) match {
        case List(c: Comment, _*) if c.dateTime.is - cmt.dateTime.is < minutes(30) =>
          cmt.changeSet(c.changeSet.is)
        case _ =>
          cmt.changeSet(now)
      }
      cmt.save
      index.vend.reindexEntityComment(mbr)
    }
    mbr.comment.get.update(docStr)
    if (!Model.reporter.hasWarnings) doSave
  }

  /** Activate comment for member entity. */
  private def activate(mbr: MemberEntity, cmt: Comment) = Model.synchronized {
    Model.reporter.reset
    def doSave() = {
      Comment.deactivateAll(mbr.uniqueName)
      cmt.active(true).save
      index.vend.reindexEntityComment(mbr)
    }
    mbr.comment.get.update("" + cmt.comment.is)
    if (!Model.reporter.hasWarnings) doSave
  }

  /** Render export link for member entity. */
  private def export(mbr: MemberEntity, isSelf: Boolean) = mbr match {
    case tpl: DocTemplateEntity =>
      <xml:group>
        <a class="control menu">Export</a>
        <ul style="display: none;">
          <li>{ SHtml.a(doExport(mbr, isSelf, false) _, Text("Symbol Only")) }</li>
          <li>{ SHtml.a(doExport(mbr, isSelf, true) _, Text("All Subsymbols")) }</li>
        </ul>
      </xml:group>
    case _ =>
      SHtml.a(doExport(mbr, isSelf, false) _, Text("Export"), ("class", "control"))
  }

  /** Provide export link logic. */
  private def doExport(mbr: MemberEntity, isSelf: Boolean, rec: Boolean)(): JsCmd = {
    var pars = if (rec) "rec=true" :: Nil else Nil
    mbr.tag match {
      case cmt: Comment => pars ::= "rev=%s" format(cmt.dateTime.is.getTime)
      case _ =>
    }
    val abs = "/" + mbr.uniqueName.replace(".", "/").replace("#", "$")
    val path = abs + ".xml" + pars.mkString("?", "&", "")
    JsRaw("window.open('%s', 'Export')" format (path))
  }
}
}
}