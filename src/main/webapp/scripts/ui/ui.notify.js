/*
 * jQuery Notify Plugin 1.0.0 Beta 1
 *
 * Copyright (c) 2009 Hunter Perrin
 *
 * Licensed under the GNU Affero GPL:
 *	  http://www.gnu.org/licenses/agpl.html
 */

(function($) {
	var timer, body, jwindow;
	$.extend({
		dispose_all: function () {
			var body_data = body.data("notify");
			/* POA: Added null-check */
			if (body_data && body_data.length) {
				$.each(body_data, function(){
					if (this.dispose)
						this.dispose();
				});
			}
		},
		position_all: function () {
			if (timer)
				clearTimeout(timer);
			timer = null;
			var body_data = body.data("notify");
			if (!body_data || !body_data.length)
				return;
			$.each(body_data, function(){
				var s = this.opts.stack;
				if (!s) return;
				if (!s.nextpos1)
					s.nextpos1 = s.firstpos1;
				if (!s.nextpos2)
					s.nextpos2 = s.firstpos2;
				if (!s.addpos2)
					s.addpos2 = 0;
				if (this.css("display") != "none") {
					var curpos1, curpos2;
					var animate = {};
					// Calculate the current pos1 value.
					var csspos1;
					switch (s.dir1) {
						case "down":
							csspos1 = "top";
							break;
						case "up":
							csspos1 = "bottom";
							break;
						case "left":
							csspos1 = "right";
							break;
						case "right":
							csspos1 = "left";
							break;
					}
					curpos1 = parseInt(this.css(csspos1));
					if (isNaN(curpos1))
						curpos1 = 0;
					// Remember the first pos1, so the first visible notice goes there.
					if (typeof s.firstpos1 == "undefined") {
						s.firstpos1 = curpos1;
						s.nextpos1 = s.firstpos1;
					}
					// Calculate the current pos2 value.
					var csspos2;
					switch (s.dir2) {
						case "down":
							csspos2 = "top";
							break;
						case "up":
							csspos2 = "bottom";
							break;
						case "left":
							csspos2 = "right";
							break;
						case "right":
							csspos2 = "left";
							break;
					}
					curpos2 = parseInt(this.css(csspos2));
					if (isNaN(curpos2))
						curpos2 = 0;
					// Remember the first pos2, so the first visible notice goes there.
					if (typeof s.firstpos2 == "undefined") {
						s.firstpos2 = curpos2;
						s.nextpos2 = s.firstpos2;
					}
					// Check that it's not beyond the viewport edge.
					if ((s.dir1 == "down" && s.nextpos1 + this.height() > jwindow.height()) ||
						(s.dir1 == "up" && s.nextpos1 + this.height() > jwindow.height()) ||
						(s.dir1 == "left" && s.nextpos1 + this.width() > jwindow.width()) ||
						(s.dir1 == "right" && s.nextpos1 + this.width() > jwindow.width()) ) {
						// If it is, it needs to go back to the first pos1, and over on pos2.
						s.nextpos1 = s.firstpos1;
						s.nextpos2 += s.addpos2 + 10;
						s.addpos2 = 0;
					}
					// Animate if we're moving on dir2.
					if (s.animation && s.nextpos2 < curpos2) {
						switch (s.dir2) {
							case "down":
								animate.top = s.nextpos2+"px";
								break;
							case "up":
								animate.bottom = s.nextpos2+"px";
								break;
							case "left":
								animate.right = s.nextpos2+"px";
								break;
							case "right":
								animate.left = s.nextpos2+"px";
								break;
						}
					} else
						this.css(csspos2, s.nextpos2+"px");
					// Keep track of the widest/tallest notice in the column/row, so we can push the next column/row.
					switch (s.dir2) {
						case "down":
						case "up":
							if (this.outerHeight(true) > s.addpos2)
								s.addpos2 = this.height();
							break;
						case "left":
						case "right":
							if (this.outerWidth(true) > s.addpos2)
								s.addpos2 = this.width();
							break;
					}
					// Move the notice on dir1.
					if (s.nextpos1) {
						// Animate if we're moving toward the first pos.
						if (s.animation && (curpos1 > s.nextpos1 || animate.top || animate.bottom || animate.right || animate.left)) {
							switch (s.dir1) {
								case "down":
									animate.top = s.nextpos1+"px";
									break;
								case "up":
									animate.bottom = s.nextpos1+"px";
									break;
								case "left":
									animate.right = s.nextpos1+"px";
									break;
								case "right":
									animate.left = s.nextpos1+"px";
									break;
							}
						} else
							this.css(csspos1, s.nextpos1+"px");
					}
					if (animate.top || animate.bottom || animate.right || animate.left)
						this.animate(animate, {duration: 500, queue: false});
					// Calculate the next dir1 position.
					switch (s.dir1) {
						case "down":
						case "up":
							s.nextpos1 += this.height() + 10;
							break;
						case "left":
						case "right":
							s.nextpos1 += this.width() + 10;
							break;
					}
				}
			});
			// Reset the next position data.
			$.each(body_data, function(){
				var s = this.opts.stack;
				if (!s) return;
				s.nextpos1 = s.firstpos1;
				s.nextpos2 = s.firstpos2;
				s.addpos2 = 0;
				s.animation = true;
			});
		},
		notify: function(options) {
			if (!body)
				body = $("body");
			if (!jwindow)
				jwindow = $(window);

			var animating;
			
			// Build main options.
			var opts;
			if (typeof options != "object") {
				opts = $.extend({}, $.notify.defaults);
				opts.text = options;
			} else {
				opts = $.extend({}, $.notify.defaults, options);
			}

			if (opts.before_init) {
				if (opts.before_init(opts) === false)
					return null;
			}

			// This keeps track of the last element the mouse was over, so
			// mouseleave, mouseenter, etc can be called.
			var nonblock_last_elem;
			// This is used to pass events through the notice if it is non-blocking.
			var nonblock_pass = function(e, e_name){
				notify.css("display", "none");
				var element_below = document.elementFromPoint(e.clientX, e.clientY);
				notify.css("display", "block");
				var jelement_below = $(element_below);
				var cursor_style = jelement_below.css("cursor");
				notify.css("cursor", cursor_style != "auto" ? cursor_style : "default");
				// If the element changed, call mouseenter, mouseleave, etc.
				if (!nonblock_last_elem || nonblock_last_elem.get(0) != element_below) {
					if (nonblock_last_elem) {
						dom_event.call(nonblock_last_elem.get(0), "mouseleave", e.originalEvent);
						dom_event.call(nonblock_last_elem.get(0), "mouseout", e.originalEvent);
					}
					dom_event.call(element_below, "mouseenter", e.originalEvent);
					dom_event.call(element_below, "mouseover", e.originalEvent);
				}
				dom_event.call(element_below, e_name, e.originalEvent);
				// Remember the latest element the mouse was over.
				nonblock_last_elem = jelement_below;
			};

			// Create our widget.
			// Stop animation, reset the removal timer, and show the close
			// button when the user mouses over.
			var notify = $("<div />", {
				"class": "ui-notify "+opts.addclass,
				"css": {"display": "none"},
				"mouseenter": function(e){
					if (opts.nonblock) e.stopPropagation();
					if (opts.mouse_reset && animating == "out") {
						// If it's animating out, animate back in really quick.
						notify.stop(true);
						notify.css("height", "auto").animate({"width": opts.width, "opacity": opts.nonblock ? opts.nonblock_opacity : opts.opacity}, "fast");
					} else if (opts.nonblock && animating != "out") {
						// If it's non-blocking, animate to the other opacity.
						notify.animate({"opacity": opts.nonblock_opacity}, "fast");
					}
					if (opts.hide && opts.mouse_reset) notify.cancel_remove();
					if (opts.closer && !opts.nonblock) notify.closer.show();
				},
				"mouseleave": function(e){
					if (opts.nonblock) e.stopPropagation();
					nonblock_last_elem = null;
					notify.css("cursor", "auto");
					if (opts.nonblock && animating != "out")
						notify.animate({"opacity": opts.opacity}, "fast");
					if (opts.hide && opts.mouse_reset) notify.queue_remove();
					notify.closer.hide();
					$.position_all();
				},
				"mouseover": function(e){
					if (opts.nonblock) e.stopPropagation();
				},
				"mouseout": function(e){
					if (opts.nonblock) e.stopPropagation();
				},
				"mousemove": function(e){
					if (opts.nonblock) {
						e.stopPropagation();
						nonblock_pass(e, "onmousemove");
					}
				},
				"mousedown": function(e){
					if (opts.nonblock) {
						e.stopPropagation();
						e.preventDefault();
						nonblock_pass(e, "onmousedown");
					}
				},
				"mouseup": function(e){
					if (opts.nonblock) {
						e.stopPropagation();
						e.preventDefault();
						nonblock_pass(e, "onmouseup");
					}
				},
				"click": function(e){
					if (opts.nonblock) {
						e.stopPropagation();
						nonblock_pass(e, "onclick");
					}
				},
				"dblclick": function(e){
					if (opts.nonblock) {
						e.stopPropagation();
						nonblock_pass(e, "ondblclick");
					}
				}
			});
			notify.opts = opts;
			// Create a drop shadow.
			if (opts.shadow && !$.browser.msie)
				notify.shadow_container = $("<div />", {"class": "ui-widget-shadow ui-corner-all ui-notify-shadow"}).prependTo(notify);
			// Create a container for the notice contents.
			notify.container = $("<div />", {"class": "ui-widget ui-widget-content ui-corner-all ui-notify-container "+(opts.type == "error" ? "ui-state-error" : "ui-state-highlight")})
			.appendTo(notify);

			notify.version = "1.0.0b1";

			// This function is for updating the notice.
			notify.notify = function(options) {
				// Update the notice.
				var old_opts = opts;
				if (typeof options == "string")
					opts.text = options;
				else
					opts = $.extend({}, opts, options);
				notify.opts = opts;
				// Update the shadow.
				if (opts.shadow != old_opts.shadow) {
					if (opts.shadow && !$.browser.msie)
						notify.shadow_container = $("<div />", {"class": "ui-widget-shadow ui-notify-shadow"}).prependTo(notify);
					else
						notify.children(".ui-notify-shadow").remove();
				}
				// Update the additional classes.
				if (opts.addclass === false)
					notify.removeClass(old_opts.addclass);
				else if (opts.addclass !== old_opts.addclass)
					notify.removeClass(old_opts.addclass).addClass(opts.addclass);
				// Update the title.
				if (opts.title === false)
					notify.title_container.hide("fast");
				else if (opts.title !== old_opts.title)
					notify.title_container.html(opts.title).show(200);
				// Update the text.
				if (opts.text === false) {
					notify.text_container.hide("fast");
				} else if (opts.text !== old_opts.text) {
					if (opts.insert_brs)
						opts.text = opts.text.replace(/\n/g, "<br />");
					notify.text_container.html(opts.text).show(200);
				}
				// Change the notice type.
				if (opts.type != old_opts.type)
					notify.container.toggleClass("ui-state-error ui-state-highlight");
				if ((opts.notice_icon != old_opts.notice_icon && opts.type == "notice") ||
					(opts.error_icon != old_opts.error_icon && opts.type == "error") ||
					(opts.type != old_opts.type)) {
					// Remove any old icon.
					notify.container.find("div.ui-notify-icon").remove();
					if ((opts.error_icon && opts.type == "error") || (opts.notice_icon)) {
						// Build the new icon.
						$("<div />", {"class": "ui-notify-icon"})
						.append($("<span />", {"class": opts.type == "error" ? opts.error_icon : opts.notice_icon}))
						.prependTo(notify.container);
					}
				}
				// Update the width.
				if (opts.width !== old_opts.width)
					notify.animate({width: opts.width});
				// Update the minimum height.
				if (opts.min_height !== old_opts.min_height)
					notify.container.animate({minHeight: opts.min_height});
				// Update the opacity.
				if (opts.opacity !== old_opts.opacity)
					notify.fadeTo(opts.animate_speed, opts.opacity);
				if (!opts.hide)
					notify.cancel_remove();
				else if (!old_opts.hide)
					notify.queue_remove();
				notify.queue_position();
				return notify;
			};

			// Queue the position function so it doesn't run repeatedly and use
			// up resources.
			notify.queue_position = function() {
				if (timer)
					clearTimeout(timer);
				timer = setTimeout($.position_all, 10);
			};

			// Display the notice.
			notify.display = function() {
				// If the notice is not in the DOM, append it.
				if (!notify.parent().length)
					notify.appendTo(body);
				// Run callback.
				if (opts.before_open) {
					if (opts.before_open(notify) === false)
						return;
				}
				notify.queue_position();
				// First show it, then set its opacity, then hide it.
				if (opts.animation == "fade" || opts.animation.effect_in == "fade") {
					// If it's fading in, it should start at 0.
					notify.show().fadeTo(0, 0).hide();
				} else {
					// Or else it should be set to the opacity.
					if (opts.opacity != 1)
						notify.show().fadeTo(0, opts.opacity).hide();
				}
				notify.animate_in(function(){
					if (opts.after_open)
						opts.after_open(notify);

					notify.queue_position();

					// Now set it to hide.
					if (opts.hide)
						notify.queue_remove();
				});
			};

			// Remove the notice.
			notify.dispose = function() {
				if (notify.timer) {
					window.clearTimeout(notify.timer);
					notify.timer = null;
				}
				// Run callback.
				if (opts.before_close) {
					if (opts.before_close(notify) === false)
						return;
				}
				notify.animate_out(function(){
					if (opts.after_close) {
						if (opts.after_close(notify) === false)
							return;
					}
					notify.queue_position();
					// If we're supposed to remove the notice from the DOM, do it.
					if (opts.dispose)
						notify.detach();
				});
			};

			// Animate the notice in.
			notify.animate_in = function(callback){
				// Declare that the notice is animating in. (Or has completed animating in.)
				animating = "in";
				var animation;
				if (typeof opts.animation.effect_in != "undefined")
					animation = opts.animation.effect_in;
				else
					animation = opts.animation;
				if (animation == "none") {
					notify.show();
					callback();
				} else if (animation == "show")
					notify.show(opts.animate_speed, callback);
				else if (animation == "fade")
					notify.show().fadeTo(opts.animate_speed, opts.opacity, callback);
				else if (animation == "slide")
					notify.slideDown(opts.animate_speed, callback);
				else if (typeof animation == "function")
					animation("in", callback, notify);
				else if (notify.effect)
					notify.effect(animation, {}, opts.animate_speed, callback);
			};

			// Animate the notice out.
			notify.animate_out = function(callback){
				// Declare that the notice is animating out. (Or has completed animating out.)
				animating = "out";
				var animation;
				if (typeof opts.animation.effect_out != "undefined")
					animation = opts.animation.effect_out;
				else
					animation = opts.animation;
				if (animation == "none") {
					notify.hide();
					callback();
				} else if (animation == "show")
					notify.hide(opts.animate_speed, callback);
				else if (animation == "fade")
					notify.fadeOut(opts.animate_speed, callback);
				else if (animation == "slide")
					notify.slideUp(opts.animate_speed, callback);
				else if (typeof animation == "function")
					animation("out", callback, notify);
				else if (notify.effect)
					notify.effect(animation, {}, opts.animate_speed, callback);
			};

			// Cancel any pending removal timer.
			notify.cancel_remove = function() {
				if (notify.timer)
					window.clearTimeout(notify.timer);
			};

			// Queue a removal timer.
			notify.queue_remove = function() {
				// Cancel any current removal timer.
				notify.cancel_remove();
				notify.timer = window.setTimeout(function(){
					notify.dispose();
				}, (isNaN(opts.delay) ? 0 : opts.delay));
			};

			// Provide a button to close the notice.
			notify.closer = $("<div />", {
				"class": "ui-notify-closer",
				"css": {"cursor": "pointer", "display": "none"},
				"click": function(){
					notify.dispose();
					notify.closer.hide();
				}
			})
			.append($("<span />", {"class": "ui-icon ui-icon-circle-close"}))
			.appendTo(notify.container);

			// Add the appropriate icon.
			if ((opts.error_icon && opts.type == "error") || (opts.notice_icon)) {
				$("<div />", {"class": "ui-notify-icon"})
				.append($("<span />", {"class": opts.type == "error" ? opts.error_icon : opts.notice_icon}))
				.appendTo(notify.container);
			}

			// Add a title.
			notify.title_container = $("<div />", {
				"class": "ui-notify-title",
				"html": opts.title
			})
			.appendTo(notify.container);
			if (opts.title === false)
				notify.title_container.hide();

			// Replace new lines with HTML line breaks.
			if (opts.insert_brs && typeof opts.text == "string")
				opts.text = opts.text.replace(/\n/g, "<br />");
			// Add text.
			notify.text_container = $("<div />", {
				"class": "ui-notify-text",
				"html": opts.text
			})
			.appendTo(notify.container);
			if (opts.text === false)
				notify.text_container.hide();

			// Set width and min height.
			if (typeof opts.width == "string")
				notify.css("width", opts.width);
			if (typeof opts.min_height == "string")
				notify.container.css("min-height", opts.min_height);

			// Add the notice to the notice array.
			var body_data = body.data("notify");
			if (body_data == null || typeof body_data != "object")
				body_data = [];
			if (opts.stack.push == "top")
				body_data = $.merge([notify], body_data);
			else
				body_data = $.merge(body_data, [notify]);
			body.data("notify", body_data);

			// Run callback.
			if (opts.after_init)
				opts.after_init(notify);

			// Mark the stack so it won't animate the new notice.
			opts.stack.animation = false;

			// Display the notice.
			notify.display();

			return notify;
		}
	});

	// Some useful regexes.
	var re_on = /^on/;
	var re_mouse_events = /^(dbl)?click$|^mouse(move|down|up|over|out|enter|leave)$|^contextmenu$/;
	var re_ui_events = /^(focus|blur|select|change|reset)$|^key(press|down|up)$/;
	var re_html_events = /^(scroll|resize|(un)?load|abort|error)$/;
	// Fire a DOM event.
	var dom_event = function(e, orig_e){
		var event_object;
		e = e.toLowerCase();
		if (document.createEvent && this.dispatchEvent) {
			// FireFox, Opera, Safari, Chrome
			e = e.replace(re_on, '');
			if (e.match(re_mouse_events)) {
				// This allows the click event to fire on the notice. There is
				// probably a much better way to do it.
				$(this).offset();
				event_object = document.createEvent("MouseEvents");
				event_object.initMouseEvent(
					e, orig_e.bubbles, orig_e.cancelable, orig_e.view, orig_e.detail,
					orig_e.screenX, orig_e.screenY, orig_e.clientX, orig_e.clientY,
					orig_e.ctrlKey, orig_e.altKey, orig_e.shiftKey, orig_e.metaKey, orig_e.button, orig_e.relatedTarget
				);
			} else if (e.match(re_ui_events)) {
				event_object = document.createEvent("UIEvents");
				event_object.initUIEvent(e, orig_e.bubbles, orig_e.cancelable, orig_e.view, orig_e.detail);
			} else if (e.match(re_html_events)) {
				event_object = document.createEvent("HTMLEvents");
				event_object.initEvent(e, orig_e.bubbles, orig_e.cancelable);
			}
			if (!event_object) return;
			this.dispatchEvent(event_object);
		} else {
			// Internet Explorer
			if (!e.match(re_on)) e = "on"+e;
			event_object = document.createEventObject(orig_e);
			this.fireEvent(e, event_object);
		}
	};

	$.notify.defaults = {
		// The notice's title.
		title: false,
		// The notice's text.
		text: false,
		// Additional classes to be added to the notice. (For custom styling.)
		addclass: "",
		// Create a non-blocking notice. It lets the user click elements underneath it.
		nonblock: false,
		// The opacity of the notice (if it's non-blocking) when the mouse is over it.
		nonblock_opacity: .2,
		// Width of the notice.
		width: "300px",
		// Minimum height of the notice. It will expand to fit content.
		min_height: "16px",
		// Type of the notice. "notice" or "error".
		type: "notice",
		// The icon class to use if type is notice.
		notice_icon: "ui-icon ui-icon-info",
		// The icon class to use if type is error.
		error_icon: "ui-icon ui-icon-alert",
		// The animation to use when displaying and hiding the notice. "none", "show", "fade", and "slide" are built in to jQuery. Others require jQuery UI. Use an object with effect_in and effect_out to use different effects.
		animation: "fade",
		// Speed at which the notice animates in and out. "slow", "def" or "normal", "fast" or number of milliseconds.
		animate_speed: "slow",
		// Opacity of the notice.
		opacity: 1,
		// Display a drop shadow.
		shadow: false,
		// Provide a button for the user to manually close the notice.
		closer: true,
		// After a delay, remove the notice.
		hide: true,
		// Delay in milliseconds before the notice is removed.
		delay: 8000,
		// Reset the hide timer if the mouse moves over the notice.
		mouse_reset: true,
		// Remove the notice's elements from the DOM after it is removed.
		dispose: true,
		// Change new lines to br tags.
		insert_brs: true,
		// The stack on which the notices will be placed. Also controls the direction the notices stack.
		stack: {"dir1": "down", "dir2": "left", "push": "bottom"}
	};
})(jQuery);