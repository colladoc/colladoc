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

import lib.Helpers
import lib.Helpers._
import model.Comment

import net.liftweb.common._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._

import tools.nsc.Global
import tools.nsc.doc.model.comment.CommentFactory
import tools.nsc.doc.model._

import java.util.Date

trait PersistableCommentFactory extends UpdatableCommentFactory { thisFactory: ModelFactory with CommentFactory =>

  val global: Global
  import global.reporter

  override def comment(sym: global.Symbol, inTpl: => DocTemplateImpl) = {
    updatedComment(sym, inTpl) match {
      case Some(com) => global.docComment(sym, com.comment.is)
      case None =>
    }
    super.comment(sym, inTpl)
  }
  
  override def update(mbr: MemberEntity, docStr: String) = {
    super.update(mbr, docStr)
    if (!reporter.hasWarnings && !reporter.hasErrors) {
      val comment: Comment = Comment.create
        .qualifiedName(mbr.uniqueName)
        .comment(docStr)
        .dateTime(new Date)
        .user(User.currentUser.open_!)

      Comment.findAll(By(Comment.qualifiedName, mbr.uniqueName), By(Comment.user, User.currentUser.open_!),
          OrderBy(Comment.dateTime, Descending), MaxRows(1)) match {
        case List(c: Comment, _*) if c.dateTime.is - comment.dateTime.is < minutes(30) =>
          comment.changeSet(c.changeSet.is)
        case _ =>
          comment.changeSet(new Date)
      }

      comment.save
    }
  }

  private def updatedComment(sym: global.Symbol, inTpl: => DocTemplateImpl) = {
    makeMember(sym, inTpl) match {
      case List(mbr: MemberEntity, _*) =>
        Comment.findAll(By(Comment.qualifiedName, mbr.uniqueName),
          OrderBy(Comment.dateTime, Descending), MaxRows(1)) match {
          case List(c: Comment, _*) if c.dateTime.is.getTime > sym.sourceFile.lastModified => Some(c)
          case _ => None
        }
      case Nil => None
    }
  }

  implicit def isUpdated(mbr: MemberEntity) = new {
    def isUpdated() =
      if (mbr.comment.isDefined) {
        val com: UpdatableComment = mbr.comment.get.asInstanceOf[UpdatableComment]
        updatedComment(com.sym, com.inTpl) match {
          case Some(c) => true
          case None => false
        }
      } else false
  }
  
}

}
}
}