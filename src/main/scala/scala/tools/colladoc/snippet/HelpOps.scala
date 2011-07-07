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
package scala.tools.colladoc.snippet

/**
 * Snippet for help page.
 * @author Sergey Ignatov
 */
object HelpOps {
  def help =
    <div class="help">
      <div id="help_tabs">
        <ul>
          <li><a href="#help_about">About</a></li>
          <li><a href="#help_edit">Editing</a></li>
          <li><a href="#help_search">Search</a></li>
        </ul>
        <div id="help_about">
          <h4>About:</h4>
          <p>
            Colladoc is web application allowing to edit Scala symbols documentation.
          </p>
          <h4>Mergedoc integration:</h4>
          <p>
            The Colladoc web application also provides REST interface which provide access to the collected comments. <br />
            This allows to export the changes and merge them into original source code. <br />
            Mergedoc sources you can find at <a href="https://github.com/collaborative-scaladoc/mergedoc" class="link">GitHub</a>.
          </p>
          <h4>Helpful links:</h4>
          <p>
            <ul>
              <li><a href="http://collaborative-scaladoc.github.com/" class="link">Official site</a></li>
              <li><a href="http://collaborative-scaladoc.posterous.com/" class="link">Official blog</a></li>
              <li><a href="https://github.com/collaborative-scaladoc/colladoc/wiki" class="link">Wiki</a></li>
            </ul>
          </p>
        </div>
        <div id="help_edit">
        </div>
        <div id="help_search">
        </div>
      </div>
    </div>
}