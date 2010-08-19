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
package util {

import NameUtils._

import net.liftweb.util.Helpers._

import reflect.NameTransformer
import tools.nsc.doc.model.{DocTemplateEntity, MemberEntity, Package}

/**
 * Provides utility functions related to model entity paths.
 * @author Petr Hosek
 */
object PathUtils {

  /**
   * Get entity name.
   * @param mbr model entity
   * @return entity name
   */
  private def name(mbr: MemberEntity): String = mbr match {
    case tpl: DocTemplateEntity => NameTransformer.encode(tpl.name) + (if (tpl.isObject) "$" else "")
    case mbr: MemberEntity => urlEncode(mbr.identifier)
  }

  /**
   * Convert member entity to fully qualified path.
   * @param mbr member entity
   * @param isSelf whether the entity is defining
   * @return fully qualified path
   */
  def memberToPath(mbr: MemberEntity, isSelf: Boolean) = {
    def innerPath(nme: String, mbr: MemberEntity): String =
      mbr.inTemplate match {
        case inPkg: Package => nme
        case inTpl: DocTemplateEntity if mbr.isTemplate => innerPath(name(inTpl) + "$" + nme, inTpl)
        case inTpl: DocTemplateEntity if !mbr.isTemplate => innerPath(name(inTpl) + "/" + nme, inTpl)
      }
    mbr match {
      case tpl: DocTemplateEntity if tpl.isPackage => (if (!isSelf) name(tpl) + "/" else "") + "package"
      case mbr: MemberEntity => innerPath(name(mbr), mbr)
    }
  }

  /**
   * Traverse model from root package and find member entity accoding to its path.
   * @param rootPack model root package
   * @param path fully qualified entity path
   * @return member entity with name if found or nothiing
   */
  def pathToMember(rootPack: Package, path: List[String]): MemberEntity = {
    def downPacks(pack: Package, path: List[String]): (Package, List[String]) = {
      pack.packages.find{ _.name == path.head } match {
        case Some(p) => downPacks(p, path.tail)
        case None => (pack, path)
      }
    }
    def downInner(tpl: DocTemplateEntity, path: List[String]): MemberEntity = path match {
      case p :: r if p.isEmpty => downInner(tpl, r)
      case p :: r =>
        tpl.members.sortBy{ t => -1 * name(t).length }.find{ t => p.startsWith(name(t)) } match {
          case Some(t: DocTemplateEntity) => downInner(t, p.stripPrefix(name(t)).stripPrefix("$") :: r)
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

  /**
   * Convert template entity to fully qualified path.
   * @param tpl template entity
   * @param isSelf whether the entity is defining
   * @return fully qualified path
   */
  def templateToPath(tpl: DocTemplateEntity, isSelf: Boolean) =
    memberToPath(tpl, isSelf)

  /**
   * Traverse model from root package and find teamplate entity accoding to its path.
   * @param rootPack model root package
   * @param path fully qualified entity path
   * @return template entity with name if found or nothiing
   */
  def pathToTemplate(rootPack: Package, path: List[String]): DocTemplateEntity =
    pathToMember(rootPack, path) match {
      case tpl: DocTemplateEntity => tpl
      case mbr: MemberEntity => mbr.inTemplate
    }

}

}
}
}