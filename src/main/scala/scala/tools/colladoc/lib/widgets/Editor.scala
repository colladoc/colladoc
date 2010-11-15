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
import net.liftweb.http.{LiftRules, S}
import net.liftweb.http.js.JE._

import xml.{NodeSeq, Unparsed}

/**
 * Provides support and integration for jQuery editor widget.
 * @author Petr Hosek
 */
object Editor {

  def apply(value: String, parse: String => NodeSeq, func: String => Any, attrs: (String, String)*) =
    new Editor().render(value, parse, func, attrs:_*)

  def editorObj(value: String, parse: String => NodeSeq, func: String => Any, attrs: (String, String)*) =
    new Editor().editorObj(value, parse, func, attrs:_*)

}

/**
 * Provides support and integration for jQuery editor widget.
 * @author Petr Hosek
 */
class Editor {

  /**
   * Render a text area with editor support.
   * @param value initial content
   * @param parse the function to call to parse the content
   * @param func the function to be called when content is changed
   * @param attrs the attributes that can be added to text area
   */
  def render(value: String, parse: String => NodeSeq, func: String => Any, attrs: (String, String)*) = {
    fmapFunc(SFuncHolder(func)) { funcName =>
      val what = encodeURL(S.contextPath + "/" + LiftRules.ajaxPath + "?" + funcName + "=foo")
      val input = nextFuncName
      fmapFunc(SFuncHolder(func)) { funcName =>
        val onLoad = JsRaw("""jQuery(document).ready(function(){
            jQuery("#""" + input + """").markItUp(markItUpSettings);
            jQuery.markItUp({ target:"#""" + input + """", previewParserPath: """ + what.encJs + """ } );
          });""")

        <span>
          <head>
            <script type="text/javascript">{ Unparsed(onLoad.toJsCmd) }</script>
          </head>
          { (attrs.foldLeft(<textarea id={ input }>{ value }</textarea>)(_ % _)) %
                ("onchange" -> SHtml.makeAjaxCall(JsRaw("'" + funcName + "=' + encodeURIComponent(this.value)")))
          }
        </span>
      }
    }
  }

  /**
   * Render a text area with editor support.
   * @param value initial content
   * @param parse the function to call to parse the content
   * @param func the function to be called when content is changed
   * @param attrs the attributes that can be added to text area
   */
  def editorObj(value: String, parse: String => NodeSeq, func: String => Any, attrs: (String, String)*) = {
    val f = (ignore: String) => {
      val dta = S.param("data").openOr("")
      PlainTextResponse(parse(dta).toString)
    }
    fmapFunc(SFuncHolder(f)) { fName =>
      val what = encodeURL(S.contextPath + "/" + LiftRules.ajaxPath + "?" + fName + "=foo")
      val input = nextFuncName
      fmapFunc(SFuncHolder(func)) { funcName =>
        val onLoad = JsRaw("""jQuery("#""" + input + """").markItUp(
            jQuery.extend({previewParserPath: """ + what.encJs + """ }, markItUpSettings)
          );""")

        val element = (attrs.foldLeft(<textarea id={ input } name={ funcName }>{ value }</textarea>)(_ % _)) %
                ("onchange" -> SHtml.makeAjaxCall(JsRaw("'" + fName + "=' + encodeURIComponent(this.value)")))

        (element, onLoad)
      }
    }
  }
  
}

}
}
}