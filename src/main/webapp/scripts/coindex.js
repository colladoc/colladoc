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
    $.validator.setDefaults({
        highlight: function(input) {
            $(input).addClass("ui-state-error");
        },
        unhighlight: function(input) {
            $(input).removeClass("ui-state-error");
        }
    });
    
    $(".user").dialog({
        autoOpen: false,
        title: 'User Details',
        buttons: {
            'Save': function() {
                if ($(".user").valid()) {
                    $(".user").submit();
                    $(this).dialog('close');
                }
            },
            'Cancel': function() {
                $(this).dialog('close');
            }
        },
        modal: true,
        draggable: false,
        resizable: false
    });
    $(".user").validate();

    var user_form = $($(".login > form")[0]);
    var openid_form = $("#openid_form");

    $(".login").dialog({
        autoOpen: false,
        title: 'User Login',
        buttons: {
            'Login': function() {
                if (!openid_form.is(':visible') && user_form.valid()) {
                    user_form.submit();
                    $(this).dialog('close');
                }
                if (openid_form.is(':visible') && openid_form.valid()) {
                    openid_form.submit();
                    $(this).dialog('close');
                }
            },
            'Cancel': function() {
                $(this).dialog('close');
            }
        },
        modal: true,
        draggable: false,
        resizable: false
    });
  
    user_form.validate();
    openid_form.validate();

    $("#openid_switcher").live("click", function(){
      var username = $("#username");
      var password = $("#password");
      var openid = $("#openid_identifier");
      var openid_link = $("#openid_switcher");

      var is_openid_form_open = openid_form.is(':visible');

      openid_form.toggle();

      if (!is_openid_form_open) {
        username.attr('disabled', 'true');
        password.attr('disabled', 'true');
        openid_link.text("less OpenID");
        openid.val('');
        openid.focus();
      } else {
        username.removeAttr('disabled');
        password.removeAttr('disabled');
        openid_link.text("more OpenID");
        username.focus();
      }
    });

    $(".direct a").live("click", function(){
      var id = $(this).attr('id');
      var link = '';
      switch (id) {
        case 'google':
          link = "https://www.google.com/accounts/o8/id";
          break;
        case 'yahoo':
          link = "http://yahoo.com/";
        break;
      }
      $("#openid_identifier").val(link);
      openid_form.submit();
    });
})

function resizeFilterBlock() {
    $("#filter").css("top", $("#user").outerHeight(true));
    $("#tpl").css("top", $("#user").outerHeight(true) + $("#filter").outerHeight(true));
}