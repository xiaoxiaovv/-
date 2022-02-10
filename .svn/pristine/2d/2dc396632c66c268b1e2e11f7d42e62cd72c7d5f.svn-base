/**
 * Author : YCSnail
 * Date : 2017-07-21
 * Email : liyancai1986@163.com
 */
var dashboardCtrl = function ($rootScope, $scope, $http, $state) {
    $scope.reprintMediaMonitorActive = false;
    $scope.riseRateMonitorActive = false;
    $scope.wechatNewsActive = false
    $scope.subjectActive = false;
    $scope.hotNewsActive = false;
    $scope.weiboSiteActive = false;
    if($.cookie('roleName') == '互联网用户') {
        $state.go('captureSite', {type: 'list', siteType: 1});
        return;
    }
    $scope.userName = $.cookie("userName")
    if($.cookie('isRecommandInited') == 'false') {
        $scope.isBarrierBed = true;
    }else{
        if($.cookie('isPNumTips') == 'true' && $.cookie('showTips') == 'true'){
            $('#phoneModel').show();
        }
    }
    getCaptureDashboardModules($rootScope, $scope, $http, $state);
    $scope.allWeiboSiteToggleSites = function() {
        $scope.weiboSiteActive = !$scope.weiboSiteActive;
        if($scope.weiboSiteActive) {
            _.forEach($scope.weiboSiteList, function(v, i) {
                v.active = true;
            })
        }else {
            _.forEach($scope.weiboSiteList, function(v, i) {
                v.active = false;
            })
        }
    }
    $scope.allHotNewsToggleSites = function() {
        $scope.hotNewsActive = !$scope.hotNewsActive;
        if($scope.hotNewsActive) {
            _.forEach($scope.hotNewsSiteList, function(v, i) {
                v.active = true;
            })
        }else {
            _.forEach($scope.hotNewsSiteList, function(v, i) {
                v.active = false;
            })
        }
    }
    $scope.allSubjectToggleSites = function() {
        $scope.subjectActive = !$scope.subjectActive;
        if($scope.subjectActive) {
            _.forEach($scope.subjectSiteList, function(v, i) {
                v.active = true;
            })
        }else {
            _.forEach($scope.subjectSiteList, function(v, i) {
                v.active = false;
            })
        }
    }
    $scope.allWechatNewsToggleSites = function() {
        $scope.wechatNewsActive = !$scope.wechatNewsActive;
        if($scope.wechatNewsActive) {
            _.forEach($scope.highLightWeChartList, function(v, i) {
                v.active = true;
            })
        }else {
            _.forEach($scope.highLightWeChartList, function(v, i) {
                v.active = false;
            })
        }
    }
    $scope.allRiseToggleSites = function() {
        $scope.riseRateMonitorActive = !$scope.riseRateMonitorActive;
        if($scope.riseRateMonitorActive) {
            _.forEach($scope.riseRateMonitorList, function(v, i) {
                v.active = true;
            })
        }else {
            _.forEach($scope.riseRateMonitorList, function(v, i) {
                v.active = false;
            })
        }
    }
    $scope.allReprintToggleSites = function() {
        $scope.reprintMediaMonitorActive = !$scope.reprintMediaMonitorActive;
        if($scope.reprintMediaMonitorActive) {
            _.forEach($scope.reprintMediaMonitorList, function(v, i) {
                v.active = true;
            })
        }else {
            _.forEach($scope.reprintMediaMonitorList, function(v, i) {
                v.active = false;
            })
        }
    }
    /*设置勾选*/
    $scope.dashboardToggleSites = function (module) {
        this.item.active = !this.item.active;
        if(module == 'weiboSite') {
            if($scope.weiboSiteList.every( item => item.active === true)) {
                $scope.weiboSiteActive = true;
            }else {
                $scope.weiboSiteActive = false;
            }
        }
        if(module == 'hotNews') {
            if($scope.hotNewsSiteList.every( item => item.active === true)) {
                $scope.hotNewsActive = true;
            }else {
                $scope.hotNewsActive = false;
            }
        }
        if(module == 'subject') {
            if($scope.subjectSiteList.every( item => item.active === true)) {
                $scope.subjectActive = true;
            }else {
                $scope.subjectActive = false;
            }
        }
        if(module == 'wechatNews') {
            if($scope.highLightWeChartList.every( item => item.active === true)) {
                $scope.wechatNewsActive = true;
            }else {
                $scope.wechatNewsActive = false;
            }
        }
        if(module == 'rise') {
            if($scope.riseRateMonitorList.every( item => item.active === true)) {
                $scope.riseRateMonitorActive = true;
            }else {
                $scope.riseRateMonitorActive = false;
            }
        }
        if(module == 'reprint') {
            if($scope.reprintMediaMonitorList.every( item => item.active === true)) {
                $scope.reprintMediaMonitorActive = true;
            }else {
                $scope.reprintMediaMonitorActive = false;
            }
        }

    };
    $scope.goToCaptureSite = function() {
        var currentDate = new Date();
        var preDate = new Date(currentDate.valueOf() -  1 * 24 * 60 * 60 * 1000);
        var format = function(time, format){
            var t = new Date(time);
            var tf = function(i){return (i < 10 ? '0' : '') + i};
            return format.replace(/yyyy|MM|dd|HH|mm|ss/g, function(a){
                switch(a){
                    case 'yyyy':
                        return tf(t.getFullYear());
                        break;
                    case 'MM':
                        return tf(t.getMonth() + 1);
                        break;
                    case 'mm':
                        return tf(t.getMinutes());
                        break;
                    case 'dd':
                        return tf(t.getDate());
                        break;
                    case 'HH':
                        return tf(t.getHours());
                        break;
                    case 'ss':
                        return tf(t.getSeconds());
                        break;
                }
            })
        }
        var startTime = format(preDate, 'yyyy-MM-dd HH:mm:ss');
        $state.go('captureSite', {
            type         : 'list',
            siteType     : 1,
            order        : 2,
            startTime    : startTime
        });
    };
    /*推送*/
    $scope.push =function(id, item) {
        $scope.item = item;
        if(!item.isPush) {
            bjj.http.ng.postBody($scope, $http, '/api/capture/news/push?_method=post', JSON.stringify({
                newsIds: [id]
            }), function (res) {
                bjj.dialog.alert('success', '推送成功！');
                $scope.item.isPush = true;
            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            });
        }
    };
    /*收藏*/
    $scope.favorite = function($event, newsIds, item) {
        var clickHeight = $event.pageY;
        var allHeight = $(".bjj-content").height();
        $scope.item = item;
        if(!item.isCollection){
            if($scope.normalGroup.length < 8) {
                if( allHeight - clickHeight < $scope.normalGroup.length * 35) {
                    $('.dashboard-body .handle .newsFavoriteBox ol').css('margin-top','-'+($scope.normalGroup.length * 35 + 35) + 'px' )
                }else {
                    $('.dashboard-body .handle .newsFavoriteBox ol').css('margin-top', 0 )
                }
            }else {
                if( allHeight - clickHeight < 8 * 35) {
                    $('.dashboard-body .handle .newsFavoriteBox ol').css('margin-top','-'+( 8 * 35 + 35) + 'px' )
                }else {
                    $('.dashboard-body .handle .newsFavoriteBox ol').css('margin-top', 0 )
                }
            }
            $(".site-group").hide();
            $(".newsFavoriteBox").hide();
            $event.stopPropagation();
            if($rootScope.normalGroup.length > 1) {
                $($event.target).next(".newsFavoriteBox").show()
                $($event.target).parent().next().children(".newsFavoriteBox").show();
            }else {
                var groupId = $rootScope.normalGroup[0].id;
                dashboardFavorite($scope, $http, newsIds, groupId,function(){
                    item.isCollection = true;
                });
            }
        }else{
            delFavouriteNews($scope,$http,newsIds,function(){
                item.isCollection = false;
            });

        }
    };
    /* 新手引导*/
    $scope.informationNext = function() {
        $('.out-guide .tips').hide();
        $('.out-guide .monitor').show();
    }
    $scope.monitorPrev  = function() {
        $('.out-guide .monitor').hide();
        $('.out-guide .tips').show();
    }
    $scope.monitorNext = function() {
        $('.out-guide .monitor').hide();
        $('.out-guide .intelligent-organization').show();
    }
    $scope.intelligentOrganizationPrev = function() {
        $('.out-guide .intelligent-organization').hide();
        $('.out-guide .monitor').show();
    }
    $scope.intelligentOrganizationNext = function() {
        $('.out-guide .intelligent-organization').hide();
        $('.out-guide .set-account').show();
    }
    $scope.setAccountPrev = function() {
        $('.out-guide .set-account').hide();
        $('.out-guide .intelligent-organization').show();
    }
    $scope.setAccountFinished = function() {
        finishedFunction($scope, $http)
    }
    $scope.closeDialog = function() {
        finishedFunction($scope, $http)
    }
};
var getCaptureDashboardModules = function ($rootScope, $scope, $http, $state) {
    bjj.http.ng.get($scope, $http, '/api/custom/dashboard/modules', {}, function (res) {
        $scope.dashboardModuleList = res.list;
        if($scope.appInfo.appKey == 'ppc') {
            $scope.dashboardModuleList.splice(0, 0, {dashboard : "ppcNews", isShow : true})
        }
        getSiteList($rootScope, $scope, $http, $state);
    });
};
var dashboardFavorite = function($scope, $http, newsIds, groupId, callback) {
    bjj.http.ng.postBody($scope, $http, '/api/capture/news/favorite?_method=post', JSON.stringify({
        newsIds : [newsIds],
        groupId : groupId
    }), function (res) {
        bjj.dialog.alert('success', '收藏成功！');
        if(callback) {
            callback()
        }
    },function(res) {
        bjj.dialog.alert('danger', res.msg);
    })
};
var finishedFunction = function ($scope, $http) {
    var setting = { expires: 1000, path: '/' };
    bjj.http.ng.post($scope, $http, '/api/account/recommand/initialization', {}, function (res) {
        $scope.isBarrierBed = false;
        console.log(123)
        if($.cookie('isPNumTips') == 'true' && $.cookie('showTips') == 'true'){
            $('#phoneModel').show();
        }
        $.cookie('isRecommandInited',   true, setting);
    },function(res) {
        bjj.dialog.alert('danger', res.msg);
    });
};
var delFavouriteNews = function($scope, $http, newsIds,callback) {
    bjj.http.ng.del($scope, $http, 'api/capture/news/collect/' + newsIds, {}, function (res) {
        bjj.dialog.alert('danger', "取消收藏！");
        callback();
    }, function (res) {
        bjj.dialog.alert('danger', res.msg);
    });
}