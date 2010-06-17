$(document).ready(function() {
    $('a.edit').hide()
    $('.fullcomment').hover(
            function() { $('a.edit', this).show() },
            function() { $('a.edit', this).hide() }
            )
})