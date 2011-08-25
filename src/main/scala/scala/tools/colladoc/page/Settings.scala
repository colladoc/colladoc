/*
 * Copyright (c) 2011, Sergey Ignatov. All rights reserved.
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
package scala.tools.colladoc.page

import tools.nsc.doc.model._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.SHtml.ElemAttr._
import net.liftweb.widgets.gravatar.Gravatar
import net.liftweb.http.js.JsCmd
import net.liftweb.http.{S, SHtml}
import net.liftweb.util.BindHelpers._
import tools.colladoc.model.mapper.{Category, Properties, User}
import net.liftweb.common.Full
import tools.colladoc.lib.js.JqUI._
import xml.{Text, NodeSeq}
import net.liftweb.http.js.JE.Str
import net.liftweb.mapper.{By, OrderBy, Ascending}
import net.liftweb.http.js.jquery.JqJE.Jq

/**
 * Settings page.
 * @author Sergey Ignatov
 */
class Settings(rootPack: Package) extends scala.tools.colladoc.page.Template(rootPack) {
  /**Page title. */
  override val title = "Settings"

  /** Page body. */
  override val body =
    <body class="settings">
      { if (User.validSuperUser_?) {
          <div id="definition">
            <img src="images/settings_big.png" />
            <h1>Settings</h1>
          </div>
          <div id="template">
            { adminForm }
          </div>
      }}
    </body>

  /** Admin user form. */
  private def adminForm =
    <div id="settings_tab">
      <ul>
        <li><a href="#user_settings">Users</a></li>
        <li><a href="#project_settings">Project settings</a></li>
        <li><a href="#discussions_settings">Discussions</a></li>
      </ul>
      <div id="user_settings">
        <input id="user_filter" class="text ui-widget-content ui-corner-all"/>
        { userList }
      </div>
      <div id="project_settings">
        { projectSettings }
      </div>
      <div id="discussions_settings">
        { categoriesSettings }
      </div>
    </div>

  /** List with links to user's profiles. */
  private def userList =
    <div id="user_list">
      <ul>
        {
          User.findAll(OrderBy(User.userName, Ascending)) map { u =>
            <li class={"ui-selectee ui-corner-all" + (if (u.deleted_?) " deleted" else if (u.banned.is) " banned" else "")}>
              <div class="profile_link">
                {
                  Gravatar(u.email, 16) ++
                  u.profileHyperlinkLocal ++
                  (if (u.superUser.is) SHtml.span(NodeSeq.Empty, Noop, ("class", "ui-icon ui-icon-lightbulb")) else NodeSeq.Empty)
                }
              </div>
            </li>
          }
        }
      </ul>
    </div>

  /** Form with project properties. */
  private def projectSettings: NodeSeq = {
    var title = Properties.get("-doc-title").getOrElse("")
    var version = Properties.get("-doc-version").getOrElse("")

    def doSave(): JsCmd = {
      Properties.set("-doc-title", title)
      Properties.set("-doc-version", version)
      S.notice("Project settings successfully saved.")
      Noop
    }

    val form =
      <lift:form class="properties">
        <fieldset>
          <p>
            <label for="title">Title:</label>
            <settings:title class="text required ui-widget-content ui-corner-all" />
          </p>
          <p>
            <label for="version">Version:</label>
            <settings:version class="text required ui-widget-content ui-corner-all" />
          </p>
          <settings:submit />
          <settings:save />
          <settings:reset />
        </fieldset>
      </lift:form>

    bind("settings", form,
      "title" -%> SHtml.text(title, title = _, ("id", "title")),
      "version" -%> SHtml.text(version, version = _, ("id", "version")),
      "submit" -> SHtml.hidden(doSave _),
      "save" -> SHtml.a(Text("Save"), SubmitForm(".properties"), ("class", "button")),
      "reset" -> SHtml.a(Text("Reset"), SetValById("title", Str(title)) & SetValById("version", Str(version)), ("class", "button"))
    )
  }

  /** Categories properties. */
  private def categoriesSettings: NodeSeq = {
    var name = ""

    def doSave(): JsCmd = {
      Category.find(By(Category.name, name)) match {
        case Full(c) =>
          S.notice("Category " + name + " already exists.")
          Noop
        case _ =>
          Category.create.name(name).save()
          S.notice("Category " + name + " successfully created.")
          Replace("categories_form", categoriesSettings) & Jq(Str(".button")) ~> Button()
      }
    }

    def categoryToHtml(c: Category) =
      <tr>
        <td>
          {
            SHtml.ajaxText(
              c.name.is,
              text => {
                if (c.name.is != text )
                  c.name(text).save()
                Noop
              }
            )
          }
        </td>
        <td>
          {
            SHtml.ajaxCheckbox(
              c.anonymousView,
              bool => { c.anonymousView(bool).save(); Noop }
            )
          }
        </td>
        <td>
          {
            SHtml.ajaxCheckbox(
              c.anonymousPost,
              bool => { c.anonymousPost(bool).save(); Noop }
            )
          }
        </td>
        <td>
          {
            SHtml.a(
              JqUIConfirm("Confirm delete"),
              () => { c.valid(false).save(); Replace("categories_form", categoriesSettings) & Jq(Str(".button")) ~> Button() },
              SHtml.span(NodeSeq.Empty, Noop, ("class", "ui-icon ui-icon-trash")),
              ("class", "button")
            )
          }
        </td>
      </tr>

    val form =
      <div id="categories_form">
        <lift:form class="category">
          <h3>Categories</h3>
          <table id="categories_table">
            <tr>
              <th>Name</th>
              <th>Anonymous viewable</th>
              <th>Anonymous postable</th>
              <th></th>
            </tr>
            { Category.all map categoryToHtml _ }
            <tr>
              <td colspan="3">
                <category:name class="text required ui-widget-content ui-corner-all" />
              </td>
              <td>
                <category:save />
              </td>
            </tr>
          </table>
          <category:submit />
        </lift:form>
      </div>

    bind("category", form,
      "name" -%> SHtml.text(name, name = _),
      "submit" -> SHtml.hidden(doSave _),
      "save" -> SHtml.a(SHtml.span(NodeSeq.Empty, Noop, ("class", "ui-icon ui-icon-plus")), SubmitFormWithValidation(".category"), ("class", "button"))
    )
  }
}
