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

import model.mapper.{Comment, User}
import lib.DependencyFactory._
import page.Profile
import xml.{NodeSeq, Text}
import net.liftweb.util.BindHelpers._
import net.liftweb.util.DefaultDateTimeConverter._
import net.liftweb.http.{SHtml, S, RequestVar}
import net.liftweb.mapper.By
import net.liftweb.widgets.gravatar.Gravatar
import lib.js.JqUI.SubmitFormWithValidation
import net.liftweb.http.js.JsCmds.SetValById
import net.liftweb.http.js.JE.Str
import net.liftweb.http.js.{JsCmds, JsCmd}

/**
 * User profile snippet.
 * @author Sergey Ignatov
 */
class ProfileOps {
  object username extends RequestVar[String](S.param("username") openOr (User.currentUser.map(_.userName.is) openOr ""))

  lazy val profile = new Profile(model.vend.rootPackage)

  def title(xhtml: NodeSeq): NodeSeq = Text(getUser.userName.is)

  def getUser = User.find(By(User.userName, username)).open_!

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
      <lift:form class="profile_form">
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
            <label for="version">E-mail</label>
            <user:email class="text required ui-widget-content ui-corner-all" />
          </p>
          <p>
            <label for="password">Password</label>
            <user:password class="text required ui-widget-content ui-corner-all" />
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
      "password" -%> SHtml.password("", user.password(_), ("id", "password")),
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

  def body(xhtml: NodeSeq): NodeSeq = {
    val user = getUser

    val fullname = user.userName.is

    val cmts: List[Comment] = Comment.findAll(By(Comment.user, user), By(Comment.valid, true))
    val comments =
        if (cmts.length == 0)
          <h3>No comments by user.</h3>
        else
          <xml:group>
            <h3>Comments by user</h3>
            <ul>
              { cmts.sortBy(c => c.qualifiedName.is).map(c =>
                  {
                    val abs = "/" + c.qualifiedName.is.replace(".", "/").replace("#", "$") + ".html"
                    <li>
                      <a href={abs}>{c.qualifiedName.is}</a>:
                      <span class="comment">{c.comment.is}</span>
                      <span class="datetime" title={Comment.atomDateFormatter(c.dateTime)}>{formatDate(c.dateTime)}</span>
                    </li>
                  })
              }
            </ul>
          </xml:group>

    bind("profile", profile.body,
      "form"     -> userForm(user),
      "fullname" -> Text(fullname),
      "comments" -> comments
    )
  }
}