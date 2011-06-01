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
package mapper {

import net.liftweb.mapper._
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.http._
import js.JsCmds._
import net.liftweb.util.Helpers._
import xml.Text

/**
 * Mapper for user table storing registered users.
 * @author Petr Hosek
 */
class User extends ProtoUser[User] with OneToMany[Long, User]  {
  def getSingleton = User

  /** Username. */
  object userName extends MappedString(this, 32) {
    override def dbIndexed_? = true
    override def validations = valUnique(S.??("unique.user.name")) _ :: super.validations
    override def displayName = fieldOwner.userNameDisplayName
    override val fieldId = Some(Text("txtFirstName"))
  }

  object openId extends MappedString(this, 100) {
    override def dbIndexed_? = true
  }

  def userNameDisplayName = S.??("user.name")

  /** User comment changes. */
  object comments extends MappedOneToMany(Comment, Comment.user)

  /** Table entry. */
  def toTableRow = {
    val row =
      <tr>
        <td>{userName}</td>
        <td>{email}</td>
        <td>{openId}</td>
        <td><row:superuser /></td>
      </tr>

      bind("row", row,
      "superuser" -%> SHtml.ajaxCheckbox(
        superUser, bool => {
          superUser(bool)
          save
          Noop
        }))
    }
}

/**
 * Mapper for user table storing registered users.
 * @author Petr Hosek
 */
object User extends User with KeyedMetaMapper[Long, User] {
  override def dbTableName = "users"

  /** Current logged in user identifier */
  private object curUserId extends SessionVar[Box[String]](Empty)

  /** Get current logged in user identifier */
  def currentUserId: Box[String] = curUserId.is

  /** Current logged in user */
  private object curUser extends RequestVar[Box[User]](currentUserId.flatMap(id => find(id))) with CleanRequestVarOnSessionTransition

  /** Get current logged in user */
  def currentUser: Box[User] = curUser.is

  /** Whether currently logged in user is superuser */
  def superUser_? : Boolean = currentUser.map(_.superUser.is) openOr false

  /** Whether any user is logged in. */
  def loggedIn_? = currentUserId.isDefined
  /** Log in user with given identifier. */
  def logUserIdIn(id: String) {
    curUser.remove()
    curUserId(Full(id))
  }
  /** Log in user. */
  def logUserIn(who: User) {
    curUser.remove()
    curUserId(Full(who.id.toString))
  }

  /** Log out current user. */
  def logoutCurrentUser = logUserOut()
  /** Log out user. */
  def logUserOut() {
    curUserId.remove()
    curUser.remove()
    S.request.foreach(_.request.session.terminate)
  }

  def createIfNew(openid: String): User = {
    find(By(User.openId, openid)).openOr {
      val newUser = User.create.openId(openid)
      newUser.save()
      newUser
    }
  }

  /** Singup user form. */
  def singupUserHtml =
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
        <p>
          <label for="openid">OpenID:</label>
          <user:openid class="text ui-widget-content ui-corner-all" />
        </p>
        <p>
          By singing up you acknowledge you have read and agree to the
          <a href="http://www.scala-lang.org/sites/default/files/contributor_agreement.pdf" class="link">Contributor License Agreement</a>.
        </p>
        <user:submit />
      </fieldset>
    </lift:form>

  /** Edit user form. */
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
        <p>
          <label for="openid">OpenID:</label>
          <user:openid class="text ui-widget-content ui-corner-all" />
        </p>
        <user:submit />
      </fieldset>
    </lift:form>

  /** Edit user dialog. */
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
      "openid" -%> SHtml.text(user.openId.is, text => (), ("readonly", "readonly")),
      "submit" -> SHtml.hidden(doSave _))
  }

  private def toTable(users: List[User]) = {
    val rows = users.map(_.toTableRow)
    <lift:UserListSortable />
    <table id="user_list" class="tablesorter">
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>OpenID</th>
          <th>Superuser</th>
        </tr>
      </thead>
      <tbody>
        {rows}
      </tbody>
    </table>
  }

  /** Admin user form. */
  def adminHtml =
    <div class="admin">
      <h4>Work with source code:</h4>
      {SHtml.a(() => {
        Model.rebuild
        S.notice("Model succesfully reloaded from source")
        Noop
      }, Text("Reload from source"), ("class", "link"))}
      <br />
      {SHtml.a(() => {
        S.notice("Comments succesfully merged"); Noop // TODO: add action
      }, Text("Merge comments"), ("class", "link"))}
      <h4>Users:</h4>
      {toTable(User.findAll(OrderBy(User.userName, Descending)))}
    </div>

  /** Admin dialog for superuser. */
  def adminForm = adminHtml

  /** Signup user dialog. */
  def signup = {
    val user = create

    def doSignup() {
      user.validate match {
        case Nil =>
          S.notice("User succesfully created")
          user.save()
          logUserIn(user)
          RedirectTo("/")
        case n =>
          S.error(n)
      }
    }

    bind("user", singupUserHtml,
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
      "openid" -%> SHtml.text("", user.openId(_)),
      "submit" -> SHtml.hidden(doSignup _))
  }

  /** Login user form. */
  def loginHtml =
    <div class="login form">
      <lift:form>
        <fieldset>
          <p>
            <label for="name">Username:</label>
            <user:username id="username" class="text required ui-widget-content ui-corner-all" />
          </p>
          <p>
            <label for="password">Password:</label>
            <user:password id="password" class="text required ui-widget-content ui-corner-all" />
          </p>
          <user:submit />
        </fieldset>
      </lift:form>
      <div>
        <ul class="providers">
          <li class="direct" title="Google">
            <a href="javascript:void(0);" id="google"><img src="images/google.png" alt="Sign in with Google"/></a>
          </li>
          <li class="direct" title="Yahoo">
            <a href="javascript:void(0);" id="yahoo"><img src="images/yahoo.png" alt="Sign in with Yahoo"/></a>
          </li>
        </ul>
      </div>
      <a href="javascript:void(0);" id="openid_switcher" class="link">more OpenID</a>
      <form id="openid_form" class="hidden" method="post" action="/openid/login">
        <input id="openid_identifier" type="text" name="openid_identifier" class="text required ui-widget-content ui-corner-all" />
      </form>
    </div>

  /** Login user dialog. */
  def login = {
    var username: String = ""
    var password: String = "*"

    def doLogin = {
      find(By(userName, username)) match {
        case Full(user) if user.password.match_?(password) =>
          S.notice("User logged in")
          logUserIn(user)
          RedirectTo("/")
        case _ =>
          S.error("Invalid user credentials")
      }
    }

    bind("user", loginHtml,
      "username" -%> SHtml.text("", username = _),
      "password" -%> SHtml.password("", password = _),
      "submit" -> SHtml.hidden(doLogin _))
  }

  /** Logout user. */
  def logout = {
    logoutCurrentUser
  }

}

}
}
}