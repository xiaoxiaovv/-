/**
 * Author : YCSnail
 * Date : 2017-07-27
 * Email : liyancai1986@163.com
 */
var newsDetailCtrl = function ($rootScope, $scope, $http, $state, $stateParams, $sce) {
    $scope.page = $stateParams.page;
    $scope.canGoBack = (history.length > 1);

    bjj.http.ng.get($scope, $http, '/api/capture/newsDetail/' + $stateParams.newsId, {}, function (res) {
        $scope.news = res.msg;
        $scope.news.content = $sce.trustAsHtml($scope.news.content);
        if($scope.news.newsType == 6) {
            $(".detail-content").css({"max-width":"640px","margin":"0 auto"})
        }else {
            $(".detail-content").css({"width":"100%"})
        }
    });
    $scope.detailPush = function(id, news) {
        $scope.news = news;
        if(!news.isPush) {
            var newsIds = {'newsIds':[$stateParams.newsId]};
            $http({
                method: 'POST',
                url: '/api/capture/news/push',
                data: newsIds
            }).then(function (res) {
                if(res.data.status == 200) {
                    bjj.dialog.alert('success', '推送成功！');
                    $scope.news.isPush = true;
                    angular.forEach($scope.dataList, function (v,i) {
                        $scope.active = false;
                        v.active = undefined;
                        v.isPush = true;
                        $scope.news = [];
                    });
                }else if(res.data.status == 500) {
                    bjj.dialog.alert('danger', res.data.msg);
                }
            }, function(res) {
                bjj.dialog.alert('danger', res.data.msg);
            });
        }else {
            bjj.dialog.alert('danger', '请不要重复推送！');
        }
    };
    $scope.dashboardDetailFavorite = function($event, newsId, news) {
        $scope.news = news;
        $event.stopPropagation();
        if(!news.isCollection) {
            if($rootScope.normalGroup.length > 1) {
                $(".dashboardDetailFavorite").show();
            }else {
                var groupId = $rootScope.normalGroup[0].id;
                dashboardDetailFavorite($scope, $http, $stateParams, groupId, function(){
                    news.isCollection = true;
                });
            }
        } else {
            delFavouriteNews($scope, $http, newsId, function(){
                news.isCollection = false;
            });
        }
    };
    $scope.dashboardDetailFavoriteGroup = function($event, groupId) {
        $event.stopPropagation();
        dashboardDetailFavorite($scope, $http, $stateParams, groupId, function() {
            $(".dashboardDetailFavorite").hide();
            $scope.news.isCollection = true;
        })
    };
    angular.element(window).bind('click',function(){
        $(".dashboardDetailFavorite").hide();
    });
    $scope.return = function() {
        history.go(-1);
    }
};
var dashboardDetailFavorite = function($scope, $http, $stateParams, groupId, callback) {
    var newsIds = {'newsIds': [$stateParams.newsId]};
    bjj.http.ng.postBody($scope, $http, '/api/capture/news/favorite?_method=post', JSON.stringify({
        newsIds : newsIds.newsIds,
        groupId : groupId
    }), function (res) {
        bjj.dialog.alert('success', '收藏成功！');
        if(callback) {
            callback();
        }
    },function(res) {
        bjj.dialog.alert('danger', res.msg);
    })
};
var delFavouriteNews = function($scope, $http, newsIds,callback) {
    bjj.http.ng.del($scope, $http, 'api/capture/news/collect/' + newsIds, {}, function (res) {
        bjj.dialog.alert('danger', "取消收藏！");
        callback();
    }, function (res) {
        bjj.dialog.alert('danger', res.msg);
    });
};