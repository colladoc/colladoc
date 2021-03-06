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

import tools.nsc.doc.model._

/**
 * Provides utility functions related to model entity naming.
 * @author Petr Hosek
 */
object NameUtils  {

  /** Transforms entity to a class providing various naming functions */
  implicit def names(mbr: MemberEntity) = new NameExtensions(mbr)

  /**
   * Get entity name.
   * @param mbr model entity
   * @return entity name
   */
  private def name(mbr: MemberEntity): String = mbr match {
    case tpl: DocTemplateEntity => tpl.name + (if (tpl.isObject) "#" else "")
    case mbr: MemberEntity => mbr.identifier
  }

  /**
   * Provides various entity naming functions.
   */
  class NameExtensions(mbr: MemberEntity) {

    /**
     * Get identifier.
     * @return entity identifier
     */
    def identifier(): String = mbr match {
      case fnc: Def =>
        def params(vlss: List[ValueParam]): String = vlss match {
          case Nil => ""
          case vl :: Nil => vl.resultType.name
          case vl :: vls => vl.resultType.name + ", " + params(vls)
        }
        fnc.name + fnc.valueParams.map{ "(" + params(_) + ")" }.mkString
      case _ => mbr.name
    }

    /**
     * Get unique, fully qualified name.
     * @return entity unique name
     */
    def uniqueName(): String = {
      def innerName(nme: String, tpl: DocTemplateEntity): String = tpl.inTemplate match {
        case inPkg: Package if inPkg.isRootPackage => nme
        case inTpl: DocTemplateEntity => innerName(name(inTpl) + "." + nme, inTpl)
      }
      mbr match {
        case tpl: DocTemplateEntity => innerName(name(tpl), tpl)
        case mbr: MemberEntity => innerName(name(mbr.inTemplate), mbr.inTemplate) + "#" + identifier
      }
    }
    
  }

  /**
   * Traverse model from root package and find entity accoding to its name.
   * @param rootPack model root package
   * @param nme entity unique name
   * @return entity with name if found or nothiing 
   */
  def nameToMember(rootPack: Package, nme: String): Option[MemberEntity] = {
    def downPacks(pack: Package, path: List[String]): (Package, List[String]) =
      pack.packages.find{ _.name == path.head } match {
        case Some(p) => downPacks(p, path.tail)
        case None => (pack, path)
      }
    def downInner(tpl: DocTemplateEntity, path: List[String]): Option[MemberEntity] = path match {
      case p :: r if p.isEmpty => downInner(tpl, r)
      case p :: r => tpl.members.sortBy(t => -1 * name(t).length).find(t => p.startsWith(name(t))) match {
          case Some(t: DocTemplateEntity) => downInner(t, p.stripPrefix(name(t)).stripPrefix("#") :: r)
          case Some(m: MemberEntity) => Some(m)
          case None => None
        }
      case Nil => Some(tpl)
    }
    downPacks(rootPack, nme split("""[.]""") toList) match {
      case (pack, Nil) => Some(pack)
      case (pack, path) => downInner(pack, path)
    }
  }

}

}
}
}