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

object WebService extends RestHelper {

  serve {
    case Req(path, "xml", GetRequest) => changeSet(path)
  }

  def changeSet(path: List[String]) = {
    val tpl = pathToTemplate(Model.model.rootPackage, path.toList)
    <scaladoc>
      { processClass(tpl) }
      { tpl.methods map (processMethod(_)) }
    </scaladoc>
  }

  def processClass(tpl: DocTemplateEntity) =
    <item>
      <type>{ if (tpl.isClass) "class" else if (tpl.isObject) "object" }</type>
      <filename>{ fileName(tpl) }</filename>
      <identifier>{ tpl.qualifiedName }</identifier>
      <newcomment>{ tpl.comment.get.source getOrElse "" }</newcomment>
    </item>

  def processMethod(fnc: Def) =
    if (fnc.inheritedFrom.isEmpty || fnc.inheritedFrom.contains(fnc.inTemplate)) {
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
        <newcomment>{ fnc.comment.get.source getOrElse "" }</newcomment>
      </item>
    } else <xml:group></xml:group>

  def fileName(mbr: MemberEntity) = {
    mbr.symbol() match {
      case Some(sym) if sym.sourceFile != null =>
        val path = sym.sourceFile.path.stripPrefix(ColladocSettings.getSourcePath)
        if (path.startsWith("/")) path.stripPrefix("/")
        else path
      case _ => ""
    }
  }

  private def pathToTemplate(rootPack: Package, path: List[String]): DocTemplateEntity = {
    def doName(tpl: DocTemplateEntity): String =
      NameTransformer.encode(tpl.name) + (if (tpl.isObject) "$" else "")
    def downPacks(pack: Package, path: List[String]): (Package, List[String]) = {
      pack.packages.find{ _.name == path.head } match {
        case Some(p) => downPacks(p, path.tail)
        case None => (pack, path)
      }
    }
    def downInner(tpl: DocTemplateEntity, path: List[String]): DocTemplateEntity = {
      if (!(path isEmpty))
        tpl.templates.find{ doName(_) == path.head } match {
          case Some(t) => downInner(t, path.tail)
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