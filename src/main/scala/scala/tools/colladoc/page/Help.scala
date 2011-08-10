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
            <li><a href="#help_comments">Comments</a></li>
            <li><a href="#help_discussions">Discussions</a></li>
            <li><a href="#help_search">Search</a></li>
          </ul>
          <div id="help_about">
            <h2>Colladoc is a web application allowing to edit Scala symbols documentation.</h2>
            <p>
              This application is based on existing Scaladoc 2 sources converted into full featured web application using the Lift web framework.
              The application internally uses the documentation model constructed using Scala compiler frontend in the same way as Scaladoc 2 does.
            </p>
            <p>
              The interface of this web application is extended in order to allow wiki-like editing of Scala symbol comments.
              Application also allows to show different versions of documentation related to single symbol for each users together
              with displaying history of all changes in the form of aggregated timeline.
            </p>
            <p>
              The <b>Colladoc</b> web application also provides REST interface which provide access to the collected comments.
              This allows to export the changes and merge them into original source code.
            </p>
            <p>
              The <b>Mergedoc</b> application is a command-line tool reimplementing the functionality of scaladoc-merge tool providing same features.
              The application allows to merge changes exported from the Colladoc web application to directly to source codes.
            </p>
            <p>
              <h3>Useful links:</h3>
              <ul>
                <li><a href="http://colladoc.github.com/" class="link">Official site</a></li>
                <li><a href="http://colladoc.posterous.com/" class="link">Official blog</a></li>
                <li><a href="https://github.com/colladoc/colladoc" class="link">Sources</a></li>
                <li><a href="https://github.com/colladoc/colladoc/wiki" class="link">Wiki</a></li>
              </ul>
            </p>
          </div>
          <div id="help_comments">
            <h2>Browsing</h2>
            <p>
              Colladoc application is based upon <a href="http://www.scala-lang.org/api">Scaladoc</a>.
              Therefore, basic functionality, such is browsing documentation works in the exactly same way.
              Moreover, this functionality is similar to other well-known tools such as <a href="http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/">Javadoc</a>.
            </p>
            <h2>Editing</h2>
            <p>
              Colladoc allows you to edit comments for any symbol.
              To edit the comment, you need to use the edit mode for selected symbol which can be switched by clicking the edit link.
              This will replace the symbol comment with inline editor which can be used to edit the comment (or even create in case the symbol has no comment).
              Finally, you may choose to submit or cancel the changes. <em>Note that you have to be logged in to edit the comments.</em>
            </p>
            <div>
              <h3>Regular users</h3>
              <p>
                Registered users can edit comments for existing symbols.
              </p>
              <h3>Superusers</h3>
              <p>
                Furthermore, superusers are allowed to:
                <ul>
                  <li>edit and delete comments for existing symbols,</li>
                  <li>propagate comments in symbols hierarchy,</li>
                  <li>select the default revision for each symbol comment.</li>
                </ul>
              </p>
            </div>
            <h2>Exporting</h2>
            <p>
              Comment changes may be exported in the XML format used by scaladoc-merge tool.
              This tool allows you to merge the changed comments into the original source code.
              To export the changes, click the export link.
            </p>
            <p>
              The Scaladoc merge tool can be installed in Eclipse using this update site.
              Once installed, context-click on the project containing the documentation you want to update, and choose "Merge Scaladoc...".
            </p>
            <h2>History</h2>
            <p>
              History of all changes can be seen for every symbol. Previous versions of each change can be seen, edited and exported.
            </p>
            <p>
              The history page contains the timeline of all changes done in selected time-frame.
              Moreover this may be filtered according to selected to user.
              The aggregation is used so that changes done by one user in short time-frame will be displayed as one.
            </p>
          </div>
          <div id="help_discussions">
            <h2>Discussions</h2>
            <p>
              Colladoc allows you to add additional content in the form of discussions, similar to discussions known from blogs and other websites.
              Not only allow you this functionality to submit related content, it also allows you to ask/answer questions and propose improvements to existing content.
            </p>
            <p>
              Discussion section is available for each top-level symbol (i.e. packages, classes, objects and traits) and is collapsed by default.
            </p>
            <h2>Categories</h2>
            <p>
              Discussion content is divided into categories. These are pre-defined globally for the application.
              Each category may have different policy, specifying whether anonymous users are allowed to see and/or edit its content.
            </p>
            <p>
              Categories and their policies can be managed onyl by superusers.
            </p>
          </div>
          <div id="help_search">
            <p>
              The search functionality provides a full-text search capabilities making it possible to search for identifiers, symbols and documentation comments.
              This functionality provides powerful query syntax that resembles the Scala language.
            </p>
            <p>
              Search queries can be entered using the search box. Relevant search results are presented on a separate search page.
              This page uses infinite scrolling to asynchronously load additional results when needed.
            </p>
            <div>
              <h3>Sample queries to get you started:</h3>
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