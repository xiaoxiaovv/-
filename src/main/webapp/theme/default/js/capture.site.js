/**
 * Author : YCSnail
 * Date : 2017-07-24
 * Email : liyancai1986@163.com
 */
var captureSiteCtrl = function ($rootScope, $scope, $http, $state, $stateParams) {
    var allSite     = { siteId: "-1", siteName: "全部", siteType:1 };
    var allWechat   = { siteId: "-2", siteName: "全部", siteType:2 };
    var allWeibo    = { siteId: "-3", siteName: "全部", siteType:3 };
    var currentSiteId = $stateParams.currentSiteId;
    var pageType = $stateParams.type;
    var siteType = $stateParams.siteType;
    var byOrder    = $stateParams.order;
    var defaultTime = $stateParams.startTime;
    $scope.data = {
        current: siteType == undefined ? 1 : siteType
    };
    if($rootScope.formPortal == true){
        $rootScope.minBar = true;
    }
    bjj.http.ng.get($scope, $http, '/api/capture/sites', {}, function (res) {
        $scope.webCount = res.siteCount.webStatus.webCount;
        $scope.allowWeb = res.siteCount.webStatus.allowWeb;
        $scope.weChatCount = res.siteCount.wechatStatus.weChatCount;
        $scope.allowWeChat = res.siteCount.wechatStatus.allowWeChat;
        $scope.weiBoCount = res.siteCount.weiboStatus.weiBoCount;
        $scope.allowWeiBo = res.siteCount.weiboStatus.allowWeiBo;
        $rootScope.siteList = res.msg;

        if($scope.data.current == 1) {
            $scope.siteList.unshift(allSite);
            var siteList = _.filter($rootScope.siteList,{siteType:1});
            if(siteList.length == 1) {
                $scope.siteList = [];
            }
        }
        if($scope.data.current == 2) {
            $scope.siteList.unshift(allWechat);
        }
        if($scope.data.current == 3) {
            $scope.siteList.unshift(allWeibo);
        }
        if(pageType == 'list' && $scope.siteList.length > 0){
            var randomNum = Math.random();
            if(siteType == 3) {
                $state.go('captureSite.weibolist', {
                    currentSiteId   : currentSiteId,
                    siteId      : $scope.siteList[0].siteId,
                    siteName    : $scope.siteList[0].siteName,
                    randomNum   : randomNum,
                    siteType    : 3
                });
            } else {
                $state.go('captureSite.news', {
                    siteId      : $scope.siteList[0].siteId,
                    siteName    : $scope.siteList[0].siteName,
                    auto        :  $scope.siteList[0].isAutoPush,
                    byOrder      :  byOrder,
                    defaultTime  :  defaultTime,
                    randomNum   : randomNum
                });
            }
            setTimeout(function () {
                $(".site-left-cont .capture-list .nav-list li").eq(0).addClass('active');
            });
        }
        setTimeout(function () {
            //通过此处设置可以选中站点 ps:全部等都可以重构为此处操作
            if(currentSiteId != undefined && currentSiteId != null) {
                $("li.site-"+ $stateParams.currentSiteId).addClass('active').siblings().removeClass('active');
            }
            $(".site-left-cont .capture-list").on("click", ".nav-list li", function() {
                $(this).parent().siblings(".nav-list").children("li").removeClass("active");
                $(this).addClass("active").siblings().removeClass("active");
            });
            $scope.loadSiteCount();
        }, 0);
    },function(res) {
        $rootScope.siteList = [];
    } ,'captureSiteLoading');

    $scope.loadSiteCount = function () {
        var recountList = _.filter($scope.siteList, function (v) {
            return (v.countTime == null
                || v.resetTime == null ||
                v.resetTime > v.countTime ||
                (new Date().getTime() - v.countTime > 10 * 60 * 1000)) && ['-1', '-2', '-3'].indexOf(v.siteId) == -1;
        });
        for(let i = 0; i< recountList.length; i++) {
            setTimeout(function () {
                bjj.http.ng.get($scope, $http, '/api/capture/site/' + recountList[i].siteId + '/count', {}, function (res) {
                    recountList[i].count = res.msg.count;
                });
            }, (i + 1) * 1000);
        }
    };
    $scope.resetSiteCount = function (siteId) {
        var loginSource = $.cookie('loginSource');
        if (loginSource == 2) {
            if (['-1', '-2', '-3'].indexOf(siteId) == -1) {
                bjj.http.ng.del($scope, $http, '/api/capture/site/' + siteId + '/count', {}, function (res) {
                    _.map($scope.siteList, function (v) {
                        if (v.siteId == siteId) {
                            v.count = 0;
                            v.resetTime = new Date().getTime();
                        }
                    });
                });
            }
        }
    };

    $scope.setSiteClick = function(params) {
        $scope.data.current = params;
        if(!$scope.siteList) return;
        if($scope.data.current == 2) {
            $scope.siteList = $rootScope.siteList;
            $scope.siteList.unshift(allWechat);
            $scope.siteList.splice(1,1);
            var siteList = _.filter($scope.siteList,{siteType:2});
            if(siteList.length == 1) {
                $scope.siteList = [];
            }
        }
        if($scope.data.current == 1) {
            $scope.siteList = $rootScope.siteList;
            $scope.siteList.unshift(allSite);
            $scope.siteList.splice(1,1);
            var siteList = _.filter($scope.siteList,{siteType:1});
            if(siteList.length==1) {
                $scope.siteList = [];
            }
        }
        if($scope.data.current == 3) {
            $scope.siteList = $rootScope.siteList;
            $scope.siteList.unshift(allWeibo);
            $scope.siteList.splice(1,1);
            var siteList = _.filter($scope.siteList,{siteType:3});
            if(siteList.length==1) {
                $scope.siteList = [];
            }
        }
    };
    $scope.siteClick = function (siteId, siteName, auto) {
        $scope.siteId = siteId;
        $scope.resetSiteCount(siteId);
        var randomNum = Math.random();
        $state.go('captureSite.news', {
            siteId      : siteId,
            siteName    : siteName,
            auto        : auto,
            randomNum   : randomNum
        });
    };
    $scope.weiboSiteClick = function (siteId, siteName, auto) {
        $scope.siteId = siteId;
        $scope.resetSiteCount(siteId);
        var randomNum = Math.random();
        $state.go('captureSite.weibolist', {
            siteId      : siteId,
            siteName    : siteName,
            randomNum   : randomNum
        });
    };
    $scope.goAccountSite = function() {
        $rootScope.fromRouter = false;
        $state.go('accountSite', {
            captureSiteId :  $scope.siteId
        });
    }
};
