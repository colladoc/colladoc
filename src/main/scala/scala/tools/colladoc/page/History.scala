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

import model.{User, Comment, Model}
import model.comment.DynamicModelFactory
import model.comment.DynamicModelFactory._
import model.Model.factory._
import lib.Helpers._
import lib.Widgets._
import lib.DependencyFactory

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._

import collection.mutable.{LinkedList, HashMap}
import tools.nsc.doc.model._
import xml.{NodeSeq, Node, Elem, Text}

import java.util.{Calendar, Date}
import java.text.SimpleDateFormat

class History extends tools.nsc.doc.html.page.Template(Model.model.rootPackage) {

  val dateFormat = new SimpleDateFormat("MM/dd/yyyy")
  var fromDate: Date = today
  var toDate: Date = today.rollDay(1)
  var userName: String =
    if (User.loggedIn_?)
      User.currentUser.open_! userName
    else
      ""

  override val title = "History"

  override val body =
    <body class="history">
      <div id="definition">
        <img src="images/history_big.png" />
        <h1>History</h1>
      </div>
      <div id="template">

        <div id="mbrsel">
          <div id='textfilter'><span class='pre'/><input type='text' accesskey='/'/><span class='post'/></div>
          { <div id="visbl">
              <span class="filtertype">Visibility</span>
              <ol><li class="public in">Public</li><li class="all out">All</li></ol>
            </div>
          }
          { <div id="order">
              <span class="filtertype">Ordering</span>
              <ol><li class="date in">Date</li><li class="alpha out">Alphabetic</li></ol>
            </div>
          }
          { <div id="date">
              <span class="filtertype">Date</span>
              <ol>
                <li class="from">From</li>
                <li>{ Datepicker(dateFormat.format(fromDate), dateFrom _, ("class", "date from"), ("readonly", "readonly")) }</li>
              </ol>
              <ol>
                <li class="to">To</li>
                <li>{ Datepicker(dateFormat.format(toDate), dateTo _, ("class", "date to"), ("readonly", "readonly")) }</li>
              </ol>
            </div>
          }
          { <div id="user">
              <span class="filtertype">User</span>
              <ol><li class="filter">{ Autocomplete(userName, users _, user _) }</li></ol>
            </div>
          }
        </div>
        <h3>Changed Members</h3>
        { historyToHtml(fromDate, toDate, userName) }
      </div>
    </body>

  def users(term: String) = {
    User.findAll(Like(User.userName, "%" + term + "%")) map { _.userName.is }
  }

  def user(user: String) = {
    userName = user
    Replace("history", historyToHtml(fromDate, toDate, userName)) & Run("reload()")
  }

  def dateFrom(dte: String) = {
    fromDate = dateFormat.parse(dte)
    Replace("history", historyToHtml(fromDate, toDate, userName)) & Run("reload()")
  }

  def dateTo(dte: String) = {
    toDate = dateFormat.parse(dte)
    Replace("history", historyToHtml(fromDate, toDate, userName)) & Run("reload()")
  }

//  def historyToHtml(from: Date, to: Date, user: String): NodeSeq = {
//    val historyStatement = """select cmts.* from comments cmts
//      inner join (select qualifiedName, max(dateTime) as dateTime from comments
//        where dateTime >= ? and dateTime <= ? %s group by qualifiedName, changeSet
//      ) cmt on (cmts.qualifiedName = cmt.qualifiedName and cmts.dateTime = cmt.dateTime)"""
//    <div id="history">
//      { User.find(Like(User.userName, user)) match {
//          case Full(u) =>
//            commentsToHtml(Comment.findAllByPreparedStatement({conn =>
//              val stmt = conn.connection.prepareStatement(historyStatement.format("and user_c = ?"))
//              stmt.setDate(1, new java.sql.Date(from.getTime))
//              stmt.setDate(2, new java.sql.Date(to.getTime))
//              stmt.setLong(3, u.id.is)
//              stmt
//            }))
//          case _ =>
//            commentsToHtml(Comment.findAllByPreparedStatement({conn =>
//              val stmt = conn.connection.prepareStatement(historyStatement.format(""))
//              stmt.setDate(1, new java.sql.Date(from.getTime))
//              stmt.setDate(2, new java.sql.Date(to.getTime))
//              stmt
//            }))
//        }
//      }
//    </div>
//  }

  def historyToHtml(from: Date, to: Date, user: String): NodeSeq =
    <div id="history">
      { User.find(Like(User.userName, user)) match {
          case Full(u) =>
            commentsToHtml(Comment.findAll(By_>(Comment.dateTime, from),
              By_<(Comment.dateTime, to),
              By(Comment.user, u.id.is),
              OrderBy(Comment.dateTime, Descending)))
          case _ =>
            commentsToHtml(Comment.findAll(By_>(Comment.dateTime, from),
              By_<(Comment.dateTime, to),
              OrderBy(Comment.dateTime, Descending)))
        }
      }
    </div>

  /**
   * This method and its call may be removed when changeset column will be properly filled
   */
  def merge(cmts: List[Comment]) = {
    cmts.groupBy(c => c.qualifiedName.is + c.user.is).values.flatMap{ cs =>
      cs.head :: ((cs zip cs.tail) collect {
        case (c1, c2) if c1.dateTime.is - c2.dateTime.is > minutes(30) => c2
      })
    }
  }

  import scala.math.Ordering._

  protected def commentsToHtml(cmts: List[Comment]): NodeSeq = {
    val mbrs = merge(cmts).map{ processComment(_) }
    val tpls = HashMap.empty[DocTemplateEntity, List[MemberEntity]]
    for (mbr <- mbrs) {
      val tpl = mbr.inTemplate
      if (!tpls.contains(tpl))
        tpls += tpl -> (mbr :: Nil)
      else {
        tpls += tpl -> (mbr :: tpls(tpl))
      }
    }
    <xml:group>
      { tpls.toList.sortBy{ _._1.name } map{ case (tpl, mbrs) => {
          val date = time(mbrs map{ _.tag } collect{ case c: Comment => c.dateTime.is.getTime } max)
          <div class="changeset" name={ tpl.qualifiedName } date={ timestamp(date).toString }>
            { signature(tpl, true) }
            { membersToHtml(mbrs) }
          </div>
          }
        }
      }
    </xml:group>
  }

  protected def membersToHtml(mbrs: List[MemberEntity]): NodeSeq = {
    val valueMembers = mbrs collect {
      case (tpl: TemplateEntity) if tpl.isObject || tpl.isPackage => tpl
      case (mbr: MemberEntity) if mbr.isDef || mbr.isVal || mbr.isVar => mbr
    }
    val typeMembers = mbrs collect {
      case (tpl: TemplateEntity) if tpl.isTrait || tpl.isClass => tpl
      case (mbr: MemberEntity) if mbr.isAbstractType || mbr.isAliasType => mbr
    }
    val constructors = mbrs collect { case (mbr: MemberEntity) if mbr.isConstructor => mbr }
    <xml:group>
      { if (constructors.isEmpty) NodeSeq.Empty else
          <div id="constructors" class="members">
            <h3>Instance constructors</h3>
            <ol>{ constructors map { memberToHtml(_) } }</ol>
          </div>
      }
      { if (typeMembers.isEmpty) NodeSeq.Empty else
          <div id="types" class="types members">
            <h3>Type Members</h3>
            <ol>{ typeMembers map { memberToHtml(_) } }</ol>
          </div>
      }
      { if (valueMembers.isEmpty) NodeSeq.Empty else
          <div id="values" class="values members">
            <h3>Value Members</h3>
            <ol>{ valueMembers map { memberToHtml(_) } }</ol>
          </div>
      }
    </xml:group>
  }

  override def signature(mbr: MemberEntity, isSelf: Boolean, isReduced: Boolean = false) = mbr.tag match {
    case cmt: Comment => super.signature(mbr, isSelf, isReduced) \\+ <span class="stamp">{ cmt.userNameDate }</span>
    case _ => super.signature(mbr, isSelf, isReduced)
  }

  override def memberToHtml(mbr: MemberEntity) = mbr.tag match {
    case cmt: Comment => super.memberToHtml(mbr) \\% Map("date" -> timestamp(cmt.dateTime.is).toString)
    case _ => super.memberToHtml(mbr)
  }

  def processComment(cmt: Comment) = {
    val mbr = pathToMember(Model.model.rootPackage, cmt.qualifiedName.is.split("""[.#]""").toList)
    val comment = Model.factory.parse(mbr.symbol.get, mbr.template.get, cmt.comment.is)
    DynamicModelFactory.createMember(mbr, comment, cmt)
  }

}

}
}