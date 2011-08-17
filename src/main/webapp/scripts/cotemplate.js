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

    var url = location.href;
    var urlpart=url.split("q=");
    var src=(parent.location.href + '#q='+ urlpart[1]).replace(" ","+");

    if ((urlpart[1] !="") && (typeof(urlpart[1]) !="undefined")) {localSearchHighlight(urlpart[1]); copy(src);}

    shortcut_enable();
});

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
    $('.select', $(selector)).selectmenu({ width: 250 });
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
    $('linkUrl').setAttribute('href', txt);
};

$.extend({
    /**
     * Create DialogBox by ID
     * @param {String} id elementID
     */
    getOrCreateDialog: function(id) {
      $box = $('#' + id);
      if (!$box.length) {
        $box = $('<div id="' + id + '"><p></p></div>').hide().appendTo('body');
      }
      return $box;
    }
  });

/**
 * Override javascript confirm() and wrap it into a jQuery-UI Dialog box
 * @depends $.getOrCreateDialog
 * @param {String} message the alert message
 * @param {String/Object} callback the confirm callback
 * @param {Object} options jQuery Dialog box options
 */
function confirm(message, callback, options) {
  var defaults = {
    modal: true,
    resizable: false,
    buttons: {
      Ok: function() {
        $(this).dialog('close');
        return (typeof callback == 'string') ?
          window.location.href = callback :
          callback();
      },
      Cancel: function() {
        $(this).dialog('close');
        return false;
      }
    },
    minHeight: 50,
    dialogClass: 'modal-shadow'
  };

  $confirm = $.getOrCreateDialog('colladoc_confirm');
  // set message
  $("p", $confirm).html(message);
  // init dialog
  $confirm.dialog($.extend({}, defaults, options));
}

function prettyDate() {
  if (jQuery.prettyDate)
    $(".datetime").prettyDate();
}

$(document).ready(function() {
  prettyDate();
});

$(document).ready(function() {
  $('#discussions_header').live('click', function(){
    $('#discussions_wrapper').slideToggle(100);
  });
});

$(document).ready(function() {
  $("img[src$='lib/case class.png']").attr("src", "lib/class.png");
});