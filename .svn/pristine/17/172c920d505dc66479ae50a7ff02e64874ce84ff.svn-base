/**
 * Author : YCSnail
 * Date : 2017-07-26
 * Email : liyancai1986@163.com
 */
var accountSiteCtrl = function ($rootScope, $scope, $http, $state, $stateParams) {
    $rootScope.allSiteActive = false;
    $scope.siteListEmpty = false;
    $rootScope.AllSiteIds = [];
    $rootScope.siteType = {
        addSiteType : '',
        keywords: ''
    }
    if($rootScope.formPortal == true){
        $rootScope.minBar = true;
    }
    bjj.http.ng.get($scope, $http, '/api/capture/sites', {}, function (res) {
        $rootScope.siteList = res.msg;
        if($stateParams.captureSiteId) {
            if($stateParams.captureSiteId == -1 || $stateParams.captureSiteId == -2 || $stateParams.captureSiteId == -3) {
                setTimeout(function () {
                    $(".site-list ul li").eq(1).addClass("actively");
                    $state.go("accountSite.modify", { siteId: res.msg[0].siteId });
                }, 0);
            }else {
                _.forEach($rootScope.siteList, function(v, i) {
                    if(v.siteId == $stateParams.captureSiteId) {
                        setTimeout(function () {
                            $(".site-list ul li").eq(i+1).addClass("actively");
                            $state.go("accountSite.modify", { siteId: res.msg[i].siteId });
                        }, 0);
                    }
                })
            }
        }else{
            if($rootScope.siteList.length > 0){
                setTimeout(function () {
                    $(".site-list ul li").eq(1).addClass("actively");
                    if($rootScope.fromRouter) {}else {
                        $state.go("accountSite.modify", { siteId: res.msg[0].siteId });
                    }
                }, 0);
            }
        }
        $(".site-list .nav-list").on("click", "li", function() {
            $(this).addClass("actively").siblings().removeClass("actively");
        });
    },function() {
        $rootScope.siteList = [];
        $state.go("accountSite.add");
    },'');
    $scope.searchSite = function() {
        $rootScope.AllSiteIds = [];
        $rootScope.allSiteActive = false;
        _.forEach($scope.siteList, function(data) {
            data.active = false;
        })
        bjj.http.ng.get($scope, $http, '/api/capture/site/list', {
            siteType: $rootScope.siteType.addSiteType,
            siteName: $rootScope.siteType.keywords
        }, function (res) {
            $rootScope.siteList = res.list;
            if($rootScope.siteList.length > 0){
                $state.go("accountSite.modify", { siteId: res.list[0].siteId });
                setTimeout(function () {
                    $(".site-list ul li").eq(1).addClass("actively");
                }, 0);
            }else {
                $state.go("accountSite");
                $scope.siteListEmpty = true;
            }
        })
    }
    $scope.getSite = function() {
        this.item.active = !this.item.active;
        if($scope.siteList.every( item => item.active === true)) {
            $rootScope.allSiteActive = true;
        }else {
            $rootScope.allSiteActive = false;
            $rootScope.AllSiteIds = [];
        }
        _.forEach($scope.siteList, function(data) {
            if(data.active) {
                $rootScope.AllSiteIds.push(data.siteId)
            }
        })
    }
    $scope.allgetSite = function() {
        $rootScope.allSiteActive = !$rootScope.allSiteActive;
        $rootScope.AllSiteIds = [];
        _.forEach($scope.siteList, function(data) {
            if($rootScope.allSiteActive) {
                data.active = true;
                $rootScope.AllSiteIds.push(data.siteId)
            }else {
                data.active = false;
            }
        })
    }
    if($stateParams.pageType == 1) {
        $state.go("accountSite.add");
    }
    if($stateParams.pageType == 2) {
        $state.go("accountSite.modify");
    }
    $scope.siteId = '';
    $scope.transmit = function($event) {
        $rootScope.deleteFlag = false;
        var _this = $event.target;
        var siteId = _this.dataset.siteId;
        $state.go("accountSite.modify",{ siteId: siteId });
    };
    $scope.deleteAllSite = function() {
        if($rootScope.AllSiteIds.length == 0) {
            bjj.dialog.alert('danger', '请选择要删除的站点');
            return;
        }
        bjj.dialog.confirm('确定删除所有勾选站点？', function(){
            bjj.http.ng.postBody($scope, $http, '/api/capture/site/remove', JSON.stringify({
                siteIds: $rootScope.AllSiteIds.join(',')
            }), function (res) {
                $rootScope.AllSiteIds = [];
                $rootScope.allSiteActive = false;
                bjj.dialog.alert('success', '删除成功');
                bjj.http.ng.get($scope, $http, '/api/capture/site/list', {
                    siteType: $rootScope.siteType.addSiteType,
                    siteName: $rootScope.siteType.keywords
                }, function (res) {
                    $rootScope.siteList = res.list;
                    if($rootScope.siteList.length > 0){
                        $state.go("accountSite.modify", { siteId: res.list[0].siteId });
                        setTimeout(function () {
                            $(".site-list ul li").eq(1).addClass("actively");
                        }, 0);
                    }else {
                        $state.go("accountSite");
                        $scope.siteListEmpty = true;
                    }
                })
            }, function (res) {
            },function(res) {
                bjj.dialog.alert('danger', res.msg);
            })
        })
    }
    /*删除站点*/
    $scope.deleteSiteId = function($event) {
        var siteId = $event.target.dataset.siteId;
        $state.go("accountSite.modify", { siteId: siteId });
        bjj.dialog.confirm('确定删除？', function(){
            bjj.http.ng.del($scope, $http, '/api/capture/site/' + siteId + '?_method=delete', {}, function (res) {
                bjj.dialog.alert('success', '删除成功');
                $rootScope.editSaveSite = false;
                $rootScope.allSiteActive = false;
                $rootScope.AllSiteIds = [];
                bjj.http.ng.get($scope, $http, '/api/capture/site/list', {
                    siteType: $rootScope.siteType.addSiteType,
                    siteName: $rootScope.siteType.keywords
                }, function (res) {
                    $rootScope.siteList = res.list;
                    if($rootScope.siteList.length > 0){
                        $state.go("accountSite.modify", { siteId: res.list[0].siteId });
                        setTimeout(function () {
                            $(".site-list ul li").eq(1).addClass("actively");
                        }, 0);
                    }else {
                        $state.go("accountSite");
                        $scope.siteListEmpty = true;
                    }
                })
                bjj.http.ng.get($scope, $http, '/api/capture/sites', {}, function (res) {
                    for(var i = 1;i<res.msg.length; i++) {
                        if(res.msg[i].siteId == siteId) {
                            res.msg.splice(i,1)
                        }
                    }
                },function(res) {
                    $rootScope.siteList = [];
                });
                if(!$rootScope.deleteFlag) {
                    $rootScope.deleteFlag = true;
                }
                $rootScope.deleteFlag = true;
            },function(res) {
                bjj.dialog.alert('danger', res.msg);
                $rootScope.deleteFlag = false;
            });
        });
    };
    $scope.myKeyup = function (e) {
        if (e.keyCode == 13) {
            $scope.searchSites($scope, $http)
        }
    };
    $scope.searchSites = function() {
        $rootScope.allSiteActive = false;
        $rootScope.AllSiteIds = [];
        bjj.http.ng.get($scope, $http, '/api/capture/site/list', {
            siteType: $rootScope.siteType.addSiteType,
            siteName: $rootScope.siteType.keywords
        }, function (res) {
            $rootScope.siteList = res.list;
            if($rootScope.siteList.length > 0){
                $state.go("accountSite.modify", { siteId: res.list[0].siteId });
                setTimeout(function () {
                    $(".site-list ul li").eq(1).addClass("actively");
                }, 0);
            }else {
                $state.go("accountSite");
                $scope.siteListEmpty = true;
            }
        })
    }
};
