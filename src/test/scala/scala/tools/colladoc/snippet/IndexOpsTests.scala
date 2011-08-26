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

import net.liftweb.http.{S, LiftSession}
import org.specs.specification.Examples
import net.liftweb.common.Empty
import net.liftweb.util.{TimeHelpers, StringHelpers}
import org.specs.SpecificationWithJUnit
import tools.nsc.doc.doclet.{Indexer, Universer}
import tools.nsc.doc.Universe
import org.specs.mock.JMocker
import tools.nsc.doc.model.{DocTemplateEntity, Package}


object IndexOpsTests extends SpecificationWithJUnit with JMocker {
  val session = new LiftSession("", StringHelpers.randomString(20), Empty)
  val stableTime = TimeHelpers.now

    override def executeExpectations(ex: Examples, t: =>Any): Any = {
      S.initIfUninitted(session) {
        super.executeExpectations(ex, t)
      }
    }

  "IndexOps Snippet" should {
    "Put the filter in the node" in {
      val mockUniverser = mock[Universer]
      val mockUniverse = mock[Universe]
      val mockRootPackage = mock[Package]
      val mockIndexer = mock[Indexer]
      expect {
        exactly(1).of(mockUniverser).universe willReturn mockUniverse
        exactly(1).of(mockIndexer).index
        exactly(1).of(mockUniverse).rootPackage willReturn mockRootPackage
        exactly(1).of(mockRootPackage).isRootPackage willReturn true
        exactly(1).of(mockRootPackage).templates willReturn List[DocTemplateEntity]()
        exactly(1).of(mockRootPackage).packages willReturn List[Package]()
      }

      val index = new IndexOps(mockUniverser, mockIndexer)

      val str = index.body(<html></html>).toString()

      str.indexOf((<div id="filter"></div>).toString()) must be >= 0
    }
  }
}