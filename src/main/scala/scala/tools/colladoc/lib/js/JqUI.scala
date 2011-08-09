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
import net.liftweb.http.js.JE.{Call, Str}

/**
 * Various jQuery UI commands.
 * @author Petr Hosek
 */
object JqUI {

  /** Convert selected element to button. */
  case class Button() extends JsExp with JsMember {
    override def toJsCmd = "button()"
  }

  /** Convert selected element to select menu. */
  case class SelectMenu(width: Int = 225) extends JsExp with JsMember {
    override def toJsCmd = "selectmenu({ width: " + width.toString + " })"
  }

  /** Dialog command. */
  object DialogOperation extends Enumeration("open", "close", "destroy") {
    type Operation = Value
    val open, close, destroy = Value
  }

  /** Invoke selected dialog with command. */
  case class Dialog(op: DialogOperation.Operation) extends JsExp with JsMember {
    override def toJsCmd = "dialog(" + cmd.toString.encJs + ")"
  }

  /** Open modal dialog. */
  case class OpenDialog() extends JsExp with JsMember {
    override def toJsCmd = "dialog('open')"
  }

  /** Close modal dialog. */
  case class CloseDialog() extends JsExp with JsMember {
    override def toJsCmd = "dialog('close')"
  }

  /** Notification type. */
  object NotificationType extends Enumeration("notice", "error") {
    type Type = Value
    val notice, error = Value
  }

  /** Show notification. */
  case class Notify(_type: NotificationType.Type, text: String, title: String = "", hide: Boolean = true) extends JsCmd {
    override def toJsCmd = "jQuery.notify({ text: " + text.encJs + ", type: " + _type.toString.encJs +
            (if (title.nonEmpty) ", title: " + title.encJs else "") + ", hide: " + hide.toString + " });"
  }

  /** Submit form. */
  case class SubmitForm(jqId: String) extends JsCmd with JsMember {
    override def toJsCmd = "$('" + jqId +"').submit();"
  }

  /** Submit form with validation. */
  case class SubmitFormWithValidation(jqId: String) extends JsCmd with JsMember {
    override def toJsCmd = "if ($('" + jqId +"').valid()) " + "$('" + jqId + "').submit();"
  }

  /** Confirm dialog. */
  def ColladocConfirm(what: String) = Call("confirm", Str(what))

  /** Run prettyDate function. */
  case object PrettyDate extends JsExp with JsMember {
    override def toJsCmd = "prettyDate();"
  }
}

}
}
}