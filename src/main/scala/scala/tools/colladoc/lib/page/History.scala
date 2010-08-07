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
package lib {
package page {

import model.comment.DynamicModelFactory
import model.comment.DynamicModelFactory._
import model.Model.factory._
import lib.Widgets._
import lib.XmlUtils._

import net.liftweb.http.S._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.{Run, Replace, SetHtml}

import java.net.URLDecoder
import reflect.NameTransformer

import tools.nsc.doc.model._
import net.liftweb.mapper._
import xml.transform.{RuleTransformer, RewriteRule}
import xml._

import java.util.{Calendar, Date}
import java.text.SimpleDateFormat
import net.liftweb.util.Helpers
import net.liftweb.http.js.JE.{JsRaw, Str, JsFunc}
import net.liftweb.http.{JsonResponse, LiftRules, S, SHtml}
import model.{User, Comment, Model}
import net.liftweb.common.Full
import collection.mutable.{LinkedList, HashMap}

class History extends Template(Model.model.rootPackage) {

  val dateFormat = new SimpleDateFormat("MM/dd/yyyy")
  var fromDate: Date = new Date
  var toDate: Date = {
    val calendar = Calendar.getInstance
    calendar.roll(Calendar.DATE, 1)
    calendar.getTime
  }
  var userName: String = {
    if (User.loggedIn_?)
      User.currentUser.open_! userName
    else
      ""
  }

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

  def historyToHtml(from: Date, to: Date, user: String): NodeSeq =
    <div id="history">
      <h3>Changed Members</h3>
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

  protected def commentsToHtml(cmts: List[Comment]): NodeSeq = {
    def diff(date1: Date, date2: Date) = {
      val cal1 = Calendar.getInstance
      cal1.setTime(date1)
      val cal2 = Calendar.getInstance
      cal2.setTime(date2)
      (cal2.getTimeInMillis - cal1.getTimeInMillis) / (60 * 1000)
    }
    def merge(cmts: List[Comment]) = {
      cmts.groupBy(c => c.qualifiedName.is + c.user.is).values.flatMap{ cs =>
        cs.head :: ((cs zip cs.tail) collect {
          case (c1, c2) if diff(c1.dateTime.is, c2.dateTime.is) > 30 => c1
        })
      }
    }
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
      { tpls map { case (tpl, mbrs) =>
          <div class="changeset" name={ tpl.qualifiedName }>
            { signature(tpl, false) }
            { membersToHtml(mbrs) }
          </div>
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

  override def memberToHtml(mbr: MemberEntity) =
    super.memberToHtml(mbr) \\% Map("date" -> mbr.date.getOrElse(new Date).toString)

  def processComment(cmt: Comment) = {
    val mbr = pathToEntity(Model.model.rootPackage, cmt.qualifiedName.is.split("""[.#]""").toList)
    val comment = Model.factory.parse(mbr.symbol.get, mbr.template.get, cmt.comment.is)
    DynamicModelFactory.createMember(mbr, comment, cmt.dateTime.is)
  }

  def updateName(node: NodeSeq, mbr: MemberEntity): NodeSeq = {
    def updateNodes(ns: Seq[Node]): Seq[Node] =
      for(subnode <- ns) yield subnode match {
        case Elem(prefix, "span", attrib @ UnprefixedAttribute("class", Text("name"), _), scope, _) =>
          Elem(prefix, "span", attrib, scope, Text(mbr.qualifiedName))
        case Elem(prefix, label, attribs, scope, children @ _*) =>
          Elem(prefix, label, attribs, scope, updateNodes(children) : _*)
        case Group(children) =>
          Group(updateNodes(children))
        case other => other
      }
    updateNodes(node.theSeq)
  }

  private def pathToEntity(rootPack: Package, path: List[String]): MemberEntity = {
    def doName(mbr: MemberEntity): String = mbr match {
        case tpl: DocTemplateEntity => NameTransformer.encode(tpl.name) + (if (tpl.isObject) "$" else "")
        case mbr: MemberEntity => URLDecoder.decode(mbr.identifier, "UTF-8")
      }
    def downPacks(pack: Package, path: List[String]): (Package, List[String]) = {
      pack.packages.find{ _.name == path.head } match {
        case Some(p) => downPacks(p, path.tail)
        case None => (pack, path)
      }
    }
    def downInner(tpl: DocTemplateEntity, path: List[String]): MemberEntity = path match {
      case p :: r if p.isEmpty => downInner(tpl, r)
      case p :: r =>
        tpl.members.sortBy{ t => -1 * doName(t).length }.find{ t => p.startsWith(doName(t)) } match {
          case Some(t: DocTemplateEntity) => downInner(t, p.stripPrefix(doName(t)).stripPrefix("$") :: r)
          case Some(m: MemberEntity) => m
          case None => tpl
        }
      case Nil => tpl
    }
    downPacks(rootPack, path) match {
      case (pack, "package" :: Nil) => pack
      case (pack, path) => downInner(pack, path)
    }
  }

}

}
}
}