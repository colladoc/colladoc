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

import tools.nsc.doc.html.page.Index
import lib.XmlUtils._
import model.{User, Model}

import net.liftweb.http.{S, SHtml}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.http.js.{JsCmds, JsMember}
import tools.nsc.doc.Universe

import net.liftweb.widgets.gravatar.Gravatar
import net.liftweb.http.js.jquery.JqJE.{JqId, Jq, JqClick}
import net.liftweb.http.js.JE.{JsFunc, Str}
import xml.{NodeSeq, Text, Elem}

class UserIndex(universe: Universe) extends Index(universe) {

  override def browser = super.browser \+ login
  
  def login: Elem =
    <div id="user">
      { if (User.loggedIn_?)
          loggedIn
        else
          loggedOut
      }
    </div>

  private def loggedOut =
    <xml:group>
      <ul class="usernav">
        <li>
          { SHtml.a(Text("Signup"), openDialog(".user")) }
        </li>
        <li>
          { SHtml.a(Text("Login"), openDialog(".login")) }
        </li>
      </ul>
      { User.signup }
      { User.login }
    </xml:group>

  private def loggedIn =
    <xml:group>
      <div class="avatar">
        { Gravatar(User.currentUser.open_! email, 16) }
        <span class="name">
          { User.currentUser.open_! userName }
        </span>
      </div>
      <ul class="usernav">
        <li>
          { SHtml.a(Text("Settings"), openDialog(".user")) }
        </li>
        <li>
          { SHtml.a(() => { User.logout; RedirectTo("/") }, Text("Log Out")) }
        </li>
      </ul>
      { User.edit }
    </xml:group>

  private def openDialog(id: String) = Jq(Str(id)) ~> new JsMember { def toJsCmd = "dialog('open')" }
  
}