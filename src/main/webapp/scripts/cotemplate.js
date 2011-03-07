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

$(document).ready(function() {
    reinit('body');

    var url= $(location).attr("href");
    var urlpart=url.split("q=");
    var src=(parent.location.href + '#q='+ urlpart[1]).replace(" ","+");

    if ((urlpart[1] !="") && (typeof(urlpart[1]) !="undefined")) {localSearchHighlight(urlpart[1]); copy(src);}

    shortcut_enable();
})

function shortcut_enable(){
    var isCtrl=false;
    $(document).keyup(function (e) {
        if(e.which == 17) isCtrl=false;
    }).keydown(function (e) {
        if(e.which == 17) isCtrl=true;
        if(e.which == 81 && isCtrl == true) {

            var elem = parent.window.document.getElementById("svalue");
            elem.focus();
            elem.select();
            isCtrl = false;
            return false;
       }
    });
}

function reinit(selector) {
    $('.button', $(selector)).button();
    $('.select', $(selector)).selectmenu({ width: 300 });
    $('.menu', $(selector)).each(function() {
        $(this).next().menu({ input: $(this) }).hide();
    }).click(function(event) {
        var menu = $(this).next();
        if (menu.is(":visible")) {
            menu.hide();
            return false;
        }
        menu.menu("deactivate").show().css({top: 0, left: 0}).position({
            my: "left top",
            at: "right top",
            of: this
        });
        $(document).one("click", function() {
            menu.hide();
        });
        return false;
    });
};





function copy(txt) {

                var clip = new ZeroClipboard.Client();
                //Glue the clipboard client to the link
                clip.glue('linkURL');

                //Grab the text from the url

                clip.addEventListener('mouseup',function() {
                    alert("Copied to clipboard text:\n\n" + txt);
                    clip.setText(txt);

                });
            };

