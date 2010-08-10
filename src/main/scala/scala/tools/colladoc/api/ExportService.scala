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
package api {

import model.{Comment, Model}
import model.comment.DynamicModelFactory
import model.Model.factory._
import lib.Helpers._

import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import net.liftweb.http.{S, Req, GetRequest}
import net.liftweb.http.rest.RestHelper
import net.liftweb.mapper.By

import collection.mutable.HashSet
import tools.nsc.doc.model._
import xml.{Node, NodeSeq, Null}

object ExportService extends RestHelper {

  serve {
    case r @ Req(path, "xml", GetRequest) => changeSet(path, r)
  }

  def changeSet(path: List[String], req: Req) = {
    val rec = req.param("rec").getOrElse(false.toString) match {
      case AsBoolean(true) => true
      case _ => false
    }
    val rev = req.param("rev").getOrElse(Long.MinValue.toString) match {
      case AsLong(long) => long
      case _ => Long.MaxValue
    }
    <scaladoc>
      { new Traversal(rec, rev) construct(Model.model.rootPackage, path) }
    </scaladoc>
  }

  class Traversal(recursive: Boolean, revision: Long) {

    protected val visited = HashSet.empty[MemberEntity]

    def construct(pack: Package, path: List[String]): NodeSeq = {
      val mbr = pathToMember(pack, path)
      Comment.find(By(Comment.qualifiedName, mbr.qualifiedIdentifier), By(Comment.dateTime, time(revision))) match {
        case Full(c) =>
          val cmt = Model.factory.parse(mbr.symbol.get, mbr.template.get, c.comment.is)
          construct(DynamicModelFactory.createMember(mbr, cmt, c))
        case _ =>
          construct(mbr)
      }
    }

    def construct(mbr: MemberEntity): NodeSeq =
      <xml:group>
        { if (!visited.contains(mbr)) {
            visited += mbr
            mbr match {
              case tpl: DocTemplateEntity => processTemplate(tpl)
              case _ => processMember(mbr)
            }
          }
        }
      </xml:group>

    protected def processTemplate(tpl: DocTemplateEntity): NodeSeq =
      <xml:group>
        { if (tpl.isUpdated) {
            <item>
              <type>{ tpl match {
                case _ if tpl.isPackage => "package"
                case _ if tpl.isTrait => "trait"
                case _ if tpl.isClass => "class"
                case _ if tpl.isObject => "object"
              }}</type>
              <filename>{ entityToFileName(tpl) }</filename>
              <identifier>{ tpl.qualifiedIdentifier }</identifier>
              <newcomment>{ tpl.comment.get.source.get }</newcomment>
            </item>
          }
        }
        { (tpl.values ++ tpl.abstractTypes ++ tpl.methods) map { processMember(_) } }
        { tpl.members collect { case t: DocTemplateEntity if !t.isPackage || recursive => t } map { construct(_) } }
      </xml:group>

    protected def processMember(mbr: MemberEntity): Node =
      <xml:group>
        { if ((mbr.inheritedFrom.isEmpty || mbr.inheritedFrom.contains(mbr.inTemplate)) && mbr.isUpdated) {
            <item>
              <type>{ mbr match {
                case _ if mbr.isDef || mbr.isVal || mbr.isVar => "value"
                case _ if mbr.isAbstractType || mbr.isAliasType => "type"
              }}</type>
              <filename>{ entityToFileName(mbr) }</filename>
              <identifier>{ mbr.qualifiedIdentifier }</identifier>
              <newcomment>{ mbr.comment.get.source.get }</newcomment>
            </item>
          }
        }
      </xml:group>

    protected def entityToFileName(mbr: MemberEntity) = {
      mbr.symbol match {
        case Some(sym) if sym.sourceFile != null =>
          val path = sym.sourceFile.path.stripPrefix(Model.settings.sourcepath.value)
          if (path.startsWith("/")) path.stripPrefix("/")
          else path
        case _ => ""
      }
    }

  }
}

}
}