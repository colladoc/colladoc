// © 2009–2010 EPFL/LAMP
// code by Gilles Dubochet with contributions by Pedro Furlanetto

$(document).ready(function(){
    var prefilters = $("#ancestors > ol > li").filter(function(){
        var name = $(this).attr("name");
        return name == "scala.Any" || name == "scala.AnyRef";
    });
    prefilters.removeClass("in");
    prefilters.addClass("out");
    filter();

    var input = $("#textfilter input");
    input.bind("keyup", function(event) {
        if (event.keyCode == 27) { // escape
            input.attr("value", "");
        }
        filter();
    });
    input.focus(function(event) { input.select(); });
    $("#textfilter > .post").click(function(){
        $("#textfilter input").attr("value", "");
        filter();
    });

    // Set up click functionality for the symbol filters.
    $("#symboltype > ol > li.package").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
        }
        else if ($(this).hasClass("in")) {
            $(this).removeClass("in").addClass("out");
        };
        filter();
    });
    $("#symboltype > ol > li.type").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
        }
        else if ($(this).hasClass("in")) {
            $(this).removeClass("in").addClass("out");
        };
        filter();
    });
    $("#symboltype > ol > li.object").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
        }
        else if ($(this).hasClass("in")) {
            $(this).removeClass("in").addClass("out");
        };
        filter();
    });
    $("#symboltype > ol > li.class").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
        }
        else if ($(this).hasClass("in")) {
            $(this).removeClass("in").addClass("out");
        };
        filter();
    });
    $("#symboltype > ol > li.trait").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
        }
        else if ($(this).hasClass("in")) {
            $(this).removeClass("in").addClass("out");
        };
        filter();
    });
    $("#symboltype > ol > li.constructor").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
        }
        else if ($(this).hasClass("in")) {
            $(this).removeClass("in").addClass("out");
        };
        filter();
    });
    $("#symboltype > ol > li.def").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
        }
        else if ($(this).hasClass("in")) {
            $(this).removeClass("in").addClass("out");
        };
        filter();
    });
    $("#symboltype > ol > li.val").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
        }
        else if ($(this).hasClass("in")) {
            $(this).removeClass("in").addClass("out");
        };
        filter();
    });
    $("#symboltype > ol > li.var").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
        }
        else if ($(this).hasClass("in")) {
            $(this).removeClass("in").addClass("out");
        };
        filter();
    });

    $("#ancestors > ol > li").click(function(){
        if ($(this).hasClass("in")) {
            $(this).removeClass("in");
            $(this).addClass("out");
        }
        else if ($(this).hasClass("out")) {
            $(this).removeClass("out");
            $(this).addClass("in");
        };
        filter();
    });
    $("#ancestors > ol > li.hideall").click(function() {
        $("#ancestors > ol > li.in").removeClass("in").addClass("out");
        filter();
    })
    $("#ancestors > ol > li.showall").click(function() {
        var filtered =
            $("#ancestors > ol > li.out").filter(function() {
                var name = $(this).attr("name");
                return !(name == "scala.Any" || name == "scala.AnyRef");
            });
        filtered.removeClass("out").addClass("in");
        filter();
    });
    $("#visbl > ol > li.public").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
            $("#visbl > ol > li.all").removeClass("in").addClass("out");
            filter();
        };
    })
    $("#visbl > ol > li.all").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
            $("#visbl > ol > li.public").removeClass("in").addClass("out");
            filter();
        };
    });
    $("#impl > ol > li.concrete").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");            
            $("li[data-isabs='false']").show();
        } else {
            $(this).removeClass("in").addClass("out");
            $("li[data-isabs='false']").hide();
        }
    });
    $("#impl > ol > li.abstract").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");                        
            $("li[data-isabs='true']").show();
        } else {
            $(this).removeClass("in").addClass("out");
            $("li[data-isabs='true']").hide();
        }
    });
    $("#order > ol > li.alpha").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
            $("#order > ol > li.inherit").removeClass("in").addClass("out");
            orderAlpha();
        };
    })
    $("#order > ol > li.inherit").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
            $("#order > ol > li.alpha").removeClass("in").addClass("out");
            orderInherit();
        };
    });
    initInherit();
    //http://flowplayer.org/tools/tooltip.html
    $(".extype").tooltip({
        tip: "#tooltip",
        position:"top center",
        onBeforeShow: function(ev) {
            $(this.getTip()).text(this.getTrigger().attr("name"));
        }
    });
    $(".defval").tooltip({
        tip: "#tooltip",
        position:"top center",        
        onBeforeShow: function(ev) {
            $(this.getTip()).html(this.getTrigger().attr("name"))
        }        
    });

    reloadSignatureAnimation();
});


   // iterate through the items and assign functionality for
    // newly created ones by InfiniteScroll.. trick uses whether "pointer was assigned or not"
    // done by Alex

function reloadSignatureAnimation() {
        var docAllSigs = $(".signature");
    function commentShowFct(fullComment){
        var vis = $(":visible", fullComment);
        if (vis.length > 0) {
            fullComment.slideUp(100);
        }
        else {
            fullComment.slideDown(100);
        }
    };
    var docShowSigs = docAllSigs.filter(function(){
        return $("+ div.fullcomment", $(this)).length > 0;
    });

    docShowSigs.each(function(i) {
        if ($(this).css("cursor") != "pointer") {
            $(this).css("cursor","pointer");
            $(this).click(function(){
                commentShowFct($("+ div.fullcomment", $(this)));
            });
        }
    });

    function commentToggleFct(shortComment){
        var vis = $("~ div.fullcomment:visible", shortComment);
        if (vis.length > 0) {
            shortComment.slideDown(100);
            vis.slideUp(100);
        }
        else {
            var hid = $("~ div.fullcomment:hidden", shortComment);
            hid.slideDown(100);
            shortComment.slideUp(100);
        }
    };
    var docToggleSigs = docAllSigs.filter(function(){
        return $("+ p.shortcomment", $(this)).length > 0;
    });

    docToggleSigs.each(function(i) {
        if ($(this).css("cursor") != "pointer") {
            $(this).css("cursor","pointer");
            $(this).click(function(){
                commentToggleFct($("+ p.shortcomment", $(this)));
            });
        }
    });


    $("p.shortcomment").unbind("click").click(function(){
            commentToggleFct($(this));
    });

}


function orderAlpha() {
    $("#template > div.parent").hide();
    $("#ancestors").show();
    filter();
};

function orderInherit() {
    $("#template > div.parent").show();
    $("#ancestors").hide();
    filter();
};

/** Prepares the DOM for inheritance-based display. To do so it will:
  *  - hide all statically-generated parents headings;
  *  - copy all members from the value and type members lists (flat members) to corresponding lists nested below the
  *    parent headings (inheritance-grouped members);
  *  - initialises a control variable used by the filter method to control whether filtering happens on flat members
  *    or on inheritance-grouped members. */
function initInherit() {
    // parents is a map from fully-qualified names to the DOM node of parent headings.
    var parents = new Object();
    $("#template > div.parent").each(function(){
        parents[$(this).attr("name")] = $(this);
    });
    // 
    $("#types > ol > li").each(function(){
        var qualName = $(this).attr("name");
        var owner = qualName.slice(0, qualName.indexOf("#"));
        var name = qualName.slice(qualName.indexOf("#") + 1);
        var parent = parents[owner];
        if (parent != undefined) {
            var types = $("> .types > ol", parent);
            if (types.length == 0) {
                parent.append("<div class='types members'><h3>Type Members</h3><ol></ol></div>");
                types = $("> .types > ol", parent);
            }
            types.append($(this).clone());
        }
    });
    $("#values > ol > li").each(function(){
        var qualName = $(this).attr("name");
        var owner = qualName.slice(0, qualName.indexOf("#"));
        var name = qualName.slice(qualName.indexOf("#") + 1);
        var parent = parents[owner];
        if (parent != undefined) {
            var values = $("> .values > ol", parent);
            if (values.length == 0) {
                parent.append("<div class='values members'><h3>Value Members</h3><ol></ol></div>");
                values = $("> .values > ol", parent);
            }
            values.append($(this).clone());
        }
    });
    $("#template > div.parent").each(function(){
        if ($("> div.members", this).length == 0) { $(this).remove(); };
    });
    $("#template > div.parent").each(function(){
        $(this).hide();
    });
};

// Determines whether a displayed <li> member is filtered by the symbols filter.
function isShownBySymbol(member) {
    var memberKind = member.find(".kind").text();

    switch(memberKind) {
        case "package":
            return $("#symboltype > ol > li.package").hasClass("in");
        case "type":
            return $("#symboltype > ol > li.type").hasClass("in");
        case "object":
            return $("#symboltype > ol > li.object").hasClass("in");
        case "class":
            return $("#symboltype > ol > li.class").hasClass("in");
        case "trait":
            return $("#symboltype > ol > li.trait").hasClass("in");
        case "new":
            return $("#symboltype > ol > li.constructor").hasClass("in");
        case "def":
            return $("#symboltype > ol > li.def").hasClass("in");
        case "val":
            return $("#symboltype > ol > li.val").hasClass("in");
        case "var":
            return $("#symboltype > ol > li.var").hasClass("in");
        case "default":
            return true;
    }

    return true;
};

function filter() {
    var query = $("#textfilter input").attr("value").toLowerCase();
    var queryRegExp = new RegExp(query, "i");
    var inheritHides = null
    if ($("#order > ol > li.inherit").hasClass("in")) {
        inheritHides = $("#linearization > li:gt(0)");
    }
    else {
        inheritHides = $("#linearization > li.out");
    }
    var outOwners =
        inheritHides.map(function(){
            return $(this).attr("name")
        }).get();
    var prtVisbl = $("#visbl > ol > li.all").hasClass("in");
    $(".members > ol > li").each(function(){
        var vis1 = $(this).attr("visbl");
        var qualName1 = $(this).attr("name");
        //var name1 = qualName1.slice(qualName1.indexOf("#") + 1);
        var showByOwned = true;
        if ($(this).parents(".parent").length == 0) {
           // owner filtering must not happen in "inherited from" member lists
            var owner1 = qualName1.slice(0, qualName1.indexOf("#"));
            for (out in outOwners) {
                if (outOwners[out] == owner1) {
                    showByOwned = false;
                };
            };
        };
        var showByVis = true;
        if (vis1 == "prt") {
            showByVis = prtVisbl;
        };
        var showByName = true;
        if (query != "") {
            var content = $(this).attr("name") + $("> .fullcomment .cmt", this).text();
            showByName = queryRegExp.test(content);
        };

        var showBySymbol = isShownBySymbol($(this));

        if (showByOwned && showByVis && showByName && showBySymbol) {
          $(this).show();
        }
        else {
          $(this).hide();
        };
    });
    $(".members").each(function(){
        $(this).show();
        if ($(" > ol > li:visible", this).length == 0) { $(this).hide(); }
    });
    return false
};

function windowTitle()
{
    parent.document.title=document.title;
};
