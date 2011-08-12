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
import lib.js.JqUI.{ColladocConfirm, SubmitFormWithValidation, SubmitForm}
import net.liftweb.http.js.JE.Str
import page.{Template, History, Profile}
import net.liftweb.common.Full
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.http.js.JsCmds._
import tools.nsc.doc.model.{DocTemplateEntity, MemberEntity}
import lib.util.PathUtils._

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
    if (User.validSuperUser_?)
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
          <p>
            <label for="site">Website/Blog</label>
            <user:site class="text ui-widget-content ui-corner-all" />
          </p>
          <p>
            <label for="company">Company</label>
            <user:company class="text ui-widget-content ui-corner-all" />
          </p>
          <p>
            <label for="location">Location</label>
            <user:location class="text ui-widget-content ui-corner-all" />
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
      "site" -%> SHtml.text(user.site.is, user.site(_), ("id", "site")),
      "company" -%> SHtml.text(user.company.is, user.company(_), ("id", "company")),
      "location" -%> SHtml.text(user.location.is, user.location(_), ("id", "location")),
      "submit" -> SHtml.hidden(doSave _),
      "save" -> SHtml.a(Text("Save"), SubmitFormWithValidation(".profile_form"), ("class", "button")),
      "reset" -> SHtml.a(Text("Reset"),
        SetValById("username", Str(user.userName.is)) &
        SetValById("fullname", Str(user.shortName)) &
        SetValById("email", Str(user.email.is)) &
        SetValById("password", Str("")) &
        SetValById("openid", Str(user.openId.is)) &
        SetValById("site", Str(user.site.is)) &
        SetValById("location", Str(user.location.is)) &
        SetValById("company", Str(user.company.is)),
        ("class", "button"))
    )
  }

  def fixLink(href: String) = {
    if (href.startsWith("http://") || href.startsWith("https://"))
      href
    else
      "http://" + href
  }

  def publicProfile(user: User) =
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
        {
          if (user.site.is.trim.length > 0)
            <p>
              <label>Website/blog</label>
              <a href={fixLink(user.site.is)}>{fixLink(user.site.is)}</a>
            </p>
        }
        {
          if (user.company.is.trim.length > 0)
            <p>
              <label>Company</label>
              <span>{user.company.is}</span>
            </p>
        }
        {
          if (user.location.is.trim.length > 0)
            <p>
              <label>Location</label>
              <span>{user.location.is}</span>
            </p>
        }
      </fieldset>
    </lift:form>

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
        </fieldset>
      </lift:form>

    bind("change", form,
      "old"     -%> SHtml.password("", oldPass = _, ("id", "old")),
      "new"     -%> SHtml.password("", newPass = _, ("id", "new")),
      "confirm" -%> SHtml.password("", confirm = _, ("id", "confirm")),
      "submit"   -> SHtml.hidden(doSave _),
      "save"     -> SHtml.a(Text("Save"), SubmitFormWithValidation(".change_password_form"), ("class", "button"))
    )
  }

  def changePasswordFormForSuperuser(user: User) = {
    var newPass = ""

    def doSave(): JsCmd = {
      user.password(newPass)
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
      <lift:form class="form change_password_form_for_superuser">
        <fieldset>
          <p>
            <label for="new">Enter new password:</label>
            <change:new class="text required ui-widget-content ui-corner-all" />
          </p>
          <change:submit />
          <change:save />
        </fieldset>
      </lift:form>

    bind("change", form,
      "new"     -%> SHtml.password("", newPass = _),
      "submit"   -> SHtml.hidden(doSave _),
      "save"     -> SHtml.a(Text("Save"), SubmitForm(".change_password_form_for_superuser"), ("class", "button"))
    )
  }

  def deleteProfile(user: User) = {
    if (!user.deleted_?)
      <div id="delete_account">
        {
          SHtml.a(
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
            Text("Delete account"))
        }
      </div>
    else if (User.validSuperUser_? && user.deleted_?)
      <div id="recover_account">
        {
          SHtml.a(
            ColladocConfirm("Confirm recover"),
            () => {
              user.deleted(false).save
              S.notice("Account " + user.userName + " successfully recovered")
              Noop
            },
            Text("Recover account"))
        }
      </div>
    else
      NodeSeq.Empty
  }

  def superuser(user: User) = {
    <div id="superuser_wrapper">
      <label for="superuser">Superuser</label>
      {
        SHtml.ajaxCheckbox(
          user.superUser, bool => {
            user.superUser(bool).save
            Noop
          }, ("id", "superuser"))
      }
    </div>
  }

  def available(user: User) = {
    <div id="available_wrapper">
      <label for="available">Active</label>
      {
        SHtml.ajaxCheckbox(
          !user.banned.is, bool => {
            user.banned(!bool).save
            Noop
          }, ("id", "available"))
      }
    </div>
  }

  private def tmpl(member: MemberEntity) = member match {
    case tpl: DocTemplateEntity => tpl
    case _ => member.inTemplate
  }

  def path(qualifiedName: String) = qualifiedName.split('.').toList

  def fixedPath(qualifiedName: String) = {
    val p = path(qualifiedName)
    if (p.length == 1)
      p ::: List("package")
    else
      p
  }

  def body(xhtml: NodeSeq): NodeSeq = {
    val user = getUser

    val fullname = user.userName.is

    val cmts: List[Comment] = Comment.findAll(By(Comment.user, user), By(Comment.valid, true))
    val comments = new History(model.vend.rootPackage).commentsToHtml(cmts)

    val template: Template = new Template(model.vend.rootPackage)
    val dscs = Discussion.findAll(By(Discussion.user, user), By(Discussion.valid, true), OrderBy(Discussion.dateTime, Ascending))

    val entities = dscs.map(d => (d.qualifiedName.is, d)).groupBy(p => p._1)

    lazy val x = entities map { case (qualifiedName, value) => {
      val m = pathToTemplate(model.vend.rootPackage, fixedPath(qualifiedName))

      val containingType = tmpl(m);

      <div class={if (containingType.isTrait || containingType.isClass) " type" else " value"}>
        <h4 class="definition">
          <a href={ abs(qualifiedName) }>
            <img src={ profile.relativeLinkTo{List(profile.kindToString(containingType) + ".png", "lib")} }/>
          </a>
          <span>{ qualifiedName }</span>
        </h4>
        <div class="discussion_wrapper">
          <ul>
            {value map { case (q, d) => dToHtml(d) } }
          </ul>
        </div>
      </div>
      }
    }

    def abs(qualifiedName: String) = "/" + fixedPath(qualifiedName).mkString("/") + ".html"

    def dToHtml(d: Discussion) = {
      bind("discussion_comment", template.discussionToHtml(d))
    }

    val discussionComments = <xml:group>{x map (y => y)}</xml:group>

    bind("profile",
      profile.body,
      "form"                -> { if (!public_?) userForm(user) else publicProfile(user) },
      "change_password"     -> {
        if (User.validSuperUser_?)
          changePasswordFormForSuperuser(user)
        else {
          if (!public_?)
            changePasswordForm(user)
          else
            NodeSeq.Empty
        } },
      "delete_profile"      -> { if (!public_?) deleteProfile(user) else NodeSeq.Empty },
      "superuser"           -> { if (User.validSuperUser_?) superuser(user) else NodeSeq.Empty },
      "available"           -> { if (User.validSuperUser_?) available(user) else NodeSeq.Empty },
      "discussion_comments" -> { if (User.loggedIn_?) discussionComments else NodeSeq.Empty },
      "fullname"            -> Text(fullname),
      "comments"            -> comments
    )
  }
}