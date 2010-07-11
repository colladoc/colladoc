$(document).ready(function() {
    $('a.edit').hide();
    $('.signature').hover(
            function() { $('a.edit', this).show() },
            function() { $('a.edit', this).hide() }
            );

    var docAllSigs = $("#template .signature");

    $('a.edit').click(function(event) {
       event.stopPropagation();
       var shortComment = $("+ p.shortcomment", this.parentNode)
       var fullComment = $("~ div.fullcomment:hidden", shortComment);
       if (fullComment) {
            fullComment.slideDown(100);
            shortComment.slideUp(100);
       }
    });
})