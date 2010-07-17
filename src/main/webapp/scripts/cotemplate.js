$(document).ready(function() {
    $('a.control').hide();
    $('.signature').hover(
            function() { $('a.control', this).show() },
            function() { $('a.control', this).hide() }
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