var topLevelTemplates = undefined;
var topLevelPackages = undefined;

var scheduler = undefined;
var domCache = undefined;

var kindFilterState = undefined;
var focusFilterState = undefined;

var title = $(document).attr('title')

$(document).ready(function() {
    $('body').layout({ west__size: '20%' });
    $('iframe').bind("load", function(){
        var subtitle = $(this).contents().find('title').text();
        $(document).attr('title', (title ? title + " - " : "") + subtitle);
    });

    // workaround for IE's iframe sizing lack of smartness
    if($.browser.msie) {
        function fixIFrame() {
            $('iframe').height($(window).height() )
        }
        $('iframe').bind("load",fixIFrame)
        $('iframe').bind("resize",fixIFrame)
    }

    scheduler = new Scheduler();
    scheduler.addLabel("init", 1);
    scheduler.addLabel("focus", 2);
    scheduler.addLabel("kind", 3);
    scheduler.addLabel("filter", 4);

    scheduler.addForAll = function(labelName, elems, fn) {
        var idx = 0;
        var elem = undefined;
        while (idx < elems.length) {
            elem = elems[idx];
            scheduler.add(labelName, function(elem0) { fn(elem0); }, undefined, [elem]);
            idx = idx + 1;
        }
    }

    domCache = new DomCache();
    domCache.update();

    prepareEntityList();

    configureTextFilter();
    configureKindFilter();
    configureEntityList();

    shortcut_enable();


});

function shortcut_enable(){
    scheduler.add("init", function() {

    var isCtrl=false;
    $(document).keyup(function (e) {
        if(e.which == 17) isCtrl=false;
    }).keydown(function (e) {
        if(e.which == 17) isCtrl=true;
        if(e.which == 81 && isCtrl == true) {

            $("#svalue").focus().select();

            isCtrl = false;
            return false;
       }
    });
   });
}
function configureEntityList() {
    kindFilterSync();
    configureHideFilter();
    configureFocusFilter();
    //textFilter();
}

/* The DomCache class holds a series of pointers to interesting parts of the page's DOM tree. Generally, any DOM
   accessor should be reduced to the context of a relevant entity from the cache. This is crucial to maintaining
   decent performance of the page. */
function DomCache() {
    var cache = this;
    this.packs = undefined;
    this.liPacks = undefined;
    this.update = function() {
        cache.packs = $(".pack");
        cache.liPacks = cache.packs.filter("li");
    }
}

/* Updates the list of entities (i.e. the content of the #tpl element) from the raw form generated by Scaladoc to a
   form suitable for display. In particular, it adds class and object etc. icons, and it configures links to open in
   the right frame. Furthermore, it sets the two reference top-level entities lists (topLevelTemplates and
   topLevelPackages) to serve as reference for resetting the list when needed.
   Be advised: this function should only be called once, on page load. */
function prepareEntityList() {
    var classIcon = $("#library > img.class");
    var traitIcon = $("#library > img.trait");
    var objectIcon = $("#library > img.object");
    var packageIcon = $("#library > img.package");
    scheduler.addForAll("init", domCache.packs, function(pack) {
        var packTemplates = $("> ol.templates > li", pack);
        $("> h3 > a.tplshow", pack).add("> a.tplshow", packTemplates).attr("target", "template");
        $("span.class", packTemplates).each(function() { $(this).replaceWith(classIcon.clone()); });
        $("span.trait", packTemplates).each(function() { $(this).replaceWith(traitIcon.clone()); });
        $("span.object", packTemplates).each(function() { $(this).replaceWith(objectIcon.clone()); });
        $("span.package", packTemplates).each(function() { $(this).replaceWith(packageIcon.clone()); });
    });
    scheduler.add("init", function() {
        topLevelTemplates = $("#tpl > ol.templates").clone();
        topLevelPackages = $("#tpl > ol.packages").clone();
    });
}

/* Configures the text filter  */
function configureTextFilter() {
    scheduler.add("init", function() {
        $("#filter").append("<div id='textfilter'><span class='pre' id='searchbtn' style='cursor:pointer;'/><span class='input'><input id='svalue' type='text' accesskey='/'/></span><span class='post'/></div>");
        var input = $("#textfilter input");
        resizeFilterBlock();
        input.bind("keyup", function(event) {
            if (event.keyCode == 27) { // escape
                input.attr("value", "");
            }
            //textFilter();

        });
        input.focus();
    });
    scheduler.add("init", function() {
        $("#textfilter > .post").click(function(){
            $("#textfilter input").attr("value", "");
            //textFilter();
        });
    });

    scheduler.add("init", function() {$("#textfilter input").keypress(function (e) {
		if (((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) && ($("#textfilter input").attr("value")!="")) {
		doStuff();
		}
       });
    });
        scheduler.add("init", function() {
        $("#textfilter > .pre").click(function(){

            doStuff();

        });
        scheduler.add("init", function() {

            retrieveUrl();

        });
    });
}


function doStuff() {

            var str= $("#textfilter input").attr("value");
           $("iframe").eq(0).attr("src","search?q="+escape(str));
            //textFilter();

}

function retrieveUrl(){
            var url= $(location).attr("href");
            var urlpart=url.split("q=")

            if ((urlpart[1] !="") && (typeof(urlpart[1]) !="undefined")) {
            $("#textfilter input").attr("value",unescape(urlpart[1].replace(/\+/g, " ")));
            $("iframe").eq(0).attr("src","search.html?q="+urlpart[1]);
            }
}
// Filters all focused templates and packages. This function should be made less-blocking.
//   @param query The string of the query
function textFilter() {
    scheduler.clear("filter");
    scheduler.add("filter", function() {
        var query = $("#textfilter input").attr("value")
        var queryRegExp;
        if (query.toLowerCase() != query) {
            // Regexp that matches CamelCase subbits: "BiSe" is
            // "[a-z]*Bi[a-z]*Se" and matches "BitSet", "ABitSet", ...
            queryRegExp = new RegExp(query.replace(/([A-Z])/g,"[a-z]*$1"));
        }
        else { // if query is all lower case make a normal case insensitive search
            queryRegExp = new RegExp(query, "i");
        }
        scheduler.addForAll("filter", domCache.packs, function(pack0) {
            var pack = $(pack0);
            $("> ol.templates > li", pack).each(function(){
                var item = $(this).attr("title");
                if (item == "" || queryRegExp.test(item)) {
                    $(this).show();
                    $(this).removeClass("hide");
                }
                else {
                    $(this).addClass("hide");
                    $(this).hide();
                }
            });
            if ($("> ol > li:not(.hide)", pack).length > 0) {
                pack.show();
                pack.removeClass("hide");
            }
            else {
                pack.addClass("hide");
                pack.hide();
            }
            if ($("> ol.templates > li:not(.hide)", pack).length > 0) {
                $("> h3", pack).show();
                $("> .packhide", pack).show();
                $("> .packfocus", pack).show();
            }
            else {
                $("> h3", pack).hide();
                $("> .packhide", pack).hide();
                $("> .packfocus", pack).hide();
            }
        });
    });
}

/* Configures the hide tool by adding the hide link to all packages. */
function configureHideFilter() {
    scheduler.addForAll("init", domCache.liPacks, function(pack) {
        $(pack).prepend("<a class='packhide'>hide</a>");
        $("> a.packhide", pack).click(function(event) {
            var packhide = $(this)
            var action = packhide.text();
            if (action == "hide") {
                $("~ ol", packhide).hide();
                packhide.text("show");
            }
            else {
                $("~ ol", packhide).show();
                packhide.text("hide");
            }
            return false;
        });
    });
}

/* Configures the focus tool by adding the focus bar in the filter box (initially hidden), and by adding the focus
   link to all packages. */
function configureFocusFilter() {
    scheduler.add("init", function() {
        focusFilterState = null;
        if ($("#focusfilter").length == 0) {
            $("#filter").append("<div id='focusfilter'>focused on <span class='focuscoll'></span> <a class='focusremove'><img class='icon' src='lib/remove.png'/></a></div>");
            $("#focusfilter > .focusremove").click(function(event) {
                scheduler.clear("filter");
                scheduler.add("focus", function() {
                    $("#tpl > ol.templates").replaceWith(topLevelTemplates.clone());
                    $("#tpl > ol.packages").replaceWith(topLevelPackages.clone());
                    domCache.update();
                    $("#focusfilter").hide();
                    $("#kindfilter").show();
                    resizeFilterBlock();
                    focusFilterState = null;
                    configureEntityList();
                });
            });
            $("#focusfilter").hide();
            resizeFilterBlock();
        }
    });
    scheduler.addForAll("init", domCache.liPacks, function(pack) {
        $(pack).prepend("<a class='packfocus'>focus</a>");
        $("> a.packfocus", pack).click(function(event) {
            focusFilter($(this).parent());
            return false;
        });
    });
}

/* Focuses the entity index on a specific package. To do so, it will copy the sub-templates and sub-packages of the
   focuses package into the top-level templates and packages position of the index. The original top-level
     @param package The <li> element that corresponds to the package in the entity index */
function focusFilter(package) {
    scheduler.add("focus", function() {
        scheduler.clear("filter");
        var currentFocus = package.attr("title");
        $("#focusfilter > .focuscoll").empty();
        $("#focusfilter > .focuscoll").append(currentFocus);
        var packTemplates = $("> ol.templates", package);
        var packPackages = $("> ol.packages", package);
        $("#tpl > ol.templates").replaceWith(packTemplates);
        $("#tpl > ol.packages").replaceWith(packPackages);
        domCache.update();
        $("#focusfilter").show();
        $("#kindfilter").hide();
        resizeFilterBlock();
        focusFilterState = package;
        kindFilterSync();
    });
}

function configureKindFilter() {
    scheduler.add("init", function() {
        kindFilterState = "all";
        $("#filter").append("<div id='kindfilter'><a>display packages only</a></div>");
        $("#kindfilter > a").click(function(event) { kindFilter("packs"); });
        resizeFilterBlock();
    });
}

function kindFilter(kind) {
    if (kind == "packs") {
        kindFilterState = "packs";
        kindFilterSync();
        $("#kindfilter > a").replaceWith("<a>display all entities</a>");
        $("#kindfilter > a").click(function(event) { kindFilter("all"); });
    }
    else {
        kindFilterState = "all";
        kindFilterSync();
        $("#kindfilter > a").replaceWith("<a>display packages only</a>");
        $("#kindfilter > a").click(function(event) { kindFilter("packs"); });
    }
}

/* Applies the kind filter. */
function kindFilterSync() {
    scheduler.add("kind", function () {
        if (kindFilterState == "all" || focusFilterState != null)
            scheduler.addForAll("kind", domCache.packs, function(pack0) {
                $("> ol.templates", pack0).show();
            });
        else
            scheduler.addForAll("kind", domCache.packs, function(pack0) {
                $("> ol.templates", pack0).hide();
            });
        //textFilter();
    });
}

function resizeFilterBlock() {
    $("#tpl").css("top", $("#filter").outerHeight(true));
}

function getText(val) {
  var stext = $(val).attr("value");
    return stext;
}

