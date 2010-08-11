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
package model {
package comment {

import lib.Helpers._

import net.liftweb.common._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._

import tools.nsc.Global
import tools.nsc.doc.model._
import tools.nsc.doc.model.comment._
import tools.nsc.util._

import java.util.Date

trait DynamicCommentFactory extends CommentFactory { thisFactory: DynamicModelFactory with CommentFactory =>

  val global: Global
  import global.reporter

  override def comment(sym: global.Symbol, inTpl: => DocTemplateImpl) = {
    val cmt = super.comment(sym, inTpl)
    latest(sym, inTpl) match {
      case Some(c) => Some(parse(sym, inTpl, c.comment.is))
      case None => Some(new CommentProxy(cmt.getOrElse(empty))(sym, inTpl))
    }
  }

  def parse(sym: global.Symbol, inTpl: => DocTemplateImpl, comment: String): Comment = {
    val str = global.expandedDocComment(sym, inTpl.sym, comment).trim
    new CommentProxy(parse(sym, str, comment, NoPosition))(sym, inTpl)
  }

  def update(mbr: MemberEntity, docStr: String) = {
    def saveUpdated() = {
      val usr = model.User.currentUser.open_!
      val cmt = model.Comment.create.qualifiedName(mbr.uniqueName).comment(docStr).dateTime(now).user(usr)
      model.Comment.findAll(By(model.Comment.qualifiedName, mbr.uniqueName), By(model.Comment.user, usr),
          OrderBy(model.Comment.dateTime, Descending), MaxRows(1)) match {
        case List(c: Comment, _*) if c.dateTime.is - cmt.dateTime.is < minutes(30) =>
          cmt.changeSet(c.changeSet.is)
        case _ =>
          cmt.changeSet(now)
      }
      cmt.save
    }
    
    if (mbr.comment.isDefined) {
      val prx = mbr.comment.get.asInstanceOf[CommentProxy]
      val str = global.expandedDocComment(prx.sym, prx.inTpl.sym, docStr).trim
      val cmt = parse(prx.sym, str, docStr, NoPosition)
      if (!reporter.hasWarnings && !reporter.hasErrors) {
        prx.cmt = cmt
        saveUpdated
      }
    }
  }

  private def latest(sym: global.Symbol, inTpl: => DocTemplateImpl) = makeMember(sym, inTpl) match {
    case List(mbr: MemberEntity, _*) => model.Comment.latest(mbr.uniqueName)
    case Nil => None
  }

  implicit def symbols(mbr: MemberEntity) = new {
    def symbol(): Option[global.Symbol] = mbr.comment match {
      case Some(prx: CommentProxy) => Some(prx.sym)
      case _ => None
    }

    def template(): Option[DocTemplateImpl] = mbr.comment match {
      case Some(prx: CommentProxy) => Some(prx.inTpl)
      case _ => None
    }

    def isUpdated(): Boolean = mbr.comment match {
      case Some(prx: CommentProxy) => latest(prx.sym, prx.inTpl) match {
          case Some(c) => true
          case None => false
      }
      case _ => false
    }

    def originalComment(): Option[Comment] = mbr.comment match {
      case Some(prx: CommentProxy) =>
        val cmt = DynamicCommentFactory.super.comment(prx.sym, prx.inTpl)
        Some(new CommentProxy(cmt.getOrElse(empty))(prx.sym, prx.inTpl))
      case _ => None
    }
  }

  private class CommentProxy(var cmt: Comment)(val sym: global.Symbol, val inTpl: DocTemplateImpl) extends Comment {
    def body = cmt.body
    def authors = cmt.authors
    def see = cmt.see
    def result = cmt.result
    def throws = cmt.throws
    def valueParams = cmt.valueParams
    def typeParams = cmt.typeParams
    def version = cmt.version
    def since = cmt.since
    def todo = cmt.todo
    def deprecated = cmt.deprecated
    def note = cmt.note
    def example = cmt.example
    def short = cmt.short
    def source = cmt.source

    override def equals(other: Any) = other match {
      case that: CommentProxy => cmt.equals(that.cmt)
      case _ => cmt.equals(other)
    }
    override def hashCode = cmt.hashCode
  }

  private object empty extends Comment {
    def body = new Body(Nil)
    def authors = List()
    def see = List()
    def result = None
    def throws = Map.empty[String, Body]
    def valueParams = Map.empty[String, Body]
    def typeParams = Map.empty[String, Body]
    def version = None
    def since = None
    def todo = List()
    def deprecated = None
    def note = List()
    def example = List()
    def short = Text("")
    def source = None
  }

}

}
}
}