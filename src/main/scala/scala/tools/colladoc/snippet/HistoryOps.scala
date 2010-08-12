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
package snippet {

import model.{User, Comment}
import lib.DependencyFactory._
import lib.Helpers._
import lib.Widgets._
import page.History

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._

import xml._

import java.util.{Calendar, Date}
import java.text.SimpleDateFormat

class HistoryOps {

  val dateFormat = new SimpleDateFormat("MM/dd/yyyy")
  var fromDate: Date = today
  var toDate: Date = today.rollDay(1)
  var userName: String =
    if (User.loggedIn_?)
      User.currentUser.open_! userName
    else
      ""

  val history = new History
  import history._

  /** Return history title. */
  def title(xhtml: NodeSeq): NodeSeq =
    Text(history.title)

  /** Return history body. */
  def body(xhtml: NodeSeq): NodeSeq =
    bind("history", history.body,
      "from" -> Datepicker(dateFormat.format(fromDate), dateFrom _, ("class", "date from"), ("readonly", "readonly")),
      "to" -> Datepicker(dateFormat.format(toDate), dateTo _, ("class", "date to"), ("readonly", "readonly")),
      "user" -> Autocomplete(userName, users _, user _),
      "history" -> historyToHtml(fromDate, toDate, userName))

  def users(term: String) =
    User.findAll(Like(User.userName, "%" + term + "%")) map { _.userName.is }

  def user(user: String) = {
    userName = user
    updateHistory
  }

  def dateFrom(dte: String) = {
    fromDate = dateFormat.parse(dte)
    updateHistory
  }

  def dateTo(dte: String) = {
    toDate = dateFormat.parse(dte)
    updateHistory
  }

  def updateHistory =
    Replace("history", historyToHtml(fromDate, toDate, userName)) & Run("reload()") & Run("reinit('#history')")

  def historyToHtml(from: Date, to: Date, user: String): NodeSeq =
    <div id="history">
      { User.find(Like(User.userName, user)) match {
          case Full(u) =>
            commentsToHtml(Comment.findAll(By_>(Comment.dateTime, from), By_<(Comment.dateTime, to), By(Comment.user, u.id.is),
              OrderBy(Comment.dateTime, Descending)))
          case _ =>
            commentsToHtml(Comment.findAll(By_>(Comment.dateTime, from),
              By_<(Comment.dateTime, to),
              OrderBy(Comment.dateTime, Descending)))
        }
      }
    </div>

}

}
}