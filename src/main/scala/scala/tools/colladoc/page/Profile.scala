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

/**
 * Profile profile page.
 * @author Sergey Ignatov
 */
class Profile(rootPack: Package) extends scala.tools.colladoc.page.Template(rootPack) {
  /**Page title. */
  override val title = "User"

  /** Page body. */
  override val body =
    <body class="profile">
      <div id="definition">
        <img src="/images/profile_big.png" />
        <h1><profile:fullname /></h1>
      </div>
      <div id="template">
        <div id="profile_tabs">
          <ul>
            <li><a href="#profile_tab">Public Profile</a></li>
            <li><a href="#account_admin_tab">Account Admin</a></li>
            <li><a href="#comments_tab">Comments</a></li>
            <li><a href="#discussion_comments_tab">Discussion Comments</a></li>
          </ul>
          <div id="profile_tab">
            <profile:form />
          </div>
          <div id="account_admin_tab">
              <profile:change_password />
              <profile:delete_profile />
          </div>
          <div id="comments_tab">
            <profile:comments />
          </div>
          <div id="discussion_comments_tab">
            <profile:discussion_comments />
          </div>
        </div>
      </div>
    </body>
}
