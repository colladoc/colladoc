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

    // fix for datepicker
    $('#ui-datepicker-div').css('clip', 'auto');
});

function reload() {
    var input = $("#textfilter > span > input");
    input.bind("keyup", function(event) {
        if (event.keyCode == 27) { // escape
            input.attr("value", "");
        }
        filter();
    });
    input.focus(function(event) { input.select(); });

    input.change(function() { filter(); });

    $("#textfilter > .post").click(function(){
        $("#textfilter > span > input").attr("value", "");
        filter();
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

    $(".extype").tooltip({
        tip: "#tooltip",
        position:"top center",
        onBeforeShow: function(ev) {
            $(this.getTip()).text(this.getTrigger().attr("name"));
        }
    });

    /* Add toggle arrows */
    var docAllSigs = $("h4.signature");

    function commentToggleFct(signature){
        var parent = signature.parent();
        var shortComment = $(".shortcomment", parent);
        var fullComment = $(".fullcomment", parent);
        var vis = $(":visible", fullComment);
        signature.toggleClass("closed").toggleClass("opened");
        if (vis.length > 0) {
            shortComment.slideDown(100);
            fullComment.slideUp(100);
        }
        else {
            shortComment.slideUp(100);
            fullComment.slideDown(100);
        }
    }
    docAllSigs.addClass("closed");
    docAllSigs.click(function() {
        commentToggleFct($(this));
    });

    var docSetSigs = $(".changeset > .definition");
    function commentShow(element){
        var vis = $(":visible", element);
        if (vis.length > 0) {
            element.slideUp(100);
        }
        else {
            element.slideDown(100);
        }
    }
    docSetSigs.css("cursor", "pointer");
    docSetSigs.click(function(){
        commentShow($(this).next());
    });
    filter();

    replaceCaseClassIcon();
}

function filter() {
    var value = $("#textfilter input").attr("value");
    var query = "";
    if (value)
        query = value.toLowerCase();
    var queryRegExp = new RegExp(query, "i");
    $(".members > ol > li").each(function(){
        var qualName1 = $(this).attr("name");
        var showByName = true;
        if (query != "") {
            var content = $(this).attr("name") + $("> .fullcomment .cmt", this).text();
            showByName = queryRegExp.test(content);
        };
        if (showByName) {
          $(this).show();
        }
        else {
          $(this).hide();
        };
    });
    
    var comparator = $("#order > ol > li.alpha").hasClass("in") ?
        function(a, b) { return $(a).attr("name") > $(b).attr("name") ? 1 : -1; }:
        function(a, b) { return $(a).attr("date") < $(b).attr("date") ? 1 : -1; };
    order($(".changeset"), comparator);
    $(".members > ol").each(function() {
      order($("> li", this), comparator);
    });

    $(".changeset").each(function() {
        if ($(".fullcomment", this).length > 3) { $("> .definition ~ *", this).hide(); }
    });
    return false
};

function order(members, comparator) {
    var sort = [].sort;
    var last = null;
    return sort.call(members, comparator).each(function(i) {
        var node = $(this);
        if (last) {
            last.after(node);
        } else {
            node.parent().prepend(node);
        }
        last = node;
    });
}

function replaceCaseClassIcon() {
    $("img[src$='lib/case class.png']").attr("src", "lib/class.png");
}