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

import net.liftweb.http._
import model.mapper.{CommentToString, Comment}
import model.{SearchIndex, Model}
import net.liftweb.util.Props
  
import tools.nsc.doc.Universe
import tools.nsc.doc.doclet.{Indexer, Universer, Generator}
import tools.nsc.doc.html.Doclet
import tools.nsc.doc.model.IndexModelFactory

/**
 * Factory providing various dependencies.
 * @author Petr Hosek
 */
object DependencyFactory extends Factory {
  implicit object doclet extends FactoryMaker(getDoclet _)
  implicit object model extends FactoryMaker(getModel _)
  implicit object index extends FactoryMaker(getIndex _)
  implicit object path extends FactoryMaker(getPath _)
  implicit object props extends FactoryMaker(getProps _)
  implicit object commentMapper extends FactoryMaker[CommentToString](getComment _)

  private def getComment = Comment

  private def getPath =
    S.param("path") openOr "" split('/')

  private def getProps =
    Props.props

  private lazy val getDoclet = {
    val doclet = new Doclet

    doclet match {
      case universer: Universer =>
        universer setUniverse getModel
        doclet match {
          case indexer: Indexer => indexer setIndex IndexModelFactory.makeIndex(getModel)
          case _ => ()
        }
      case _ => ()
    }

    doclet
  }

  // Note: Lazy eval is necessary here to give us an opportunity to do DI of
  // dependencies required by the model and the index.
  private lazy val getModel: Universe = {
    // Make sure that we index the model when it is created.
    getIndex

    Model.model.get // TODO: use safe solution
  }

  private lazy val getIndex =
    new SearchIndex(Model.model.get.rootPackage, getComment) // TODO: use safe solution
}
}

}
