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
import tools.nsc.doc.model.{MemberEntity, DocTemplateEntity, ModelFactory}
import collection.mutable.WeakHashMap
import tools.nsc.doc.model.comment.{Text, Body, Comment, CommentFactory}
import net.liftweb.common.{Empty, Full}
import net.liftweb.mapper._

trait UpdatableCommentFactory extends CommentFactory { thisFactory: ModelFactory with CommentFactory =>

  val global: Global
  import global.reporter

  override def comment(sym: global.Symbol, inTpl: => DocTemplateImpl): Option[Comment] =
    Some(super.comment(sym, inTpl) match {
      case Some(c) => new UpdatableComment(c)(sym, inTpl)
      case None => new UpdatableComment(EmptyComment)(sym, inTpl)
    })

  def update(mbr: MemberEntity, docStr: String) = {
    val com: UpdatableComment = mbr.comment.get.asInstanceOf[UpdatableComment]
    global.docComment(com.sym, docStr)
    val key = (com.sym, com.inTpl)
    commentCache -= key
    super.comment(com.sym, com.inTpl) match {
      case Some(c) => com.comment = c
      case None =>
    }
  }

  class UpdatableComment(var comment: Comment)(val sym: global.Symbol, val inTpl: DocTemplateImpl) extends Comment {
    def body = comment.body
    def authors = comment.authors
    def see = comment.see
    def result = comment.result
    def throws = comment.throws
    def valueParams = comment.valueParams
    def typeParams = comment.typeParams
    def version = comment.version
    def since = comment.since
    def todo = comment.todo
    def deprecated = comment.deprecated
    def note = comment.note
    def example = comment.example
    def short = comment.short
    def source = comment.source
  }

  object EmptyComment extends Comment {
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

  implicit def symbols(mbr: MemberEntity) = new {
    def symbol(): Option[global.Symbol] = mbr.comment match {
      case Some(uc: UpdatableComment) => Some(uc.sym)
      case _ => None
    }
  }

}