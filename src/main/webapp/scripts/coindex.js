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

    $(".login").dialog({
        autoOpen: false,
        title: 'User Login',
        buttons: {
            'Login': function() {
                if ($(".login").valid()) {
                    $(".login").submit();
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
    $(".login").validate();
})

function resizeFilterBlock() {
    $("#filter").css("top", $("#user").outerHeight(true));
    $("#tpl").css("top", $("#user").outerHeight(true) + $("#filter").outerHeight(true));
}