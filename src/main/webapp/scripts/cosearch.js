$(document).ready(function() {

    infiniteScroll();
    reloadSearchHeaders();

    $("body").append('<div id="loaderGif" align="center" style="position:fixed;bottom:1px;right:15px;display:none;">' +
                        'Loading...<p>'+
                        '<img src="images/ajax-loader2.gif" />' +
                 '</div>');

     replaceImgNameToClass();
     updateSearchText();

})

// there is a bug in original ScalaDoc. they forgot to include "case class" icon,
// which produces undefined img-src during the runtime.
// author: Alex

function replaceImgNameToClass(){
   var allImg=$("h4.definition a>img");
    allImg.each(function () {

        if ($(this).attr("src")=="lib/case class.png") {
           $(this).attr("src","lib/class.png");
        }

    });


}


function updateSearchText() {

        var links = $(".nodecoration li").children();

        links.each(function (i) {

                    $(this).click( function() {

                      var val=$(this).text();
                    
                      var elem = parent.window.document.getElementById("svalue");

                      $(elem).attr("value", val);

                    });

        });

}




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

function reloadSearchHighlight() {

    var urlpart=$(location).attr("href").split("q=");
    if ((urlpart[1] !="") && (typeof(urlpart[1]) !="undefined")) {localSearchHighlight(urlpart[1])};

}

function infiniteScroll(){
        $contentLoadTriggered = false;

        // initialisation of page
        var page=2;

        $("#searchPanel").scroll(function(){

            if($("#searchPanel").scrollTop() > ($("#searchResults").height() - $("#searchPanel").height()-20) && $contentLoadTriggered == false)
            {
                $contentLoadTriggered = true;

                //calculating new page url

                var pageUrl=window.location.href;
                pageUrl=pageUrl+"&page="+page;

                // Show loading image
                $("#loaderGif").fadeIn('fast').delay(300).fadeOut('fast');

                // load content
                $.ajax({
                url: pageUrl,
                async: true,
                cache: false,
                success: function (data) {

                if (data.length != 0 ){

                    var result=$(data).find("#searchResults");
                    var noRes =$(result).find("#noResults");

                        if (!noRes.length) {



                             // get the existing rec count from the view and update then
                            var recText = $("#recCount").text();

                            // number of elements of newly added elements
                            var newNum=result.children("div").children("div").children("div").children("ol").children().length;

                            // update retrieved record count
                            $("#recCount").text(parseInt(recText) + newNum);

                            var combos=$(result).find("select");
                            combos.selectmenu({width:300});

                            // append new elements to results page
                            $("#searchResults").append(result.children());
                            $contentLoadTriggered = false;

                            // increase the page variable
                            page++;

                            // reload function to bind activities to newly added elements
                            reloadSearchHighlight();
                            reloadSearchHeaders();
                            reloadSignatureAnimation();


                        }

                    }

                }
            });

            }

        });
 }