/**
 * Author : YCSnail
 * Date : 2017-07-24
 * Email : liyancai1986@163.com
 */
var captureSubjectNewsCtrl = function ($rootScope, $scope, $http, $state, $stateParams) {
    var subjectId = $stateParams.subjectId;
    var subjectName = $stateParams.subjectName;

    if($rootScope.captureSubjectFilter == undefined){
        $rootScope.captureSubjectFilter = {
            subjectId   : subjectId,
            subjectName : subjectName,
            siteType    : 0,
            heat        : 0,
            orientation : 0,
            classification : '全部',
            hasPic      : 0,
            startTime   : '',
            endTime     : ''
        }
    }
    $rootScope.captureSubjectFilter.subjectId = subjectId;
    $rootScope.captureSubjectFilter.subjectName = subjectName;
    var subjectStartTime = {
        elem: '#subjectStartTime',
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $rootScope.captureSubjectFilter.startTime = v;
            $state.go('captureSubject.news.list', { r: new Date().getTime()});
        }
    };
    var subjectEndTime = {
        elem:"#subjectEndTime",
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $rootScope.captureSubjectFilter.endTime = v;
            $state.go('captureSubject.news.list', { r: new Date().getTime()});
        }
    };
    setTimeout(function(){
        laydate(subjectStartTime);
        laydate(subjectEndTime);
    },0)
    $scope.clearSubjectTimeClick = function() {
        $('#subjectStartTime').val('');
        $('#subjectEndTime').val('');
        $rootScope.captureSubjectFilter.startTime = '';
        $rootScope.captureSubjectFilter.endTime   = '';
        $state.go('captureSubject.news.list', { r: new Date().getTime()});
    };
    $state.go('captureSubject.news.list');

    //媒体类型
    $scope.mediaTypeClick = function($event){
        var siteType = $event.target.dataset.siteType;
        $rootScope.captureSubjectFilter.siteType  = siteType ;
        $state.go('captureSubject.news.list', { r: new Date().getTime()});
    }
    //热度
    $scope.newsHeatListClick = function($event){
        var heat = $event.target.dataset.heat;
        $rootScope.captureSubjectFilter.heat = heat;
        $state.go('captureSubject.news.list', { r: new Date().getTime()});
    };
    //倾向性
    $scope.orientationListClick = function($event){
        var orientation = $event.target.dataset.orientation;
        $rootScope.captureSubjectFilter.orientation = orientation;
        $state.go('captureSubject.news.list', { r: new Date().getTime()});
    };
    //类别
    $scope.hasPicListClick = function($event){
        var hasPic = $event.target.dataset.hasPic;
        $rootScope.captureSubjectFilter.hasPic = hasPic;
        $state.go('captureSubject.news.list', { r: new Date().getTime()});
    };
    //分类
    $scope.newsClassifyListClick = function($event){
        var classification = $event.target.dataset.classification;
        $rootScope.captureSubjectFilter.classification = classification;
        $state.go('captureSubject.news.list', { r: new Date().getTime()});
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
                $scope.news.isPush = true;
            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            })
        }else {
            bjj.dialog.alert('danger', '请不要重复推送！');
        }
    };
    $scope.favoriteNews = function($event, newsId, news) {
        $event.stopPropagation();
        $scope.subjecteNews = news;
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
            $scope.subjecteNews.isCollection = true;
            var scope = angular.element($('.news-list-content')).scope();
            for(var key in scope.dataList) {
                if(newsId == scope.dataList[key].id) {
                    scope.dataList[key].isCollection = true;
                }
            }
        })
    };
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
    bjj.http.ng.del($scope, $http, 'api/capture/news/collect/' + newsIds, {}, function (res) {
        bjj.dialog.alert('danger', "取消收藏！");
        callback();
    }, function (res) {
        bjj.dialog.alert('danger', res.msg);
    });
}