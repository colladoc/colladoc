/*
 * Copyright (c) 2010, Petr Hosek. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 *     the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COLLABORATIVE SCALADOC PROJECT ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COLLABORATIVE SCALADOC
 * PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

$(document).ready(function() {
    var dates = $('.date').datepicker({
        onSelect: function(selected) {
            var option = $(this).hasClass("from") ? "minDate" : "maxDate";
            var instance = $(this).data("datepicker");
            var date = $.datepicker.parseDate(instance.settings.dateFormat || $.datepicker._defaults.dateFormat, selected, instance.settings);
            dates.not(this).datepicker("option", option, date);
            $(this).change();
        }
    });
    
    reload();
})

function reload() {
    var input = $("#textfilter > input");
    input.bind("keyup", function(event) {
        if (event.keyCode == 27) { // escape
            input.attr("value", "");
        }
        filter();
    });
    input.focus(function(event) { input.select(); });
    $("#textfilter > .post").click(function(){
        $("#textfilter > input").attr("value", "");
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

    $("#order > ol > li.date").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
            $("#order > ol > li.alpha").removeClass("in").addClass("out");
            filter();
        };
    });
    $("#order > ol > li.alpha").click(function() {
        if ($(this).hasClass("out")) {
            $(this).removeClass("out").addClass("in");
            $("#order > ol > li.date").removeClass("in").addClass("out");
            filter();
        };
    })

    $('.button').button();
    $('.select').selectmenu({
        width: 250
    });
    $(".extype").tooltip({
        tip: "#tooltip",
        position:"top center",
        onBeforeShow: function(ev) {
            $(this.getTip()).text(this.getTrigger().attr("name"));
        }
    });

    var docSigs = $(".changeset > .signature");
    function commentShow(controls){
        var vis = $(":visible", controls);
        if (vis.length > 0) {
            controls.slideUp(100);
        }
        else {
            controls.slideDown(100);
        }
    };
    docSigs.css("cursor", "pointer");
    docSigs.click(function(){
        commentShow($("+ div.members", $(this)));
    });

    var docAllSigs = $("#template .signature");
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
    docShowSigs.css("cursor", "pointer");
    docShowSigs.click(function(){
        commentShowFct($("+ div.fullcomment", $(this)));
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
    docToggleSigs.css("cursor", "pointer");
    docToggleSigs.click(function(){
        commentToggleFct($("+ p.shortcomment", $(this)));
    });
    $("p.shortcomment").click(function(){
        commentToggleFct($(this));
    });

    filter();
}

function orderAlpha() {
    $("#template > div.parent").hide();
    $("#ancestors").show();
    filter();
};

function orderDate() {
    $("#template > div.parent").show();
    $("#ancestors").hide();
    filter();
};

function filter() {
    var query = $("#textfilter > input").attr("value").toLowerCase();
    var queryRegExp = new RegExp(query, "i");
    var prtVisbl = $("#visbl > ol > li.all").hasClass("in");
    $(".members > ol > li").each(function(){
        var vis1 = $(this).attr("visbl");
        var qualName1 = $(this).attr("name");
        var showByVis = true;
        if (vis1 == "prt") {
            showByVis = prtVisbl;
        };
        var showByName = true;
        if (query != "") {
            var content = $(this).attr("name") + $("> .fullcomment .cmt", this).text();
            showByName = queryRegExp.test(content);
        };
        if (showByVis && showByName) {
          $(this).show();
        }
        else {
          $(this).hide();
        };
    });
    $(".members").each(function(){
        if ($(" > ol > li:visible", this).length == 0) { $(this).hide(); }
    });
    return false
};