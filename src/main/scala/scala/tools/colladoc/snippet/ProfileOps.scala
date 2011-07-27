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
package scala.tools.colladoc
package snippet

import model.mapper.{Discussion,Comment, User}
import lib.DependencyFactory._
import xml.{NodeSeq, Text}
import net.liftweb.util.BindHelpers._
import net.liftweb.http.{SHtml, S, RequestVar}
import net.liftweb.mapper.{Ascending, By, OrderBy}
import lib.js.JqUI.{ColladocConfirm, SubmitFormWithValidation}
import net.liftweb.http.js.JsCmds.{Noop, RedirectTo, SetValById}
import net.liftweb.http.js.JE.Str
import net.liftweb.http.js.{JsCmds, JsCmd}
import page.{Template, History, Profile}
import net.liftweb.common.Full

/**
 * User profile snippet.
 * @author Sergey Ignatov
 */
class ProfileOps {
  object username extends RequestVar[String](S.param("username") openOr (User.currentUser.map(_.userName.is) openOr ""))

  lazy val profile = new Profile(model.vend.rootPackage)

  def title(xhtml: NodeSeq): NodeSeq = Text(getUser.userName.is)

  def getUser = {
    val maybeUser = User.find(By(User.userName, username))
    if (maybeUser.isEmpty)
      S.redirectTo("/")
    maybeUser.open_!
  }

  def public_?(): Boolean = {
    if (!User.loggedIn_?)
      return true
    if (User.superUser_?)
      return false
    User.currentUser match {
      case Full(u) =>
        if (u == getUser)
          return false
        true
      case _ => true
    }
  }

  private def userForm(user: User): NodeSeq = {
    def doSave(): JsCmd = {
      user.validate match {
        case Nil =>
          S.notice("User successfully saved")
          user.save()
        case n =>
          S.error(n)
      }
      JsCmds.Noop
    }

    val form =
      <lift:form class="form profile_form">
        <fieldset>
          <p>
            <label for="username">Username</label>
            <user:username class="text required ui-widget-content ui-corner-all" />
          </p>
          <p>
            <label for="fullname">Full Name</label>
            <user:fullname class="text required ui-widget-content ui-corner-all" />
          </p>
          <p>
            <label for="email">E-mail</label>
            <user:email class="text required ui-widget-content ui-corner-all" />
          </p>
          <p>
            <label for="openid">OpenID</label>
            <user:openid class="text ui-widget-content ui-corner-all" />
          </p>
          <user:submit />
          <user:save />
          <user:reset />
        </fieldset>
      </lift:form>

    bind("user", form,
      "username" -%> SHtml.text(user.userName.is, user.userName(_), ("id", "username")),
      "fullname" -%> SHtml.text(user.shortName, name => {
          val idx = name.indexOf(" ")
          if (idx != -1) {
            user.firstName(name.take(idx))
            user.lastName(name.drop(idx + 1))
          } else
            user.firstName(name)
        }, ("id", "fullname")),
      "email" -%> SHtml.text(user.email.is, user.email(_), ("id", "email")),
      "openid" -%> SHtml.text(user.openId.is, user.openId(_), ("id", "openid")),
      "submit" -> SHtml.hidden(doSave _),
      "save" -> SHtml.a(Text("Save"), SubmitFormWithValidation(".profile_form"), ("class", "button")),
      "reset" -> SHtml.a(Text("Reset"),
        SetValById("username", Str(user.userName.is)) &
        SetValById("fullname", Str(user.shortName)) &
        SetValById("email", Str(user.email.is)) &
        SetValById("password", Str("")) &
        SetValById("openid", Str(user.openId.is)),
        ("class", "button"))
    )
  }

  def publicProfile(user: User) = {
    <lift:form class="form profile_form">
      <fieldset>
        <p>
          <label>Username</label>
          <span>{user.userName}</span>
        </p>
        <p>
          <label>Full Name</label>
          <span>{user.shortName}</span>
        </p>
      </fieldset>
    </lift:form>
  }

  def changePasswordForm(user: User) = {
    var oldPass, newPass, confirm: String = ""

    def doSave(): JsCmd = {
      if (user.password.match_?(oldPass)) {
        if (newPass == confirm) {
          user.password(newPass)
          user.validate match {
            case Nil =>
              S.notice("User successfully saved")
              user.save()
            case n =>
              S.error(n)
          }
        } else {
          S.error("Password doesn't match the confirmation")
        }
      } else {
        S.error("Old password isn't valid")
      }
      JsCmds.Noop
    }

    val form =
      <lift:form class="form change_password_form">
        <fieldset>
          <p>
            <label for="old">Enter your old password:</label>
            <change:old class="text required ui-widget-content ui-corner-all" />
          </p>
          <p>
            <label for="new">Enter your new password:</label>
            <change:new class="text required ui-widget-content ui-corner-all" />
          </p>
          <p>
            <label for="confirm">Confirm it:</label>
            <change:confirm class="text required ui-widget-content ui-corner-all" />
          </p>
          <change:submit />
          <change:save />
          <change:reset />
        </fieldset>
      </lift:form>

    bind("change", form,
      "old"     -%> SHtml.password("", oldPass = _, ("id", "old")),
      "new"     -%> SHtml.password("", newPass = _, ("id", "new")),
      "confirm" -%> SHtml.password("", confirm = _, ("id", "confirm")),
      "submit"   -> SHtml.hidden(doSave _),
      "save"     -> SHtml.a(Text("Save"), SubmitFormWithValidation(".change_password_form"), ("class", "button")),
      "reset"    -> SHtml.a(Text("Reset"),
        SetValById("old", Str(oldPass)) &
        SetValById("new", Str(newPass)) &
        SetValById("confirm", Str(confirm)),
        ("class", "button"))
    )
  }

  def deleteProfile(user: User) = {
    <div id="delete_account">
    <h2>Delete account</h2>
      {
        SHtml.ajaxButton(
          "Delete",
          ColladocConfirm("Confirm delete"),
          () => {
            user.deleted(true).save
            S.notice("Account " + user.userName + " successfully deleted")
            if (User.currentUser.open_! == user) {
              User.logout
              RedirectTo("/")
            } else {
              Noop
            }
          },
          ("class", "button"))
      }
      </div>
  }

  def body(xhtml: NodeSeq): NodeSeq = {
    val user = getUser

    val fullname = user.userName.is

    val cmts: List[Comment] = Comment.findAll(By(Comment.user, user), By(Comment.valid, true))
    val comments = new History(model.vend.rootPackage).commentsToHtml(cmts)

    val template: Template = new Template(model.vend.rootPackage)
    val dscs = Discussion.findAll(By(Discussion.user, user), By(Discussion.valid, true), OrderBy(Discussion.dateTime, Ascending))

    val entities = dscs.map(d => (d.qualifiedName.is, d)).groupBy(p => p._1)

    lazy val x = entities map { case (qualifiedName, value) =>
      <xml:group>
        <h3><a href={abs(qualifiedName)}>{qualifiedName}</a></h3>
        <ul>{value map { case (q, d) => dToHtml(d) } }</ul>
      </xml:group>
    }

    def abs(qualifiedName: String) = "/" + qualifiedName.replace(".", "/").replace("#", "$") + ".html"

    def dToHtml(d: Discussion) = {
      bind("discussion_comment", template.discussionToHtml(d))
    }

    val discussionComments = <xml:group>{x map (y => y)}</xml:group>

    bind("profile",
      profile.body,
      "form"                -> { if (!public_?) userForm(user) else publicProfile(user) },
      "change_password"     -> { if (!public_?) changePasswordForm(user) else NodeSeq.Empty },
      "delete_profile"      -> { if (!public_?) deleteProfile(user) else NodeSeq.Empty },
      "discussion_comments" -> { if (!public_?) discussionComments else NodeSeq.Empty },
      "fullname"            -> Text(fullname),
      "comments"            -> comments
    )
  }
}