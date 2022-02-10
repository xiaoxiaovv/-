/**
 * 刷新页面
 */
var reloadPage = function(clearCache){
    window.location.reload(clearCache);
};

/**
 * 跳转到其他页
 */
var goto = function(url) {
    window.location.href = url;
};
/**
 * 获取地址栏参数
 */
var getUrlParam = function(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;
};
var setUrlHash = function(key, value) {
    var reg = new RegExp("(^|&)" + key + "=([^&]*)(&|$)", "i");
    var hash = window.location.hash.substr(1).replace(reg, '');
    if(hash != '') hash += '&';
    window.location.hash = hash + key + '=' + value;
};
var getUrlHash = function(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.hash.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;
};
var clearUrlHash = function(){
    window.location.hash = '';
};
/**
 * 密码验证
 */
var isPassword = function(passWord) {
    if(!passWord){
        return 0;
    }
    var length = 0;
    var isLetterReg = /[A-Za-z]/i;
    var isNumReg = /[0-9]/;
    var isCharacterReg = new RegExp("[`~_]");
    var specialReg = new RegExp("[`~!@#$^&*%()+=|{}':;',\\-\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]")
    for (var i=0; i<passWord.length; i++) {
        var char = passWord.charCodeAt(i);
        if ((char >= 0x0001 && char <= 0x007e) || (0xff60 <= char && char <= 0xff9f))
        {
            length++;
        }
        else
        {
            length+=2;
        }
    }
    if( 5 < length && length < 19) {
        if(!specialReg.test(passWord)) {
            if (!isNumReg.test(passWord)) {
                if (!isLetterReg.test(passWord)) {
                    if(!isCharacterReg.test(passWord)) {
                        return '0'
                    }else {
                        return '1'
                    }
                }else {
                    if(!isCharacterReg.test(passWord)) {
                        return '1'
                    }else {
                        return '2'
                    }
                }
            }else {
                if (!isLetterReg.test(passWord)) {
                    if(!isCharacterReg.test(passWord)) {
                        return '1'
                    }else {
                        return '2'
                    }
                } else {
                    if(!isCharacterReg.test(passWord)) {
                        return '2'
                    }else {
                        return '3'
                    }
                }
            }
        }else {
            return '0'
        }
    }else {
        return '-1'
    }
}

/**
 * syntax: bjj.http.ng.get($http, $scope, url, params, successCallBackFunction (res) {}, failureCallBackFunction() {}, loadingID);
 * syntax: bjj.http.jq.get(url, params, successCallBackFunction (res) {}, failureCallBackFunction() {}, loadingID);
 * eg: bjj.http.ng.get($http, $scope, '/api/capture/news', {id: 1}, function (res) {});
 * eg: bjj.http.ng.get($http, $scope, '/api/capture/news', {id: 1}, function (res) {}, 'loading');
 * eg: bjj.http.ng.get($http, $scope, '/api/capture/news', {id: 1}, function (res) {}, function () {});
 * eg: bjj.http.ng.get($http, $scope, '/api/capture/news', {id: 1}, function (res) {}, function () {}, 'loading');
 * eg: bjj.http.jq.get('/api/capture/news', {id: 1}, function (res) {});
 * eg: bjj.http.jq.get('/api/capture/news', {id: 1}, function (res) {}, 'loading');
 * eg: bjj.http.jq.get('/api/capture/news', {id: 1}, function (res) {}, function () {});
 * eg: bjj.http.jq.get('/api/capture/news', {id: 1}, function (res) {}, function () {}, 'loading');
 */
var bjj = {};
if(bjj.http === undefined) {
    bjj.http = {};
}
bjj.http.ng = {};
bjj.http.ng.doRequest = function (method) {
    return function ($scope, $http, url, params, onSuccess, onFailure, loadingID) {
        if(onFailure && (typeof onFailure != 'function') && loadingID == undefined){
            loadingID = onFailure;
        }
        if (loadingID != undefined) {
            $scope[loadingID] = true;
        }
        $http({
            method  : method,
            url     : url,
            params  : params
        }).then(function (response) {
            if (loadingID != undefined) {
                $scope[loadingID] = false;
            }
            var res = response.data;
            if (res.status == 200) {
                if(onSuccess && (typeof onSuccess == 'function')){
                    onSuccess(res);
                }
            } else if(res.status == 401) {
                goto('/');
            } else if(res.status == 423) {
                if(_.isEmpty(document.referrer)){
                    goto('/');
                    return;
                }
                var _dialog = $('.yc-dialog-info');
                if(_dialog.length <= 0) {
                    bjj.dialog.info(res.msg, function () {
                        goto('/');
                    });
                }
            } else {
                if(onFailure && (typeof onFailure == 'function')){
                    onFailure(res);
                }
            }
        }, function (data) {
            console.log('error:' + method + ':' + url);
        });
    };
};
bjj.http.ng.doBodyRequest = function (method) {
    return function ($scope, $http, url, paramsStr, onSuccess, onFailure, loadingID) {
        if(onFailure && (typeof onFailure != 'function') && loadingID == undefined){
            loadingID = onFailure;
        }
        if (loadingID != undefined) {
            $scope[loadingID] = true;
        }
        $http({
            method  : method,
            url     : url,
            data    : paramsStr
        }).then(function (response) {
            if (loadingID != undefined) {
                $scope[loadingID] = false;
            }
            var res = response.data;
            if (res.status == 200) {
                if(onSuccess && (typeof onSuccess == 'function')){
                    onSuccess(res);
                }
            } else if(res.status == 401) {
                goto('/');
            } else if(res.status == 423) {
                if(_.isEmpty(document.referrer)){
                    goto('/');
                    return;
                }
                var _dialog = $('.yc-dialog-info');
                if(_dialog.length <= 0) {
                    bjj.dialog.info(res.msg, function () {
                        goto('/');
                    });
                }
            } else {
                if(onFailure && (typeof onFailure == 'function')){
                    onFailure(res);
                }
            }
        }, function (data) {
            console.log('error:' + method + ':' + url);
        });
    };
};
bjj.http.ng.doRequestWithRetry = function (method) {
    return function ($scope, $http, url, params, onSuccess, onFailure, loadingID) {
        if(onFailure && (typeof onFailure != 'function') && loadingID == undefined){
            loadingID = onFailure;
        }
        if (loadingID != undefined) {
            $scope[loadingID] = true;
            $scope[loadingID + 'Retry'] = false;
        }
        $http({
            method  : method,
            url     : url,
            params  : params,
            timeout : 20000
        }).then(function (response) {
            if (loadingID != undefined) {
                $scope[loadingID] = false;
            }
            var res = response.data;
            if (res.status == 200) {
                if(onSuccess && (typeof onSuccess == 'function')){
                    onSuccess(res);
                }
            } else if(res.status == 401) {
                goto('/');
            } else {
                if(onFailure && (typeof onFailure == 'function')){
                    onFailure();
                }
            }
        }, function () {
            if (loadingID != undefined) {
                $scope[loadingID] = false;
                $scope[loadingID + 'Retry'] = true;
            }
        });
    };
};
bjj.http.ng = {
    get     : bjj.http.ng.doRequest('GET'),
    post    : bjj.http.ng.doRequest('POST'),
    put     : bjj.http.ng.doRequest('PUT'),
    del     : bjj.http.ng.doRequest('DELETE'),
    postBody: bjj.http.ng.doBodyRequest('POST'),
    putBody : bjj.http.ng.doBodyRequest('PUT'),
    getWithRetry : bjj.http.ng.doRequestWithRetry('GET')
};

bjj.http.jq = {};
bjj.http.jq.doRequest = function (method) {
    return function (url, params, onSuccess, onFailure, loadingID) {
        if(onFailure && (typeof onFailure != 'function') && loadingID == undefined){
            loadingID = onFailure;
        }
        if (loadingID != undefined) {
            $('.' + loadingID).show();
        }
        $.ajax({
            type    : method,
            url     : url,
            dataType: 'json',
            data    : params
        }).done(function (data) {
            if (data.status == 200) {
                if(onSuccess && (typeof onSuccess == 'function')){
                    onSuccess(data);
                }
            } else if(data.status == 401) {
                goto('/');
            } else {
                if(onFailure && (typeof onFailure == 'function')){
                    onFailure();
                }
            }
        }).fail(function (data) {
            console.log('error');
        }).always(function (data) {
            if (loadingID != undefined) {
                $('.' + loadingID).hide();
            }
        });
    };
};
bjj.http.jq.doRequestWithRetry = function (method) {
    return function (url, params, onSuccess, onFailure, loadingID) {
        if(onFailure && (typeof onFailure != 'function') && loadingID == undefined){
            loadingID = onFailure;
        }
        if (loadingID != undefined) {
            $('.' + loadingID).show();
            $('.' + loadingID + 'Retry').hide();
        }
        $.ajax({
            type    : method,
            url     : url,
            timeout : 20000,
            dataType: 'json',
            data    : params
        }).done(function (data) {
            if (data.status == 200) {
                if(onSuccess && (typeof onSuccess == 'function')){
                    onSuccess(data);
                }
            } else if(data.status == 401) {
                goto('/');
            } else {
                if(onFailure && (typeof onFailure == 'function')){
                    onFailure();
                }
            }
        }).fail(function (XMLHttpRequest, status) {
            if (loadingID != undefined) {
                $('.' + loadingID + 'Retry').show();
            }
        }).always(function (XMLHttpRequest, status) {
            if (loadingID != undefined) {
                $('.' + loadingID).hide();
            }
        });
    };
};
bjj.http.jq = {
    get     : bjj.http.jq.doRequest('GET'),
    post    : bjj.http.jq.doRequest('POST'),
    put     : bjj.http.jq.doRequest('PUT'),
    del     : bjj.http.jq.doRequest('DELETE'),
    getWithRetry    : bjj.http.jq.doRequestWithRetry('GET')
};

/* 上拉加载 */
if(bjj.scroll === undefined) {
    bjj.scroll = {};
}
bjj.scroll.loadList = {};
bjj.scroll.loadList.init = function ($scope, $http, container, scrollPage, loadListFunction, pageSize) {
    $scope.pno = 1;
    $scope.psize = pageSize;
    $scope.canLoad = true;
    $scope.dataList = [];
    $scope.dataListBottomFinalView = false;
    $scope.dataListEmptyView = false;

    var _container = $(container);
    var _scrollPage = $(scrollPage);
    _container.scroll(function () {
        var totalHeight = parseFloat(_container.height()) + parseFloat(_container.scrollTop()) + 400;
        var docHeight = parseFloat(_scrollPage.height());
        if(docHeight <= totalHeight && $scope.canLoad) {
            $scope.canLoad = false;
            loadListFunction($scope, $http)
        }
    });
};
bjj.scroll.loadList.callback = function ($scope, list) {
    if($scope.pno == 1) {
        $scope.dataList = [];
        $scope.dataListBottomFinalView = false;
        $scope.dataListEmptyView = false;
    }
    if(list.length > 0) {
        $scope.canLoad = true;
        $scope.dataList = $scope.dataList.concat(list);
        $scope.pno += 1;
        if(list.length < $scope.psize) {
            $scope.canLoad = false;
            $scope.dataListBottomFinalView = true;
        }
    } else {
        $scope.canLoad = false;
        if($scope.pno != 1){
            $scope.dataListBottomFinalView = true;
        } else {
            $scope.dataListEmptyView = true;
        }
    }
};


/* 弹窗 */
/**
 * eg:
 * alert:
 * bjj.dialog.alert('info|warning|danger|success', '文本文字'});
 * bjj.dialog.alert('info|warning|danger|success', '文本文字', { hideFlag: true|false, aliveTime: 1000, callback: function(){alert(1);}}});
 * confirm:
 * bjj.dialog.confirm('确定删除？', function(){alert(1);});
 */
if(bjj.dialog === undefined) {
    bjj.dialog = {};
}
bjj.dialog.alert = function (type, text, config) {

    var _dialog = $('.yc-dialog-alert');
    if(_dialog.length > 0) { _dialog.remove(); }

    var _icon = 'fa-exclamation-circle';
    if(type == 'success') {
        _icon = 'fa-check-circle';
    } else if(type == 'danger') {
        _icon = 'fa-times-circle';
    } else if(type == 'warning') {
        _icon = 'fa-exclamation-circle';
    } else if(type == 'info') {
        _icon = 'fa-info-circle';
    }

    $("body").append("" +
        "<div class=\"modal fade yc-dialog-alert in\" tabindex=\"-1\" role=\"dialog\">\n" +
        "    <div class=\"modal-dialog\" role=\"document\">\n" +
        "        <div class=\"modal-content\">\n" +
        "           <div class=\"alert alert-" + type + " alert-dismissible\" role=\"alert\" style=\"margin: 0;\">\n" +
        "               <button type=\"button\" class=\"close\" aria-label=\"Close\" data-dismiss=\"modal\">" +
        "                   <span aria-hidden=\"true\">&times;</span>" +
        "               </button><i class=\"fa " + _icon + "\" aria-hidden=\"true\"></i> " + text +
        "           </div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "</div>"
    );

    config = config || {};
    if(config.callback && (typeof config.callback == 'function')){
        $('.yc-dialog-alert').on('hidden.bs.modal', function (event) {
            config.callback();
        });
    }

    setTimeout(function () {
        $('.yc-dialog-alert').modal();
        if(config.aliveTime == undefined || config.aliveTime == null) {
            if(type == 'success') {
                config.aliveTime = 500;
            } else if(type == 'danger') {
                config.aliveTime = 1000;
            } else if(type == 'warning') {
                config.aliveTime = 1000;
            } else if(type == 'info') {
                config.aliveTime = 1000;
            }
        }
        bjj.dialog.autoHide(config.hideFlag, config.aliveTime);
    }, 0);
};
bjj.dialog.autoHide = function(hideFlag, aliveTime){
    if(hideFlag == undefined || hideFlag == null){
        hideFlag = true;
    }
    if(hideFlag){
        if(aliveTime == undefined || aliveTime == null){
            aliveTime = 1000;
        }
        setTimeout(bjj.dialog.cancel, aliveTime);
    }
};
/**
 * 取消弹出框
 */
bjj.dialog.cancel = function(){
    $('.yc-dialog-alert').modal('hide');
};

bjj.dialog.confirm = function(text, trueCallback, falseCallback){

    var _dialog = $('.yc-dialog-confirm');
    if(_dialog.length > 0) { _dialog.remove(); }

    $("body").append("" +
        "<div class=\"modal fade in yc-dialog-confirm\" tabindex=\"-1\" role=\"dialog\">\n" +
        "    <div class=\"modal-dialog\" role=\"document\">\n" +
        "        <div class=\"modal-content\">\n" +
        "            <div class=\"modal-header\">\n" +
        "                <button type=\"button\" class=\"close\" aria-label=\"Close\" data-dismiss=\"modal\">" +
        "                    <span aria-hidden=\"true\">&times;</span>" +
        "                </button>" + "<h5 class=\"modal-title\">提示</h5>\n" +
        "            </div>\n" +
        "            <div class=\"modal-body\">\n" +
        "                <div class=\"yc-dialog-confirm-text\"><i class=\"fa fa-exclamation-circle\" aria-hidden=\"true\"></i> " + text + "</div>\n" +
        "            </div>\n" +
        "            <div class=\"modal-footer\">\n" +
        "                <button type=\"button\" class=\"btn btn-default btn-sm\" data-dismiss=\"modal\">否</button>\n" +
        "                <button type=\"button\" class=\"btn btn-primary btn-sm\">是</button>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "</div>"
    );

    if(trueCallback && (typeof trueCallback == 'function')){
        $('.yc-dialog-confirm .btn-primary').on('click', function (event) {
            trueCallback();
            $('.yc-dialog-confirm').unbind('hidden.bs.modal').modal('hide');
        });
    }

    if(falseCallback && (typeof falseCallback == 'function')){
        $('.yc-dialog-confirm').on('hidden.bs.modal', function (event) {
            falseCallback();
        });
        $('.yc-dialog-confirm .btn-default').on('click', function (event) {
            $('.yc-dialog-confirm').modal('hide');
        });
    }

    setTimeout(function () {
        $('.yc-dialog-confirm').modal();
    }, 0);
};
bjj.dialog.info = function(text, callback){

    var _dialog = $('.yc-dialog-info');
    if(_dialog.length > 0) { _dialog.remove(); }

    $("body").append("" +
        "<div class=\"modal fade in yc-dialog-info\" tabindex=\"-1\" role=\"dialog\">\n" +
        "    <div class=\"modal-dialog\" role=\"document\">\n" +
        "        <div class=\"modal-content\">\n" +
        "            <div class=\"modal-header\">\n" +
        "                <button type=\"button\" class=\"close\" aria-label=\"Close\" data-dismiss=\"modal\">" +
        "                    <span aria-hidden=\"true\">&times;</span>" +
        "                </button>" + "<h5 class=\"modal-title\">系统提示</h5>\n" +
        "            </div>\n" +
        "            <div class=\"modal-body\">\n" +
        "                <div class=\"yc-dialog-info-text\"><i class=\"fa fa-exclamation-circle\" aria-hidden=\"true\"></i> " + text + "</div>\n" +
        "            </div>\n" +
        "            <div class=\"modal-footer\">\n" +
        "                <button type=\"button\" class=\"btn btn-primary btn-sm\">确定</button>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "</div>"
    );

    if(callback && (typeof callback == 'function')){
        var _dialog = $('.yc-dialog-info');
        _dialog.find('.btn-primary').on('click', function (event) {
            callback();
            _dialog.modal('hide');
        });
        _dialog.on('hidden.bs.modal', function (event) {
            callback();
        });
    }

    setTimeout(function () {
        $('.yc-dialog-info').modal();
    }, 0);
};
bjj.dialog.systemInfo = function(text, callback){

    var _dialog = $('.yc-dialog-system-info');
    if(_dialog.length > 0) { _dialog.remove(); }

    $("body").append("" +
        "<div class=\"modal fade in yc-dialog-system-info\" tabindex=\"-1\" role=\"dialog\">\n" +
        "    <div class=\"modal-dialog\" role=\"document\">\n" +
        "        <div class=\"modal-content\">\n" +
        "            <div class=\"modal-header\">\n" +
        "                <button type=\"button\" class=\"close\" aria-label=\"Close\" data-dismiss=\"modal\">" +
        "                    <span aria-hidden=\"true\">&times;</span>" +
        "                </button>" + "<h5 class=\"modal-title\">系统提示</h5>\n" +
        "            </div>\n" +
        "            <div class=\"modal-body\">\n" +
        "                <div class=\"yc-dialog-info-text\">" + text + "</div>\n" +
        "            </div>\n" +
        "            <div class=\"modal-footer\">\n" +
        "                <button type=\"button\" class=\"btn btn-primary btn-sm\">确定</button>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "</div>"
    );

    if(callback && (typeof callback == 'function')){
        $('.yc-dialog-system-info .btn-primary').on('click', function (event) {
            callback();
            $('.yc-dialog-system-info').modal('hide');
        });
    }

    setTimeout(function () {
        $('.yc-dialog-system-info').modal();
    }, 0);
};

/* 输入框 */
if(bjj.input === undefined) {
    bjj.input = {};
}
/*
 * 自动完成
 */
bjj.input.autocomplete = function (selector, setting, onSelectCallback, onHideCallback) {

    var _serviceUrl = setting.serviceUrl || '/api/capture/site/urlSuggestion';
    var _paramName  = setting.paramName || 'url';
    var _appendTo   = setting.appendTo || 'body';

    $(selector).autocomplete({
        serviceUrl  : _serviceUrl,
        dataType    : 'json',
        paramName   : _paramName,
        appendTo    : _appendTo,
        autoSelectFirst : true,
        transformResult : function(response) {
            return {
                suggestions: $.map(response.msg, function(dataItem) {
                    if(_paramName == 'url') {
                        return { value: dataItem.url, data: dataItem.name};
                    } else {
                        return { value: dataItem.name, data: dataItem.url};
                    }
                })
            };
        },
        onSelect : function (suggestion) {
            if(onSelectCallback && (typeof onSelectCallback == 'function')){
                onSelectCallback(suggestion.value, suggestion.data);
            }
        },
        onHide : function (container) {
            if(onHideCallback && (typeof onHideCallback == 'function')){
                onHideCallback(container);
            }
        }
    });
};
/*
* 时间格式化
 */
Date.prototype.Format = function(fmt) {
    var o = {
        "M+" : this.getMonth()+1,                 //月份
        "d+" : this.getDate(),                    //日
        "h+" : this.getHours(),                   //小时
        "m+" : this.getMinutes(),                 //分
        "s+" : this.getSeconds(),                 //秒
        "q+" : Math.floor((this.getMonth()+3)/3), //季度
        "S"  : this.getMilliseconds()             //毫秒
    };
    var week = {
        "0": "日",
        "1": "一",
        "2": "二",
        "3": "三",
        "4": "四",
        "5": "五",
        "6": "六"
    };
    var quarter = {
        "1": "一",
        "2": "二",
        "3": "三",
        "4": "四"
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    if (/(E+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, ((RegExp.$1.length > 1) ? (RegExp.$1.length > 2 ? "星期" : "周") : "") + week[this.getDay() + ""]);
    }
    if (/(q+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, ((RegExp.$1.length > 1) ? "第" : "") + quarter[Math.floor((this.getMonth() + 3) / 3) + ""] + "季度");
    }
    for(var k in o)
        if(new RegExp("("+ k +")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
    return fmt;
};

var os = function() {
    var ua = navigator.userAgent,
        isQB = /(?:MQQBrowser|QQ)/.test(ua),
        isWindowsPhone = /(?:Windows Phone)/.test(ua),
        isSymbian = /(?:SymbianOS)/.test(ua) || isWindowsPhone,
        isAndroid = /(?:Android)/.test(ua),     //通过这一句来判断是否是android系统有问题，魅族的自带浏览器不行。
        isFireFox = /(?:Firefox)/.test(ua),
        isChrome = /(?:Chrome|CriOS)/.test(ua),
        isIpad = /(?:iPad|PlayBook)/.test(ua),
        isTablet = /(?:iPad|PlayBook)/.test(ua)||(isFireFox && /(?:Tablet)/.test(ua)),
        isSafari = /(?:Safari)/.test(ua),
        isPhone = /(?:iPhone)/.test(ua) && !isTablet,
        isOpera= /(?:Opera Mini)/.test(ua),
        isUC = /(?:UCWEB|UCBrowser)/.test(ua),
        isPc = !isPhone && !isAndroid && !isSymbian;
    return {
        isSafari: isSafari,
        isQB: isQB,
        isTablet: isTablet,
        isPhone: isPhone,
        isAndroid : isAndroid,
        isPc : isPc,
        isOpera : isOpera,
        isUC: isUC,
        isIpad : isIpad
    };
}();

var _defaultCover = '/theme/default/images/default-img.jpg';
var _defaultAvatar = '/theme/default/images/default-avatar.jpg';