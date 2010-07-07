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

import net.liftweb.mapper._
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds._

class User extends ProtoUser[User] with OneToMany[Long, User]  {
  def getSingleton = User

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

  def signupHtml =
    <form class="loginform" method="post" action={ S.uri }>
      <h3>Sign Up New User</h3>
      <table>
        <tr><th>Full Name:</th><td><user:fullname /></td></tr>
        <tr><th></th><td><span class="note">First and last name</span></td></tr>
        <tr><th>Email:</th><td><user:username /></td></tr>
        <tr><th></th><td><span class="note">Used as a username to login</span></td></tr>
        <tr><th>Password:</th><td><user:password /></td></tr>
        <tr><th></th><td><user:submit /></td></tr>
      </table>
    </form>

  def signup = {
    val user = create

    def doSignup() {
      user.validate match {
        case Nil => S.notice("User saved")
          user.save()
          logUserIn(user)
        case n => S.error(n)
          Console.println(n)
      }
    }

    bind("user", signupHtml,
      "fullname" -> FocusOnLoad(SHtml.text("", name => {
          val idx = name.indexOf(" ")
          user.firstName(name.take(idx))
          user.lastName(name.drop(idx + 1))
        })),
      "username" -> SHtml.text("", user.email(_)),
      "password" -> SHtml.password("", user.password(_)),
      "submit" -> SHtml.submit("Sign Up", doSignup _, ("class", "button")))
  }

  def loginHtml = {
    <form class="loginform" method="post" action={ S.uri }>
      <h3>Sign In User</h3>
      <table>
        <tr><th>Email:</th><td><user:username /></td></tr>
        <tr><th>Password:</th><td><user:password /></td></tr>
        <tr><th>&nbsp;</th><td><user:submit /></td></tr>
      </table>
    </form>
  }

  def login = {
    var username: String = ""
    var password: String = "*"

    def doLogin = {
      find(By(email, username)) match {
        case Full(user) if user.password.match_?(password) =>
          S.notice("User logged in")
          logUserIn(user)
          //S.redirectTo(homePage)
        case _ => S.error("Invalid user credentials")
      }
    }

    bind("user", loginHtml,
      "username" -> SHtml.text("", username = _),
      "password" -> SHtml.password("", password = _),
      "submit" -> SHtml.submit("Log In", doLogin _, ("class", "button")))
  }

  def logout = {
    logoutCurrentUser
    S.redirectTo("/")
  }

}