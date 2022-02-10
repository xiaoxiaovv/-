/**
 * AJAX File Upload
 * http://github.com/davgothic/AjaxFileUpload
 * 
 * Copyright (c) 2010-2013 David Hancock (http://davidhancock.co)
 *
 * Thanks to Steven Barnett for his generous contributions
 *
 * Licensed under the MIT license ( http://www.opensource.org/licenses/MIT )
 */

;(function($) {
	$.fn.AjaxFileUpload = function(options) {
		
		var defaults = {
			action:     "upload.php",
			onChange:   function(filename) {},
			onSubmit:   function(filename) {},
			onComplete: function(filename, response) {}
		},
		settings = $.extend({}, defaults, options),
		randomId = (function() {
			var id = 0;
			return function () {
				return "_AjaxFileUpload" + id++;
			};
		})();
		
		return this.each(function() {
			var $this = $(this);
			if ($this.is("input") && $this.attr("type") === "file") {
				$this.bind("change", onChange);
			}
		});
		
		function onChange(e) {
			var $element = $(e.target),
				id       = $element.attr('id'),
				$clone   = $element.removeAttr('id').clone().attr('id', id).AjaxFileUpload(options),
				filename = $element.val().replace(/.*(\/|\\)/, "");

            // We append a clone since the original input will be destroyed
            $clone.insertBefore($element);

            settings.onChange.call($clone[0], filename);

            var input = $clone[0];
            if(input.files.length <= 0){
                input = this;
            }

            //特殊处理，上传文稿时，最大500k
            var maxSize = 2048; //kb
			var tipMsg = '请将文件大小控制在2M以内！';
			if(settings.action == '/api/compile/material/import'){
				maxSize = 500;
				tipMsg = '请将文件大小控制在500K以内！'
			}

            if(input.files.length > 0){
                //读取图片数据
                var f = input.files[0];
                var reader = new FileReader();
                reader.onload = function (e) {
                    var iframe   = createIframe(),
                        form     = createForm(iframe);
                    var size = Math.ceil(f.size / 1024);
                    if(size > maxSize) {
                        bjj.dialog.alert('danger', tipMsg);
                        return;
                    } else {
                        iframe.bind("load", {element: $clone, form: form, filename: filename}, onComplete);
                        form.append($element).bind("submit", {element: $clone, iframe: iframe, filename: filename}, onSubmit).submit();
                    }
                };
                reader.readAsDataURL(f);
            }
		}
		
		function onSubmit(e) {
			var data = settings.onSubmit.call(e.data.element, e.data.filename);

			// If false cancel the submission
			if (data === false) {
				// Remove the temporary form and iframe
				$(e.target).remove();
				e.data.iframe.remove();
				return false;
			} else {
				// Else, append additional inputs
				for (var variable in data) {
					$("<input />")
						.attr("type", "hidden")
						.attr("name", variable)
						.val(data[variable])
						.appendTo(e.target);
				}
			}
		}
		
		function onComplete (e) {
			var $iframe  = $(e.target),
				doc      = ($iframe[0].contentWindow || $iframe[0].contentDocument).document,
				response = doc.body.innerText;
			if(response.indexOf('413 Request Entity Too Large') > -1) {
                response = '{"status":413,"msg":"请将文件大小控制在2M以内！"}';
            }
			if (response) {
				response = $.parseJSON(response);
			} else {
				response = {};
			}

			settings.onComplete.call(e.data.element, e.data.filename, response);
			
			// Remove the temporary form and iframe
			e.data.form.remove();
			$iframe.remove();
		}

		function createIframe() {
			var id = randomId();

			// The iframe must be appended as a string otherwise IE7 will pop up the response in a new window
			// http://stackoverflow.com/a/6222471/268669
			$("body")
				.append('<iframe src="javascript:false;" name="' + id + '" id="' + id + '" style="display: none;"></iframe>');

			return $('#' + id);
		}
		
		function createForm(iframe) {
			return $("<form />")
				.attr({
					method: "post",
					action: settings.action,
					enctype: "multipart/form-data",
					target: iframe[0].name
				})
				.hide()
				.appendTo("body");
		}
	};
})(jQuery);
