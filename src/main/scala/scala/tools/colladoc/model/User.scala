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
package model {

import lib.JsCmds._

import net.liftweb.mapper._
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import xml.Text

class User extends ProtoUser[User] with OneToMany[Long, User]  {
  def getSingleton = User

  object userName extends MappedString(this, 32) {
    override def dbIndexed_? = true
    override def validations = valUnique(S.??("unique.user.name")) _ :: super.validations
    override def displayName = fieldOwner.userNameDisplayName
    override val fieldId = Some(Text("txtFirstName"))
  }

  def userNameDisplayName = S.??("user.name")

  object comments extends MappedOneToMany(Comment, Comment.user)
}

object User extends User with KeyedMetaMapper[Long, User] {
  override def dbTableName = "users"

  private object curUserId extends SessionVar[Box[String]](Empty)

  def currentUserId: Box[String] = curUserId.is

  private object curUser extends RequestVar[Box[User]](currentUserId.flatMap(id => find(id))) with CleanRequestVarOnSessionTransition

  def currentUser: Box[User] = curUser.is

  def superUser_? : Boolean = currentUser.map(_.superUser.is) openOr false

  def loggedIn_? = currentUserId.isDefined
  def logUserIdIn(id: String) {
    curUser.remove()
    curUserId(Full(id))
  }
  def logUserIn(who: User) {
    curUser.remove()
    curUserId(Full(who.id.toString))
  }

  def logoutCurrentUser = logUserOut()
  def logUserOut() {
    curUserId.remove()
    curUser.remove()
    S.request.foreach(_.request.session.terminate)
  }

  def userHtml =
    <lift:form class="user form">
      <fieldset>
        <p>
          <label for="name">Username:</label>
          <user:username class="text required ui-widget-content ui-corner-all" />
        </p>
        <p>
          <label for="name">Full Name:</label>
          <user:fullname class="text ui-widget-content ui-corner-all" />
        </p>
        <p>
          <label for="name">Email:</label>
          <user:email class="text required email ui-widget-content ui-corner-all" />
        </p>
        <p>
          <label for="password">Password:</label>
          <user:password class="text required ui-widget-content ui-corner-all" />
        </p>
        <user:submit />
      </fieldset>
    </lift:form>

  def edit = {
    val user = currentUser.open_!

    def doSave() {
      user.validate match {
        case Nil =>
          S.notice("User succesfully saved")
          user.save()
        case n =>
          S.error(n)
      }
    }

    bind("user", userHtml,
      "username" -%> SHtml.text(user.userName.is, text => (), ("readonly", "readonly")),
      "fullname" -%> SHtml.text(user.shortName, name => {
          val idx = name.indexOf(" ")
          if (idx != -1) {
            user.firstName(name.take(idx))
            user.lastName(name.drop(idx + 1))
          } else
            user.firstName(name)
        }),
      "email" -%> SHtml.text(user.email.is, text => (), ("readonly", "readonly")),
      "password" -%> SHtml.password("", user.password(_)),
      "submit" -> SHtml.hidden(doSave _))
  }

  def signup(onSuccess: () => JsCmd) = {
    val user = create

    def doSignup() {
      user.validate match {
        case Nil =>
          S.notice("User succesfully created")
          user.save()
          logUserIn(user)
          onSuccess()
        case n =>
          S.error(n)
      }
    }

    bind("user", userHtml,
      "username" -%> SHtml.text("", user.userName(_)),
      "fullname" -%> SHtml.text("", name => {
          val idx = name.indexOf(" ")
          if (idx != -1) {
            user.firstName(name.take(idx))
            user.lastName(name.drop(idx + 1))
          } else
            user.firstName(name)
        }),
      "email" -%> SHtml.text("", user.email(_)),
      "password" -%> SHtml.password("", user.password(_)),
      "submit" -> SHtml.hidden(doSignup _))
  }

  def loginHtml =
    <lift:form class="login form">
      <fieldset>
        <p>
          <label for="name">Username:</label>
          <user:username class="text required ui-widget-content ui-corner-all" />
        </p>
        <p>
          <label for="password">Password:</label>
          <user:password class="text required ui-widget-content ui-corner-all" />
        </p>
        <user:submit />
      </fieldset>
    </lift:form>

  def login(onSuccess: () => JsCmd) = {
    var username: String = ""
    var password: String = "*"

    def doLogin = {
      find(By(userName, username)) match {
        case Full(user) if user.password.match_?(password) =>
          S.notice("User logged in")
          logUserIn(user)
          onSuccess()
        case _ =>
          S.error("Invalid user credentials")
      }
    }

    bind("user", loginHtml,
      "username" -%> SHtml.text("", username = _),
      "password" -%> SHtml.password("", password = _),
      "submit" -> SHtml.hidden(doLogin _))
  }

  def logout = {
    logoutCurrentUser
  }

}

}
}