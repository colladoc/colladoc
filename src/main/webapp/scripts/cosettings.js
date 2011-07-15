/*
 * Copyright (c) 2011, Sergey Ignatov. All rights reserved.
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
  $("#settings_tab").tabs();

  $("#userlist").jqGrid({
    url:'grid/users?',
    datatype: "xml",
    colNames:['Username', 'Superuser', ''],
    colModel:[
      {name: 'profile', index: 'profile'},
      {name: 'superuser', index: 'superuser', width: 60},
      {name: 'delete', index: 'delete', width: 18}
    ],
    rowList:[5,10,20,30],
    pager: '#userpager',
    viewrecords: true,
    sortname: 'username',
    sortorder: 'desc',
    autowidth: true,
    height: 500,
    caption: 'User list'
  }).navGrid('#userpager', {edit:false,add:false,del:false});
  $('.ui-jqgrid-titlebar-close').remove();

  $(".create").dialog({
    autoOpen: false,
    title: 'Create new user',
    buttons: {
      'Save': function() {
        if ($(".create").valid()) {
          $(".create").submit();
          setTimeout(function() {
            $("#userlist").trigger('reloadGrid', [
              {page: 1}
            ]);
          }, 2000);
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

  $(".create").validate();
});
