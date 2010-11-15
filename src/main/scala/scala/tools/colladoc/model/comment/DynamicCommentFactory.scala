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

import lib.util.NameUtils._

import tools.nsc.Global
import tools.nsc.doc.model._
import tools.nsc.doc.model.comment._
import tools.nsc.util._

/**
 * Dynamic comment factory extending [CommentFactory] with dynamic behavior.
 * @author Petr Hosek
 */
trait DynamicCommentFactory extends CommentFactory { thisFactory: DynamicModelFactory with CommentFactory =>

  val global: Global
  import global.reporter

  /** Empty comment. */
  protected[comment] val empty = createComment()

  override def comment(sym: global.Symbol, inTpl: => DocTemplateImpl) = {
    val cmt = super.comment(sym, inTpl)
    latest(sym, inTpl) match {
      case Some(c) => Some(parse(sym, inTpl, c.comment.is))
      case None => Some(new CommentProxy(cmt.getOrElse(empty))(sym, inTpl))
    }
  }

  /**
   * Parse documentation string and return comment.
   * @param sym documented symbol
   * @param inTpl parent template
   * @param docStr documentation string
   * @return parsed comment
   */
  private def parse(sym: global.Symbol, inTpl: => DocTemplateImpl, docStr: String): Comment = {
    val str = global.expandedDocComment(sym, inTpl.sym, docStr).trim
    new CommentProxy(parse(str, docStr, NoPosition))(sym, inTpl)
  }

  /**
   * Obtain latest comment for given symbol.
   * @param sym documented symbol
   * @param inTpl parent template
   * @return latest comment if any exists
   */
  private def latest(sym: global.Symbol, inTpl: => DocTemplateImpl) = makeMember(sym, inTpl) match {
    case List(mbr: MemberEntity, _*) => model.mapper.Comment.latest(mbr.uniqueName)
    case Nil => None
  }

  /**
   * Updatable coment proxy.
   * @param cmt proxied comment
   */
  protected[comment] class CommentProxy(var cmt: Comment)(val sym: global.Symbol, val inTpl: DocTemplateImpl) extends Comment {
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
    def source = cmt.source
    def constructor = cmt.constructor
    override def equals(other: Any) = other match {
      case that: CommentProxy => cmt.equals(that.cmt)
      case _ => cmt.equals(other)
    }
    override def hashCode = cmt.hashCode
  }

  /** Transforms comment into extended comment providing dynamic behavior. */
  implicit def comments(cmt: Comment) = new CommentExtensions(cmt)

  /**
   * Extended comment class providing dynamic behavior.
   * @param cmt extended comment
   */
  class CommentExtensions(cmt: Comment) {

    /**
     * Update member entity comment with given documentation string.
     * @param mbr member entity
     * @param docStr documentation string
     */
    def update(docStr: String) = cmt match {
      case prx: CommentProxy =>
        val str = global.expandedDocComment(prx.sym, prx.inTpl.sym, docStr).trim
        val cmt = parse(str, docStr, NoPosition)
        if (!reporter.hasWarnings) prx.cmt = cmt
      case _ =>
    }

    /**
     * Whether the comment has been updated.
     * @return true if comment has been already update
     */
    def isUpdated(): Boolean = cmt match {
      case prx: CommentProxy => latest(prx.sym, prx.inTpl).isDefined
      case _ => false
    }

    /**
     * Retrieve original comment as defined in source code.
     * @return original comment if defined
     */
    def original(): Option[Comment] = cmt match {
      case prx: CommentProxy =>
        val c = DynamicCommentFactory.super.comment(prx.sym, prx.inTpl)
        Some(new CommentProxy(c.getOrElse(empty))(prx.sym, prx.inTpl))
      case _ => None
    }

  }

}

}
}
}