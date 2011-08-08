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
 * Settings page.
 * @author Sergey Ignatov
 */
class Help(rootPack: Package) extends scala.tools.colladoc.page.Template(rootPack) {
  /**Page title. */
  override val title = "Help"

  /** Page body. */
  override val body =
    <body class="help">
      <div id="definition">
        <img src="images/help_big.png" />
        <h1>Help</h1>
      </div>
      <div class="template">
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
              Mergedoc sources you can find at <a href="https://github.com/colladoc/mergedoc" class="link">GitHub</a>.
            </p>
            <h4>Helpful links:</h4>
            <p>
              <ul>
                <li><a href="http://colladoc.github.com/" class="link">Official site</a></li>
                <li><a href="http://colladoc.posterous.com/" class="link">Official blog</a></li>
                <li><a href="https://github.com/colladoc/colladoc/wiki" class="link">Wiki</a></li>
              </ul>
            </p>
          </div>
          <div id="help_edit">
            <h3>Regular users</h3>
            <p>
              Colladoc allows registered users edit comments for existing entities.
            </p>
            <h3>Superusers</h3>
            <p>
              Colladoc allows superusers:
              <ul>
                <li>Edit comments for existing entities.</li>
                <li>Delete comments for existing entities.</li>
                <li>Propagate comments in hierarchy.</li>
                <li>Select the default comment from the comment list.</li>
              </ul>
            </p>
          </div>
          <div id="help_search">
            <h3>Here are some sample queries to get you started:</h3>
            <div>
              <ul class="search_example_list">
                <li><a target="template" href="/search?q=any" >any</a>
                  searches for everything that has the word any in its name, definition or comment
                </li>
                <li><a target="template" href="/search?q=any_">any_</a>
                  searches for everything that starts with any
                </li>
                <li><a target="template" href="/search?q=//_any">//_any</a>
                  searches for all comments that contain a word that ends with any
                </li>
                <li><a target="template" href="/search?q=class AnyRef">class AnyRef</a>
                  searches for all classes with name AnyRef
                </li>
                <li><a target="template" href="/search?q=trait _">trait _</a>
                  searches for all traits
                </li>
                <li><a target="template" href="/search?q=object _">object _</a>
                  searches for all objects
                </li>
                <li><a target="template" href="/search?q=class A_ || class B_">class A_ || class B_</a>
                  searches for all classes that starts with A or B
                </li>
                <li><a target="template" href="/search?q=class  _ extends _ with _">class  _ extends _ with _</a>
                  searches for all classes that extend a class and implement a trait
                </li>
                <li><a target="template" href="/search?q=var _: Int">var _: Int</a>
                  searches for all values or variables of type Int, vars are displayed before the vals
                </li>
                <li><a target="template" href="/search?q=def toString">def toString</a>
                  searches for all methods with name toString
                </li>
                <li><a target="template" href="/search?q=def toString : String">def toString : String</a>
                  searches for all methods with name toString and return type String
                </li>
                <li><a target="template" href="/search?q=def _(_) : Boolean">def _(_) : Boolean</a>
                  searches for all methods with one argument and  returnType Boolean
                </li>
                <li><a target="template" href="/search?q=def _(Int, _)">def _(Int, _)</a>
                  searches for all methods with arguments and the first is of type Int
                </li>
                <li><a target="template" href="/search?q=def _(_, *)">def _(_, *)</a>
                  searches for all methods with one or more arguments
                </li>
                <li><a target="template" href="/search?q=def _(Iterable[_]):Int">def _(Iterable[_]):Int</a>
                  searches for all methods that take an Iterable and return Int
                </li>
                <li><a target="template" href="/search?q=(Iterable[_]) %3D> Int">(Iterable[_]) =&gt; Int</a>
                  equvalent to the above, lambda syntax can also be used for searching for methods.
                </li>
                <li><a target="template" href="/search?q=%3D> (_, _)">=&gt; (_, _)</a>
                  searches for all methods that return a tuple with two elements.
                </li>
                <li><a target="template" href="/search?q=def _((_) %3D> _)">def _((_) =&gt; _)</a>
                  searches for all methods that have one as first parameter a method that takes and returns any value.
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </body>
}