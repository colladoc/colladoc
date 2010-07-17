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
import net.liftweb.http.js.JE.Str
import net.liftweb.http.js.jquery.JqJE.{Jq, JqClick}
import net.liftweb.http.js.{JsCmds, JsMember}
import xml.{Text, Elem}
import tools.nsc.doc.Universe

class AuthenticatedIndex(universe: Universe) extends Index(universe) {

  override def browser = super.browser \+ login

  def login = {
    def clickUnblock = Jq(Str(".blockOverlay")) ~> new JsMember { def toJsCmd = "click($.unblockUI)" }
    
    <div id="login">
      {
        if (User.loggedIn_?)
          <span>Logged in as { User.currentUser.open_! email }, { SHtml.a(() => { User.logout; JsCmds.Noop }, Text("Log out")) }</span>
        else
          <span>{ SHtml.a(<span>Sign up</span>, ModalDialog(User.signup) & clickUnblock) } or { SHtml.a(<span>Log in</span>, ModalDialog(User.login) & clickUnblock) }.</span>
      }
    </div>
  }

}