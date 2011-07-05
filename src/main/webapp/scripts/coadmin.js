$(document).ready(function() {
  $("#admin_tabs").tabs();

  $("#userlist").jqGrid({
    url:'grid/users?',
    datatype: "xml",
    colNames:['Username', 'Email', 'OpenID', 'Superuser', ''],
    colModel:[
      {name:'name',index:'name'},
      {name:'email',index:'email'},
      {name:'openid',index:'openid', width:350},
      {name:'superuser',index:'superuser', width: 60},
      {name:'delete', index:'delete', width: 18}
    ],
    rowList:[5,10,20,30],
    pager: '#userpager',
    viewrecords: true,
    sortname: 'username',
    sortorder: 'desc',
    autowidth: true,
    height: "100%",
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
