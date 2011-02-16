$(document).ready(function() {
    reload();
})

function reload() {
    $(".extype").tooltip({
        tip: "#tooltip",
        position:"top center",
        onBeforeShow: function(ev) {
            $(this.getTip()).text(this.getTrigger().attr("name"));
        }
    });

    var docSetSigs = $(".searchResult > .definition");
    function searchResultShow(element){
        var vis = $(":visible", element);
        if (vis.length > 0) {
            element.slideUp(100);
        }
        else {
            element.slideDown(100);
        }
    };
    docSetSigs.css("cursor", "pointer");
    docSetSigs.click(function(){
        searchResultShow($("+ div", $(this)));
    });
    var docAllSigs = $(".searchResult .signature");
    var docShowSigs = docAllSigs.filter(function(){
        return $("+ div.fullcomment", $(this)).length > 0;
    });
    docShowSigs.css("cursor", "pointer");
    docShowSigs.click(function(){
        searchResultShow($("+ div.fullcomment", $(this)));
    });
}