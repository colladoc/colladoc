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
package model {

import comment.{DynamicModelFactory, DynamicCommentFactory}
import tools.nsc.reporters.AbstractReporter
import tools.nsc.util.Position

import net.liftweb.common.Logger
import net.liftweb.util.Props
import net.liftweb.http.S

import tools.nsc.Global
import tools.nsc.doc.{SourcelessComments, Settings}
import tools.nsc.io.Directory

import java.io.File
import tools.nsc.doc.model.{TreeFactory, ModelFactory}
import tools.nsc.interactive.RangePositions

/**
 * Documentation model.
 * @author Petr Hosek
 */
object Model extends Logger {

  /** Compiler settings. */
  object settings extends Settings(msg => error(msg)) {
    processArguments((Props.props.flatMap {
      case (k, v) if k.startsWith("-") => if (!v.isEmpty) List(k, v) else List(k)
      case (_, _) => Nil
    }) toList, false)
  }

  /** Compiler warnings and errors reporter. */
  object reporter extends AbstractReporter {
    val settings = Model.settings

    def display(pos: Position, msg: String, severity: Severity) = {
      severity.count += 1
      severity match {
        case INFO => S.notice(msg)
        case WARNING => S.warning(msg)
        case ERROR => S.error(msg)
      }
    }

    def displayPrompt = S.error("There was an error while processing comment")

    override def hasErrors = false // need to do this so that the Global instance doesn't trash all the symbols just because there was an error
  }

  /** The unique compiler instance used by this processor and constructed from its `settings`. */
  object compiler extends Global(settings, reporter) with RangePositions {
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

  /** Model factory used to construct the model. */
  object factory extends ModelFactory(compiler, settings) with DynamicModelFactory with DynamicCommentFactory with TreeFactory {
    def construct(files: List[String]) = {
      (new compiler.Run()) compile files
      compiler.addSourceless

      makeModel
    }
  }

  lazy val model = factory construct (getSources)

  /**
   * Get list of sources located in sourcepath.
   * @return list of source files
   */
  private def getSources: List[String] =
    settings.sourcepath.value.split(File.pathSeparatorChar).flatMap{ p => getSources(new File(p)) }.toList

  /**
   * Get list of sources located in directory `file`..
   * @param file directory to look source files for
   * @return list of source files
   */
  private def getSources(file: File): List[String] =
    (new Directory(file)).deepFiles.filter{ _.extension == "scala" }.map{ _.path }.toList

  /** Initialize model. */
  def init() {
    List(model)

  }

}

}
}