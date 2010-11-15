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
package widgets {

import net.liftweb.http._
import net.liftweb.http.S._
import net.liftweb.util.Helpers._
import net.liftweb.http.{LiftRules, JsonResponse, S}
import net.liftweb.http.js._
import net.liftweb.http.js.JE._

import xml.Unparsed

/**
 * Provides support and integration for jQuery UI autocomplete widget.
 * @author Petr Hosek
 */
object Autocomplete {

  def apply(value: String, options: (String) => Seq[String], func: String => JsCmd, attrs: (String, String)*) =
    new Autocomplete().render(value, options, func, attrs:_*)

}

/**
 * Provides support and integration for jQuery UI autocomplete widget.
 * @author Petr Hosek
 */
class Autocomplete {

  /**
   * Render a text field with autocomplete support.
   * @param value initial input string
   * @param options the function to be called when user is typing text
   * @param func the function to be called when input string is changed
   * @param attrs the attributes that can be added to input text field
   */
  def render(value: String, options: (String) => Seq[String], func: String => JsCmd, attrs: (String, String)*) = {
    val f = (ignore: String) => {
      val q = S.param("term").openOr("")
      JsonResponse(JsArray(options(q).map(s => Str(s)) : _*))
    }
    fmapFunc(SFuncHolder(f)) { fName =>
      val what = encodeURL(S.contextPath + "/" + LiftRules.ajaxPath + "?" + fName + "=foo")
      val input = nextFuncName
      fmapFunc(SFuncHolder(func)) { funcName =>
        val onLoad = JsRaw("""jQuery(document).ready(function(){
            jQuery("#""" + input + """").autocomplete({ source: """ + what.encJs + """});
          });""")

        <span>
          <head>
            <script type="text/javascript">{ Unparsed(onLoad.toJsCmd) }</script>
          </head>
          { (attrs.foldLeft(<input type="text" id={ input } value={ value } />)(_ % _)) %
              ("onblur" -> SHtml.makeAjaxCall(JsRaw("'" + funcName + "=' + encodeURIComponent(this.value)")))
          }
        </span>
      }
    }
  }

}

}
}
}