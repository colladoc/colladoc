
//Created by Alex

$(document).ready(function(){
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

                    if (data.length != 0){
                    var result=$(data).find("#searchResults");
                    $("#searchResults").append(result.children());
                    $contentLoadTriggered = false;
                        //alert(pageUrl+ '&page='+page);
                    page++;

                    var urlpart=pageUrl.split("q=");
                    if ((urlpart[1] !="") && (typeof(urlpart[1]) !="undefined")) {localSearchHighlight((urlpart[1]).replace("_"," "))};
                    }
                }
            });

            }

        });



 });