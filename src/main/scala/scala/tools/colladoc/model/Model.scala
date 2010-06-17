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
package scala.tools.colladoc.model

import tools.nsc.Global
import tools.nsc.doc.{SourcelessComments, Universe, Settings}
import tools.nsc.doc.model.ModelFactory
import tools.nsc.reporters.{ConsoleReporter, Reporter}
import net.liftweb.common.Logger
import tools.colladoc.lib.ColladocSettings
import java.io.File
import tools.nsc.doc.model.comment.{Comment, CommentFactory}
import tools.nsc.util.NoPosition

object Model {

  val settings = new Settings(error) { classpath.value = ColladocSettings.getClassPath }
  val reporter = new ConsoleReporter(settings)

  val factory = new DocFactory(reporter, settings)
  lazy val model = factory construct (ColladocSettings.getSources)

  private def init() {
    List(model)
  }
  init()

  class DocFactory(val reporter: Reporter, val settings: Settings) {
    /** The unique compiler instance used by this processor and constructed from its `settings`. */
    object compiler extends Global(settings, reporter) {
      override protected def computeInternalPhases() {
        phasesSet += syntaxAnalyzer
        phasesSet += analyzer.namerFactory
        phasesSet += analyzer.packageObjects
        phasesSet += analyzer.typerFactory
        phasesSet += superAccessors
        phasesSet += pickler
        phasesSet += refchecks
      }
      override def onlyPresentation = true
      lazy val addSourceless = {
        val sourceless = new SourcelessComments { val global = compiler }
        docComments ++= sourceless.comments
      }
    }

    val modelFactory = new ModelFactory(compiler, settings) with CommentFactory {
      def parse(comment: String): Comment = parse(comment, NoPosition)
    }

    def construct(files: List[String]) = {
      (new compiler.Run()) compile files
      compiler.addSourceless

      modelFactory.makeModel
    }
  }

  def error(msg: String): Unit = Logger("Model").error(msg)

}