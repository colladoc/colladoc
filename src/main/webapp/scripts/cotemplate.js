$(document).ready(function() {
    $('a.edit').hide();
    $('.signature').hover(
            function() { $('a.edit', this).show() },
            function() { $('a.edit', this).hide() }
            );
})