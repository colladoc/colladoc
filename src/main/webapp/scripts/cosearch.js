$(document).ready(function() {

    infiniteScroll();

    reloadSearchHeaders();

})

function reloadSearchHeaders() {
    $(".extype").tooltip({
        tip: "#tooltip",
        position:"top center",
        onBeforeShow: function(ev) {
            $(this.getTip()).text(this.getTrigger().attr("name"));
        }
    });

    var docSetSigs = $(".searchResult > .definition");
    function searchResultShow(element){
        //var vis = $(":visible", element);
        if ($(element).is(":visible")) {
            element.slideUp(100);
        }
        else {
            element.slideDown(100);
        }
    };

   // iterate through the items and assign functionality for
    // newly created ones by InfiniteScroll.. trick uses whether "pointer was assigned or not"
    // done by Alex

    docSetSigs.each(function(i) {
        if ($(this).css("cursor") != "pointer") {
            $(this).css("cursor","pointer");
            $(this).click(function(){
                searchResultShow($("+ div", $(this)));
            });
        }
    });

    var docAllSigs = $(".searchResult > .signature");
    var docShowSigs = docAllSigs.filter(function(){
        return $("+ div.fullcomment", $(this)).length > 0;
    });

   // iterate through the items and assign functionality for
    // newly created ones by InfiniteScroll.. trick uses whether "pointer was assigned or not"
    // done by Alex

    docShowSigs.each(function(i) {
        if ($(this).css("cursor") != "pointer") {
            $(this).css("cursor","pointer");
            $(this).click(function(){
                searchResultShow($("+ div.fullcomment", $(this)));
            });
        }
    });
}



function infiniteScroll(){
        $contentLoadTriggered = false;

        // initialisation of page
        var page=2;
        var pageUrl=window.location.href;

        $("#searchPanel").scroll(function(){

            if($("#searchPanel").scrollTop() > ($("#searchResults").height() - $("#searchPanel").height()) && $contentLoadTriggered == false)
            {
                $contentLoadTriggered = true;

                // load content
                $.ajax({
                url: pageUrl+ '&page='+page,
                async: true,
                cache: false,
                success: function (data) {

                    if (data.length != 0 ){
                    var result=$(data).find("#searchResults");
                    var noRes =$(result).find("#noResults");
                        if (!noRes.length) {
                            $("#searchResults").append(result.children());
                            $contentLoadTriggered = false;
                                //alert(pageUrl+ '&page='+page);
                            page++;

                            var urlpart=pageUrl.split("q=");
                            if ((urlpart[1] !="") && (typeof(urlpart[1]) !="undefined")) {localSearchHighlight((urlpart[1]).replace("_"," "))};
                            reloadSearchHeaders();
                            reloadSignatureAnimation();
                        }

                    }
                }
            });

            }

        });
 }