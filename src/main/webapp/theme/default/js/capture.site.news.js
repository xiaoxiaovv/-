/**
 * Author : YCSnail
 * Date : 2017-07-24
 * Email : liyancai1986@163.com
 */
var captureSiteNewsCtrl = function ($rootScope, $scope, $http, $state, $stateParams) {
    var siteId = $stateParams.siteId;
    var siteName = $stateParams.siteName;
    var isAutoPush = $stateParams.auto === 'true';
    var defaultTime = $stateParams.defaultTime;
    var byOrder = $stateParams.byOrder;
    if($rootScope.captureSiteFilter == undefined){
        $rootScope.captureSiteFilter = {
            siteId      : siteId,
            siteName    : siteName,
            isAutoPush  : isAutoPush,
            heat        : 0,
            orientation : 0,
            hasPic      : 0,
            startTime   : '',
            endTime     : ''
        }
    }
    $rootScope.captureSiteFilter.siteId = siteId;
    $rootScope.captureSiteFilter.siteName = siteName;
    $rootScope.captureSiteFilter.isAutoPush = isAutoPush;
    if(defaultTime !=undefined) {
        $rootScope.captureSiteFilter.startTime = defaultTime;
    }
    var start = {
        elem: '#siteStartTime',
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $rootScope.captureSiteFilter.startTime = v;
            $state.go('captureSite.news.list', { r: new Date().getTime()});
        }
    };
    var end = {
        elem:"#siteEndTime",
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $rootScope.captureSiteFilter.endTime = v;
            $state.go('captureSite.news.list', { r: new Date().getTime()});
        }
    };
    setTimeout(function(){
        laydate(start);
        laydate(end);
    },0)
    $scope.clearSiteTimeClick = function() {
        $('#siteStartTime').val('');
        $('#siteEndTime').val('');
        $rootScope.captureSiteFilter.startTime = '';
        $rootScope.captureSiteFilter.endTime   = '';
        $state.go('captureSite.news.list', { r: new Date().getTime()});
    };
    $state.go('captureSite.news.list',{
        heatOrder : byOrder
    });
    $scope.toggleAutoPush = function () {
        _.forEach($rootScope.siteList, function (v,i) {
            if(v.siteId == $rootScope.captureSiteFilter.siteId){
                $rootScope.siteList[i].isAutoPush = !v.isAutoPush;
                var autoPushStatus = !$scope.captureSiteFilter.isAutoPush;
                bjj.http.ng.put($scope, $http, '/api/capture/site/' + $scope.captureSiteFilter.siteId + '/isAutoPush',{
                    isAutoPush : autoPushStatus
                }, function(res){
                    $scope.captureSiteFilter.isAutoPush = autoPushStatus;
                    _.forEach($rootScope.captureSiteList, function (v) {
                        if(v.siteId == $scope.captureSiteFilter.siteId){
                            v.isAutoPush = autoPushStatus;
                        }
                    });
                }, function(res){
                    $rootScope.siteList[i].isAutoPush = false;
                    bjj.dialog.alert('danger', res.msg);
                });
            }
        });
    };
    $scope.goAddSite = function() {
        $rootScope.fromRouter = false;
        $state.go('accountSite.add');
    }
    //热度
    $scope.newsHeatListClick = function($event){
        var heat = $event.target.dataset.heat;
        $rootScope.captureSiteFilter.heat = heat;
        $state.go('captureSite.news.list', { r: new Date().getTime()});
    };
    //倾向性
    $scope.newsOrientationListClick = function($event){
        var orientation = $event.target.dataset.orientation;
        $rootScope.captureSiteFilter.orientation = orientation;
        $state.go('captureSite.news.list', { r: new Date().getTime()});
    };
    //类别
    $scope.newsHasPicListClick = function($event){
        var hasPic = $event.target.dataset.hasPic;
        $rootScope.captureSiteFilter.hasPic = hasPic;
        $state.go('captureSite.news.list', { r: new Date().getTime()});
    };
    $scope.pushNews = function(id, news) {
        if(!news.isPush) {
            bjj.http.ng.postBody($scope, $http, '/api/capture/news/push?_method=post', JSON.stringify({
                newsIds: [id]
            }), function (res) {
                bjj.dialog.alert('success', '推送成功！');
                var scope = angular.element($('.news-list-content')).scope();
                for(var key in scope.dataList) {
                    if(id == scope.dataList[key].id) {
                        scope.dataList[key].isPush = true;
                    }
                }
                news.isPush = true;
                $rootScope.siteDetailPushNewsId = id;
                $rootScope.isPush = news.isPush;
            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            })
        }else {
            bjj.dialog.alert('danger', '请不要重复推送！');
        }
    };

    $scope.favoriteNews = function($event, newsId, news) {
        $scope.captureNews = news;
        $event.stopPropagation();
        if(!news.isCollection) {
            if($rootScope.normalGroup.length > 1) {
                $(".detailFavorite").show();
            }else {
                var groupId = $rootScope.normalGroup[0].id;
                detailAddFavorite($scope, $http, newsId, groupId, function(){
                    news.isCollection = true;
                    var scope = angular.element($('.news-list-content')).scope();
                    for(var key in scope.dataList) {
                        if(newsId == scope.dataList[key].id) {
                            scope.dataList[key].isCollection = true;
                        }
                    }
                });
            }
        }else {
            delFavouriteNews($scope, $http, newsId, function(){
                news.isCollection = false;
                var scope = angular.element($('.news-list-content')).scope();
                for(var key in scope.dataList) {
                    if(newsId == scope.dataList[key].id) {
                        scope.dataList[key].isCollection = false;
                    }
                }
            });
        }
    };
    $scope.detailFavoriteGroup = function($event, newsId, groupId) {
        $event.stopPropagation();
        detailAddFavorite($scope, $http, newsId, groupId, function() {
            $(".detailFavorite").hide();
            $scope.captureNews.isCollection = true;
            var scope = angular.element($('.news-list-content')).scope();
            for(var key in scope.dataList) {
                if(newsId == scope.dataList[key].id) {
                    scope.dataList[key].isCollection = true;
                }
            }
        })
    }
    angular.element(window).bind('click',function(){
        $(".detailFavorite").hide()
    });
};
var detailAddFavorite = function($scope, $http, newsId, groupId, callback) {
    bjj.http.ng.postBody($scope, $http, '/api/capture/news/favorite?_method=post', JSON.stringify({
        newsIds : [newsId],
        groupId : groupId
    }), function (res) {
        bjj.dialog.alert('success', '收藏成功！');
        if(callback) {
            callback();
        }
    },function(res) {
        bjj.dialog.alert('danger', res.msg);
    })
}
var delFavouriteNews = function($scope, $http, newsIds,callback) {
    bjj.http.ng.del( $scope, $http, 'api/capture/news/collect/' + newsIds, {}, function (res) {
        bjj.dialog.alert('danger', "取消收藏！");
        var scope = angular.element($('.news-list-content')).scope();
        for(var key in scope.dataList) {
            if(newsIds == scope.dataList[key].id) {
                scope.dataList[key].isCollection = false;
            }
        }
        callback();
    }, function (res) {
        bjj.dialog.alert('danger', res.msg);
    });
}
