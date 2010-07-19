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
package lib

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{Req, GetRequest}
import reflect.NameTransformer
import tools.colladoc.model.Model
import tools.colladoc.model.Model.factory._
import tools.nsc.doc.model._
import xml.NodeSeq

object WebService extends RestHelper {

  serve {
    case Req(path, "xml", GetRequest) => changeSet(path)
  }

  def changeSet(path: List[String]) =
    <scaladoc>
      { processEntity(pathToEntity(Model.model.rootPackage, path.toList)) }
    </scaladoc>

  def processEntity(mbr: MemberEntity): NodeSeq = mbr match {
      case p: Package => processPackage(p)
      case t: DocTemplateEntity => processClass(t)
      case f: Def => processMethod(f)
      case _ => <xml:group></xml:group>
    }

  def processPackage(pack: Package) =
    <xml:group>
      { pack.members collect { case t: DocTemplateEntity => t } map { processEntity(_) } }
    </xml:group>

  def processClass(tpl: DocTemplateEntity) =
    if (tpl.isUpdated)
      <xml:group>
        <item>
          <type>{ tpl match {
            case t if t.isTrait => "trait"
            case t if t.isClass => "class"
            case t if t.isObject => "object"
          }}</type>
          <filename>{ fileName(tpl) }</filename>
          <identifier>{ tpl.qualifiedName }</identifier>
          <newcomment>{ tpl.comment.get.source.get }</newcomment>
        </item>
        { tpl.methods map (processMethod(_)) }
      </xml:group>
    else <xml:group></xml:group>

  def processMethod(fnc: Def) =
    if (fnc.isUpdated && (fnc.inheritedFrom.isEmpty || fnc.inheritedFrom.contains(fnc.inTemplate))) {
      def identifier(fnc: Def) = {
        def params(vlss: List[ValueParam]): String = vlss match {
          case Nil => ""
          case vl :: Nil => vl.resultType.name
          case vl :: vls => vl.resultType.name + ", " + params(vls)
        }
        fnc.inTemplate.qualifiedName + "." + fnc.name + fnc.valueParams.map{ "(" + params(_) + ")" }.mkString
      }
      <item>
        <type>method</type>
        <filename>{ fileName(fnc) }</filename>
        <identifier>{ identifier(fnc) }</identifier>
        <newcomment>{ fnc.comment.get.source.get }</newcomment>
      </item>
    } else <xml:group></xml:group>

  def fileName(mbr: MemberEntity) = {
    mbr.symbol match {
      case Some(sym) if sym.sourceFile != null =>
        val path = sym.sourceFile.path.stripPrefix(Model.settings.sourcepath.value)
        if (path.startsWith("/")) path.stripPrefix("/")
        else path
      case _ => ""
    }
  }

  private def pathToEntity(rootPack: Package, path: List[String]): MemberEntity = {
    def doName(mbr: MemberEntity): String =
      NameTransformer.encode(mbr.name) + (mbr match {
        case t: DocTemplateEntity if t.isObject => "$"
        case _ => ""
      })
    def downPacks(pack: Package, path: List[String]): (Package, List[String]) = {
      pack.packages.find{ _.name == path.head } match {
        case Some(p) => downPacks(p, path.tail)
        case None => (pack, path)
      }
    }
    def downInner(tpl: DocTemplateEntity, path: List[String]): MemberEntity = {
      if (!(path isEmpty))
        tpl.members.find{ doName(_) == path.head } match {
          case Some(t: DocTemplateEntity) => downInner(t, path.tail)
          case Some(m: MemberEntity) => m
          case None => tpl
        }
      else
        tpl
    }
    downPacks(rootPack, path) match {
      case (pack, "package" :: Nil) => pack
      case (pack, path) => downInner(pack, path)
    }
  }

}