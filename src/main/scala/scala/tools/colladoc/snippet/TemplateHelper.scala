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
package scala.tools.colladoc.snippet

import xml.NodeSeq
import net.liftweb.http.S
import tools.colladoc.model.Model
import tools.nsc.doc.html.page.Template
import tools.nsc.doc.model.{Package, DocTemplateEntity}
import tools.nsc.io.File
import reflect.NameTransformer
import java.io.{ File => JFile }
import tools.colladoc.lib.DependencyFactory

class TemplateHelper {

  def body(xhtml: NodeSeq): NodeSeq = {
    def findTemplate(tpl: DocTemplateEntity, arr: Array[String]): DocTemplateEntity =
      if (!(arr isEmpty))
        findTemplate(tpl.templates.find{ _.name == arr.head }.get, arr.tail)
      else
        tpl

    val path = S.param("path") openOr "" split('/')
    val template = new Template(pathToTemplate(Model.model.rootPackage, path.toList)) {
      override def resourceToPath(resName: String): List[String] = {
        new File(new JFile(resName)).extension match {
          case "png" => resName :: "images" :: Nil
          case "js" => resName :: "scripts" :: Nil
          case "css" => resName :: Nil
        }
      }
    }

    template.body
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