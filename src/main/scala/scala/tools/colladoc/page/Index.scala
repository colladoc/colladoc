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
import model.mapper.User

import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js._
import net.liftweb.http.js.jquery.JqJE._
import net.liftweb.http.js.JE._
import net.liftweb.widgets.gravatar.Gravatar

import tools.nsc.doc.Universe
import xml.Text
import lib.js.JqUI._
import snippet.HelpOps

/**
 * Page containing index of all symbols and user panel.
 * @author Petr Hosek 
 */
class Index(universe: Universe) extends tools.nsc.doc.html.page.Index(universe) {

 override def browser = super.browser \+
    <div id="user">
      { if (User.loggedIn_?)
          loggedIn
        else
          loggedOut
      }
    </div>

  override def body =
    <body>
      <div id="library">
        <img class='class icon' width="13" height="13" src={ relativeLinkTo{List("class.png", "lib")} }/>
        <img class='trait icon' width="13" height="13" src={ relativeLinkTo{List("trait.png", "lib")} }/>
        <img class='object icon' width="13" height="13" src={ relativeLinkTo{List("object.png", "lib")} }/>
        <img class='package icon' width="13" height="13" src={ relativeLinkTo{List("package.png", "lib")} }/>
      </div>
      { browser }
      <div id="content" class="ui-layout-center">
        <iframe name="template" src={ relativeLinkTo{List("history.html")} }/>
      </div>
    </body>

  /** Render user panel for logged out user. */
  private def loggedOut =
    <xml:group>
      <ul class="usernav">
        <li><a href="/history.html" target="template">History</a></li>
        <li>{ SHtml.a(Text("Signup"), Jq(Str(".user")) ~> OpenDialog()) }</li>
        <li>{ SHtml.a(Text("Help"), Jq(Str(".help")) ~> OpenDialog()) }</li>
        <li>{ SHtml.a(Text("Login"), CmdPair(Jq(Str(".login")) ~> OpenDialog(), Focus("username"))) }</li>
      </ul>
      { User.signup }
      { User.login }
      { HelpOps.help }
    </xml:group>

  /** Render user panel for logged in user. */
  private def loggedIn =
    <xml:group>
      <div class="avatar">
        { SHtml.a(
            Gravatar(User.currentUser.open_! email, 16) ++
            SHtml.span(Text(User.currentUser.open_! userName), Noop, ("class", "name")),
            Jq(Str(".user")) ~> OpenDialog()) }
      </div>
      <ul class="usernav">
        { if (User.superUser_?)
            <li>
              { SHtml.a(Text("Settings"),
                  Jq(Str(".admin")) ~> OpenDialog() &
                  JsRaw(
                    """
                      jQuery().ready(function () {
                        jQuery("#userlist").jqGrid({
                            url:'grid/users?',
                            datatype: "xml",
                            colNames:['Username', 'Email', 'OpenID', 'Superuser', ''],
                            colModel:[
                              {name:'name',index:'name'},
                              {name:'email',index:'email'},
                              {name:'openid',index:'openid', width:350},
                              {name:'superuser',index:'superuser', width: 60},
                              {name:'delete', index:'delete', width: 18}
                            ],
                            rowList:[5,10,20,30],
                            pager: '#userpager',
                            viewrecords: true,
                            sortname: 'username',
                            sortorder: 'desc',
                            autowidth: true,
                            caption: 'User list'
                          }).navGrid('#userpager',{edit:false,add:false,del:false});
                      });
                    """)
                )
              }
            </li>
        }
        <li><a href="/history.html" target="template">History</a></li>
        <li>{ SHtml.a(Text("Help"), Jq(Str(".help")) ~> OpenDialog()) }</li>
        <li>{ SHtml.a(() => {User.logout; JsCmds.Noop}, Text("Log Out")) }</li>
      </ul>
      { User.edit }
      { if (User.superUser_?) User.adminForm }
      { if (User.superUser_?) User.createUser }
      { HelpOps.help }
    </xml:group>

}

}
}
