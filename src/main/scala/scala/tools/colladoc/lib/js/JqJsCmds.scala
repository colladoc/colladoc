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
package js {

import net.liftweb.util.Helpers._
import net.liftweb.http.js.{JsMember, JsExp, JsCmd}
import net.liftweb.http.js.jquery.{JQueryLeft, JQueryRight}

/**
 * Various jQuery commands.
 * @author Petr Hosek
 */
object JqJsCmds {

  /** Reload selected frame/iframe. */
  case class Reload() extends JsExp with JsMember {
    override def toJsCmd = "each(function() {this.contentWindow.location.reload(true);})"
  }

  /** Add class to selected element. */
  case class AddClass(_class: String) extends JsExp with JsMember {
    override def toJsCmd = "addClass(" + _class.encJs + ")"
  }

  /** Remove class from selected element. */
  case class RemoveClass(_class: String) extends JsExp with JsMember {
    override def toJsCmd = "removeClass(" + _class.encJs + ")"
  }

}

}
}
}