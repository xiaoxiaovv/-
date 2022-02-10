/**
 * Author : YCSnail
 * Date : 2017-07-26
 * Email : liyancai1986@163.com
 */
var accountSiteAddCtrl = function ($rootScope, $scope, $http, $timeout) {
    $(".site-list ul li").removeClass("active");
    $rootScope.fromRouter = true;
    $scope.siteTypeList = [
        {'siteTypeName': '网站', value: '1'},
        {'siteTypeName': '公众号', value: '2'},
        {'siteTypeName': '微博', value: '3'}
    ];
    $rootScope.newAddSite = function () {
        if ($scope.siteType == 1) {
            $scope.siteFlag = true;
            $scope.sitePublic = false;
            $scope.weiboSite = false;
        } else if ($scope.siteType == 2){
            $scope.siteFlag = false;
            $scope.sitePublic = true;
            $scope.weiboSite = false;
        } else if ($scope.siteType == 3){
            $scope.siteFlag = false;
            $scope.sitePublic = false;
            $scope.weiboSite = true;
        }
        $scope.siteName = "";
        $scope.websiteName = "";
        $scope.websiteDomain = "";
        $scope.webPublicName = "";
        $scope.webPublicDomain = "";
        $scope.weiboPublicName = '';
        $scope.weiboPublicDomain = '';
    };
    $scope.siteFlag = true;
    $scope.siteName = "";
    $scope.websiteName = "";
    $scope.websiteDomain = "";
    $scope.webPublicName = "";
    $scope.webPublicDomain = "";
    $scope.weiboPublicName = '';
    $scope.weiboPublicDomain = '';
    /*辅助输入*/
    /*url*/
    bjj.input.autocomplete('#site-url', {
        serviceUrl: '/api/capture/site/urlSuggestion?siteType=1',
        paramName: 'url'
    }, function (value, data) {
        $('#site-url').val(value).change();
        $('#siteInputName').val(data).change();
        $('input[name=siteName]').val(data).change();
        return;
    });
    /*公众号*/
    bjj.input.autocomplete('#exampleInputName', {
        serviceUrl: '/api/capture/site/urlSuggestion?siteType=2',
        paramName: 'name'
    }, function (value, data) {
        $('#exampleInputName').val(value).change();
        $('input[name=publicName]').val(data).change();
        return;
    });
    /*微博*/
    bjj.input.autocomplete('#exampleInputWeiboName', {
        serviceUrl: '/api/capture/site/urlSuggestion?siteType=3',
        paramName: 'name'
    }, function (value, data) {
        $('#exampleInputWeiboName').val(value).change();
        $('input[name=weiboName]').val(data).change();
        return;
    });
    $scope.typeChange = function () {
        if ($scope.siteType == 1) {
            $scope.siteFlag = true;
            $scope.sitePublic = false;
            $scope.weiboSite = false;
        } else if ($scope.siteType == 2){
            $scope.siteFlag = false;
            $scope.sitePublic = true;
            $scope.weiboSite = false;
        }else if ($scope.siteType == 3){
            $scope.siteFlag = false;
            $scope.sitePublic = false;
            $scope.weiboSite = true;
        }
    }
    /*手动输入的保存*/
    $scope.saveSite = function () {
        var websiteName;
        var websiteDomain;
        if ($scope.siteType == 1) {
            websiteName = $scope.websiteName;
            websiteDomain = $scope.siteName
        } else if ($scope.siteType == 2){
            websiteName = $scope.webPublicName;
            websiteDomain = $scope.webPublicDomain
        }else if ($scope.siteType == 3){
            websiteName = $scope.weiboPublicName;
            websiteDomain = $scope.weiboPublicDomain
        }
        bjj.http.ng.post($scope, $http, '/api/capture/site?_method=post', {
            siteName: websiteDomain,
            websiteName: websiteName,
            websiteDomain: $scope.websiteDomain,
            siteType: $scope.siteType
        }, function () {
            bjj.dialog.alert('success', '添加站点成功');
            $('.bjj-account-save-view .btn-primary').attr("disabled", true);
            setTimeout(function() {
                $('.bjj-account-save-view .btn-primary').attr("disabled", false);
            }, 500);
            $rootScope.allSiteActive = false;
            $rootScope.AllSiteIds = [];
            $rootScope.siteType.addSiteType = '';
            $rootScope.siteType.keywords = '';
            bjj.http.ng.get($scope, $http, '/api/capture/sites', {}, function (res) {
                $timeout(function() {
                    $rootScope.siteList = res.msg;
                },0)
            })
        }, function (res) {
            bjj.dialog.alert('danger', res.msg);
        })
    };
    /*分类选择*/
    // 行业
    bjj.http.ng.get($scope, $http, '/api/capture/site/classification', {
        flag : 1
    }, function (res) {
        $scope.classification = res.msg;
    });
    //地域
    bjj.http.ng.get($scope, $http, '/api/capture/site/area', {}, function (res) {
        $scope.area = res.msg;
    });
    //网站资质
    bjj.http.ng.get($scope, $http, '/api/capture/site/attr', {}, function (res) {
        $scope.attr = res.msg;
    });
    $scope.classificationType = "";
    $scope.areaType = "";
    $scope.attrType = "";
    $scope.sitesType = '';
    var order = '';
    $scope.classificationType = '综合';
    getSiteListData($scope, $http, order);
    $scope.getData = function () {
        $scope.active = false;
        var order = '';
        angular.forEach($scope.choiceType, function (v,i) {
            if(v.active == true) {
                v.active = false;
                $scope.site = _.filter($scope.choiceType, {'active' : true});
                $scope.sites = _.filter($scope.site, {'isHave' : false});
            }
        });
        getSiteListData($scope, $http, order);
    };
    $scope.nameSort = function () {
        $(".areaSort span").removeClass("keyColor");
        $(".subClassificationSort span").removeClass("keyColor");
        $(".siteTypeSort span").removeClass("keyColor");
        if ($(".siteSort .caret").hasClass("keyColor")) {
            var order = 1;
            $(".siteSort .caret").removeClass("keyColor").siblings().addClass("keyColor");
            getSiteListData($scope, $http, order);
        } else {
            var order = 2;
            $(".siteSort .caret").addClass("keyColor").siblings().removeClass("keyColor");
            getSiteListData($scope, $http, order);
        }
    };

    $scope.areaSort = function () {
        $(".siteSort span").removeClass("keyColor");
        $(".subClassificationSort span").removeClass("keyColor");
        $(".siteTypeSort span").removeClass("keyColor");
        if ($(".areaSort .caret").hasClass("keyColor")) {
            var order = 3;
            $(".areaSort .caret").removeClass("keyColor").siblings().addClass("keyColor");
            getSiteListData($scope, $http, order);
        } else {
            var order = 4;
            $(".areaSort .caret").addClass("keyColor").siblings().removeClass("keyColor");
            getSiteListData($scope, $http, order);
        }
    };

    $scope.attrSort = function () {
        $(".areaSort span").removeClass("keyColor");
        $(".siteSort span").removeClass("keyColor");
        $(".siteTypeSort span").removeClass("keyColor");
        if ($(".subClassificationSort .caret").hasClass("keyColor")) {
            $(".subClassificationSort .caret").removeClass("keyColor").siblings().addClass("keyColor");
            var order = 5;
            getSiteListData($scope, $http, order);
        } else {
            var order = 6;
            $(".subClassificationSort .caret").addClass("keyColor").siblings().removeClass("keyColor");
            getSiteListData($scope, $http, order);
        }
    };

    $scope.siteTypeSort = function () {
        $(".areaSort span").removeClass("keyColor");
        $(".subClassificationSort span").removeClass("keyColor");
        $(".siteSort span").removeClass("keyColor");
        if ($(".siteTypeSort .caret").hasClass("keyColor")) {
            var order = 7;
            $(".siteTypeSort .caret").removeClass("keyColor").siblings().addClass("keyColor");
            getSiteListData($scope, $http, order);
        } else {
            var order = 8;
            $(".siteTypeSort .caret").addClass("keyColor").siblings().removeClass("keyColor");
            getSiteListData($scope, $http, order);
        }
    };
    /*单选*/
    $scope.toggleSites = function () {
        this.item.active = !this.item.active;
        angular.forEach($scope.choiceType, function (v, i) {
            if (v.isHave == true) {
                v.active = true;
                $(".disabled").unbind();
            }
        });
        $scope.active = false;
        $scope.site = _.filter($scope.choiceType, {'active': true});
        $scope.sites = _.filter($scope.site, {'isHave': false});
        if ($scope.sites.length == $scope.choiceType.length) {
            $scope.active = true;
        }
    };

    /*全选*/
    $scope.toggleAllSites = function () {
        $scope.active = !$scope.active;
        angular.forEach($scope.choiceType, function (v, i) {
            if ($scope.active) {
                v.active = true;
            } else {
                v.active = undefined;
            }
            if (v.isHave == true) {
                v.active = true;
                $(".disabled").unbind();
            }
        });
        $scope.site = _.filter($scope.choiceType, {'active': true});
        $scope.sites = _.filter($scope.site, {'isHave': false});
    };
    /*加入站点*/
    $scope.addSite = function () {
        var siteIds = [];
        if ($scope.sites == undefined || $scope.sites.length == 0) {
            bjj.dialog.alert('danger', '请选择要加入的站点！');
            return;
        }
        if ($scope.sites.length > 0) {
            _.forEach($scope.sites, function (v, i) {
                siteIds.push(v.id)
            });
            bjj.http.ng.post($scope, $http, '/api/capture/site/batch?_method=post', {
                siteDetailIds: siteIds.join(','),
            }, function (res) {
                $scope.active = false;
                bjj.dialog.alert('success', res.msg);
                $rootScope.allSiteActive = false;
                $rootScope.AllSiteIds = [];
                $rootScope.siteType.addSiteType = '';
                $rootScope.siteType.keywords = '';
                bjj.http.ng.get($scope, $http, '/api/capture/sites', {}, function (res) {
                    $timeout(function(){
                         $rootScope.siteList = res.msg;
                    },0)
                });
                angular.forEach($scope.choiceType, function (v, i) {
                    v.active = undefined;
                });
                var order = '';
                getSiteListData($scope, $http, order);
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            })
        }
    };

    /*批量导入*/
    $scope.downLoadClick = function () {
        var url = '/api/capture/site/template';
        window.open(url)
    }
    /*批量下载*/
    $("#exportFile").AjaxFileUpload({
        action: "/api/capture/site/import",
        onComplete: function (filename, response) {

            $("#avatar").val(response.siteIds);
            if (response.status == 200) {
                $(".more-module span").text(filename)
                bjj.dialog.alert('success', response.msg, {aliveTime: 2000});
                $rootScope.allSiteActive = false;
                $rootScope.AllSiteIds = [];
                $rootScope.siteType.addSiteType = '';
                $rootScope.siteType.keywords = '';
                bjj.http.ng.get($scope, $http, '/api/capture/sites', {}, function (res) {
                    $timeout(function(){
                        $rootScope.siteList = res.msg;
                    },0)
                });
            } else {
                bjj.dialog.alert('danger', response.msg);
            }
        }
    });
};
var getSiteListData = function ($scope, $http, order) {
    bjj.http.ng.get($scope, $http, '/api/capture/site/recommendation?classification=' + encodeURIComponent($scope.classificationType) + '&area=' + encodeURIComponent($scope.areaType) + '&attr=' + encodeURIComponent($scope.attrType) + '&siteType=' + encodeURIComponent($scope.sitesType), {
        order: order == '' ? '' : order
    }, function (res) {
        $scope.choiceType = res.list;
        angular.forEach(res.list, function (v, i) {
            if (v.isHave == true) {
                v.active = true;
            } else {
                v.active = undefined;
            }
        });
    });
};
