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
package scala.tools.colladoc
package model
package comment

import tools.nsc.Global
import tools.nsc.doc.model.comment.{Comment => ModelComment, CommentFactory}
import scala.tools.colladoc.model.{Comment}
import net.liftweb.common.{Full, Empty}
import net.liftweb.mapper._
import java.util.Date
import tools.nsc.doc.model.{ValueParam, Def, MemberEntity, ModelFactory}

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
        .qualifiedName(mbr.qualifiedIdentifier)
        .comment(docStr)
        .dateTime(new Date)
        .user(User.currentUser.open_!)
      comment.save
    }
  }

  implicit def identifiers(mbr: MemberEntity) = new {
    def identifier() = mbr match {
      case fnc: Def =>
        def params(vlss: List[ValueParam]): String = vlss match {
          case Nil => ""
          case vl :: Nil => vl.resultType.name
          case vl :: vls => vl.resultType.name + ", " + params(vls)
        }
        fnc.name + fnc.valueParams.map{ "(" + params(_) + ")" }.mkString
      case _ => mbr.name
    }

    def qualifiedIdentifier() = mbr match {
      case fnc: Def => fnc.inTemplate.qualifiedName + "#" + identifier
      case _ => mbr.qualifiedName
    }
  }
  
  private def updatedComment(sym: global.Symbol, inTpl: => DocTemplateImpl) = {
    makeMember(sym, inTpl) match {
      case List(mbr, _*) =>
        Comment.findAll(By(Comment.qualifiedName, mbr.qualifiedIdentifier),
          OrderBy(Comment.dateTime, Descending),
          MaxRows(1)) match {
          case List(com: Comment, _*) if com.dateTime.is.getTime > sym.sourceFile.lastModified => Some(com)
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