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
package scala.tools.colladoc.lib

import Helpers._

object Helpers extends PathHelpers with StringHelpers with TimeHelpers with XmlHelpers

trait PathHelpers {
  import tools.colladoc.model.Model.factory._
  
  import reflect.NameTransformer
  import tools.nsc.doc.model.{DocTemplateEntity, MemberEntity, Package}

  import net.liftweb.util.Helpers._

  def memberToPath(mbr: MemberEntity, isSelf: Boolean) = {
    def doName(mbr: MemberEntity): String = mbr match {
        case tpl: DocTemplateEntity => NameTransformer.encode(tpl.name) + (if (tpl.isObject) "$" else "")
        case mbr: MemberEntity => urlEncode(mbr.identifier)
      }
    def innerPath(nme: String, mbr: MemberEntity): String =
      mbr.inTemplate match {
        case inPkg: Package => nme
        case inTpl: DocTemplateEntity if mbr.isTemplate => innerPath(doName(inTpl) + "$" + nme, inTpl)
        case inTpl: DocTemplateEntity if !mbr.isTemplate => innerPath(doName(inTpl) + "/" + nme, inTpl)
      }
    mbr match {
      case tpl: DocTemplateEntity if tpl.isPackage => (if (!isSelf) doName(tpl) + "/" else "") + "package"
      case mbr: MemberEntity => innerPath(doName(mbr), mbr)
    }
  }

  def templateToPath(tpl: DocTemplateEntity, isSelf: Boolean) =
    memberToPath(tpl, isSelf)

  def pathToMember(rootPack: Package, path: List[String]): MemberEntity = {
    def doName(mbr: MemberEntity): String = mbr match {
        case tpl: DocTemplateEntity => NameTransformer.encode(tpl.name) + (if (tpl.isObject) "$" else "")
        case mbr: MemberEntity => urlDecode(mbr.identifier)
      }
    def downPacks(pack: Package, path: List[String]): (Package, List[String]) = {
      pack.packages.find{ _.name == path.head } match {
        case Some(p) => downPacks(p, path.tail)
        case None => (pack, path)
      }
    }
    def downInner(tpl: DocTemplateEntity, path: List[String]): MemberEntity = path match {
      case p :: r if p.isEmpty => downInner(tpl, r)
      case p :: r =>
        tpl.members.sortBy{ t => -1 * doName(t).length }.find{ t => p.startsWith(doName(t)) } match {
          case Some(t: DocTemplateEntity) => downInner(t, p.stripPrefix(doName(t)).stripPrefix("$") :: r)
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

  def pathToTemplate(rootPack: Package, path: List[String]): DocTemplateEntity =
    pathToMember(rootPack, path) match {
      case tpl: DocTemplateEntity => tpl
      case mbr: MemberEntity => mbr.inTemplate
    }

}

trait StringHelpers {
  import util.matching.Regex

  import net.liftweb.util.Helpers._

  val unsafeChars = new Regex("""[^A-Za-z0-9_]""")

  def htmlAttributeEncode(in: String) =
    unsafeChars.replaceAllIn(in.encJs, "_")
  
}

trait TimeHelpers {
  import java.util.{Date, Calendar}

  implicit def toDate(c: Calendar) = c.getTime

  implicit def toTime(d: Date) = d.getTime

  implicit def toCalendar(c: Calendar) = new {
    def rollDay(a: Int) = { c.roll(Calendar.DAY_OF_MONTH, a); c }
    def rollMonth(a: Int) = { c.roll(Calendar.MONTH, a); c }
    def rollYear(a: Int) = { c.roll(Calendar.YEAR, a); c }
  }

}

trait XmlHelpers {
  import xml._

  implicit def addAttribute(elem: Elem) = new {
    def %(attrs: Map[String, String]) = {
      val seq = for((n, v) <- attrs) yield new UnprefixedAttribute(n, v, Null)
      (elem /: seq) { _ % _ }
    }
  }

  implicit def addNode(elem: Elem) = new {
    def \+(newChild: Node) = elem match {
      case Elem(prefix, labels, attrs, scope, child @ _*) =>
        Elem(prefix, labels, attrs, scope, child ++ newChild : _*)
    }
  }

  implicit def addNodeSeq(seq: NodeSeq) = new {
    def \\%(attrs: Map[String, String]) = seq theSeq match {
      case Seq(elem: Elem, rest @ _*) =>
        elem % attrs ++ rest
      case elem => elem
    }

    def \\+(newChild: Node) = seq theSeq match {
      case Seq(elem: Elem, rest @ _*) =>
        elem \+ newChild ++ rest
      case elem => elem
    }
  }

}