/**
 * Author : YCSnail
 * Date : 2017-07-27
 * Email : liyancai1986@163.com
 */
var subjectNewsListCtrl = function ($rootScope, $scope, $http, $sce) {
    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.containerBox', getSubjectNewsListData, 10);
    $scope.pageSource = "captureSubject.newsDetail";
    $scope.newsListOrder = 1;
    $scope.queryScope = 1;
    $scope.subjectNewsListLoad = true;
    if($rootScope.captureSubjectFilter != undefined){
        // if($rootScope.captureSubjectFilter.keywords != undefined && $rootScope.captureSubjectFilter.keywords != ''){
        //     $scope.keywords = $rootScope.captureSubjectFilter.keywords;
        // }
        if($rootScope.captureSubjectFilter.newsListOrder != undefined){
            $scope.newsListOrder = $rootScope.captureSubjectFilter.newsListOrder;
        }
        // if($rootScope.captureSubjectFilter.queryScope != undefined){
        //     $scope.queryScope = $rootScope.captureSubjectFilter.queryScope;
        // }
    }

    getSubjectNewsListData($scope, $http);
    //搜索
    $scope.keywordsSearchClick = function(){
        $scope.pno = 1;
        // $rootScope.captureSubjectFilter.keywords = $scope.keywords;
        getSubjectNewsListData($scope, $http);
    };
    $scope.myKeyup = function(e) {
        if (e.keyCode == 13) {
            $scope.pno = 1;
            // $rootScope.captureSubjectFilter.keywords = $scope.keywords;
            if($scope.subjectNewsListLoad) {
                $scope.subjectNewsListLoad = false;
                getSubjectNewsListData($scope, $http);
            }
        };
    }
    //排序
    $scope.orderClick = function($event){
        var order = $event.target.dataset.order;
        $scope.newsListOrder = $event.target.dataset.order;
        $('#dropdown-toggle .order').text($event.target.innerHTML);
        $scope.pno = 1;
        $rootScope.captureSubjectFilter.newsListOrder = order;
        getSubjectNewsListData($scope, $http);
    };
    // 标题 正文切换
    $scope.queryScopeClick = function(v){
        $scope.queryScope = v;
        // $rootScope.captureSubjectFilter.queryScope = v;
    };
    /*单选*/
    $scope.toggleNewsList = function () {
        this.item.active = !this.item.active;
        $scope.active = false;
        $scope.subjectNews = _.filter($scope.dataList, {'active' : true});
        if($scope.subjectNews.length == $scope.dataList.length){
            $scope.active = true;
        }
    };
    /*全选*/
    $scope.toggleAllNewsList = function () {
        $scope.active = !$scope.active;
        angular.forEach($scope.dataList, function (v,i) {
            if($scope.active) {
                v.active = true;
            }else {
                v.active = undefined;
            }
        });
        $scope.subjectNews = _.filter($scope.dataList, {'active' : true});
    };
    /*标为垃圾*/
    $scope.newsToRubbish = function (newsId) {
        toRubbish($scope, $http, newsId, getSubjectNewsListData);
    }
    $rootScope.deleteNewsToRubbish = function(newsId) {
        toRubbish($scope, $http, newsId, $rootScope.back2NewsList);
    }
    /*批量收藏*/
    $scope.favoriteAll = function($event) {
        $(".listFavorite").hide();
        $event.stopPropagation();
        if($rootScope.normalGroup.length > 1) {
            $(".showGroup").show();
        }else {
            var groupId = $rootScope.normalGroup[0].id;
            allAddFavorite($scope, $http, groupId);
        }
    };
    $scope.favoriteAllGroup = function($event, groupId) {
        $event.stopPropagation();
        allAddFavorite($scope, $http, groupId, function() {
            $(".showGroup").hide();
        });
    };
    //收藏
    $scope.favoriteClick = function($event, newsIds, item) {
        var clickHeight = $event.pageY;
        var allHeight = $(".bjj-content").height();
        if(!item.isCollection){
            if($scope.normalGroup.length < 8) {
                if( allHeight - clickHeight < $scope.normalGroup.length * 35) {
                    $('.listFavorite').css('margin-top','-'+($scope.normalGroup.length * 35 + 35) + 'px' )
                }else {
                    $('.listFavorite').css('margin-top', 0 )
                }
            }else {
                if( allHeight - clickHeight < 8 * 35) {
                    $('.listFavorite').css('margin-top','-'+( 8 * 35 + 35) + 'px' )
                }else {
                    $('.listFavorite').css('margin-top', 0 )
                }
            }
            $(".showGroup").hide();
            $(".listFavorite").hide();
            $event.stopPropagation();
            if($rootScope.normalGroup.length > 1) {
                $($event.target).next(".listFavorite").show();
            }else {
                var groupId = $rootScope.normalGroup[0].id;
                addFavorite($scope, $http, newsIds, groupId, function(){
                    item.isCollection = true;
                });
            }
        }else{
            delFavouriteNews($scope,$http,newsIds,function(){
                item.isCollection = false;
            });
        }
    };
    $scope.favoriteGroup = function($event, groupId) {
        $event.stopPropagation();
        var newsIds = $event.target.dataset.newsId;
        addFavorite($scope, $http, newsIds, groupId, function() {
            $(".id"+ newsIds ).hide();
            for(var key in $scope.dataList) {
                if(newsIds == $scope.dataList[key].id) {
                    $scope.dataList[key].isCollection = true;
                }
            }
        });
    };
    angular.element(window).bind('click',function(){
        $(".listFavorite").hide();
        $(".showGroup").hide();
    });
    //批量推送
    $scope.pushAll = function(){
        var ids = [];
        var newsIds = { "newsIds" : [] };
        _.forEach($scope.subjectNews, function (v, i) {
            ids.push($scope.subjectNews[i].id);
        });
        if (ids.length == 0){
            bjj.dialog.alert('danger', '请选中你要推送的新闻！');
            return;
        };
        newsIds.newsIds = ids;
        bjj.dialog.confirm('确定推送吗？', function(){
            bjj.http.ng.postBody($scope, $http, '/api/capture/news/push?_method=post', JSON.stringify(
                newsIds
            ), function (res) {
                bjj.dialog.alert('success', '推送成功！');
                _.forEach($scope.subjectNews, function (v, i) {
                    for(var key in newsIds) {
                        if(($scope.subjectNews[i].id) == newsIds[key]) {
                            $scope.subjectNews[i].isPush = true;
                        }
                    }
                    $scope.subjectNews[i].isPush = true;
                });
            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            });
        });
    };
    //推送
    $scope.push =function(id, item) {
        if(!item.isPush) {
            bjj.http.ng.postBody($scope, $http, '/api/capture/news/push?_method=post', JSON.stringify({
                newsIds: [id]
            }), function (res) {
                bjj.dialog.alert('success', '推送成功！');
                item.isPush = true;
            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            });
        }else {
            bjj.dialog.alert('danger', '请不要重复推送！');
        }
    };
    $scope.gotoNewsDetail = function ($event, newsId) {
        $rootScope.writeNewsId($rootScope.user.userName, newsId, 'subject');
        getCaptureNewsDetail($rootScope, $scope, $http, $sce, newsId);
        $($event.target).parent().addClass('read');
    };
    $rootScope.back2NewsList = function () {
        $('.capture-news-detail-page').hide();
        $('.capture-news-page').fadeIn();
        $('.bjj-cont-page').scrollTop($scope.captureNewsScrollTop);
    };
};
var getSubjectNewsListData = function($scope, $http){
    bjj.http.ng.get($scope, $http,'/api/capture/subject/' + $scope.captureSubjectFilter.subjectId + '/news', {
        pageSize        : $scope.psize,
        pageNo          : $scope.pno,
        queryId         : $scope.pno > 1 ? $scope.queryId : '',
        queryScope      : $scope.queryScope,
        queryString     : $scope.keywords,
        siteType        : $scope.captureSubjectFilter.siteType,
        hot             : $scope.captureSubjectFilter.heat,
        startTime       : $scope.captureSubjectFilter.startTime,
        endTime         : $scope.captureSubjectFilter.endTime,
        orientation     : $scope.captureSubjectFilter.orientation,
        hasPic          : $scope.captureSubjectFilter.hasPic,
        classification  : $scope.captureSubjectFilter.classification,
        order           : $scope.captureSubjectFilter.newsListOrder
    }, function (res) {
        $scope.subjectNewsListLoad = true;
        var list = res.newsList;
        if(list.length < ($(window).height() - 370)/ 161) {
            $scope.captureSubjectFilter.containerSubjectBox = true;
        }else {
            $scope.captureSubjectFilter.containerSubjectBox = false;
        }
        $scope.readNewsId($scope.user.userName, 'subject', list);
        bjj.scroll.loadList.callback($scope, list);
        $scope.queryId = res.queryId;
        $scope.listEmptyMsg = res.msg;
        if($scope.dataListEmptyView) {
            $scope.dataListBottomFinalView = false;
        }
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    }, 'captureSiteNewsLoading');
};

var getCaptureNewsDetail = function ($rootScope, $scope, $http, $sce, newsId) {
    $scope.captureNewsScrollTop = $('.bjj-cont-page').scrollTop();
    bjj.http.ng.get($scope, $http, '/api/capture/newsDetail/' + newsId, {}, function (res) {
        $rootScope.news = res.msg;
        $rootScope.news.content = $sce.trustAsHtml($rootScope.news.content);
        if($scope.news.newsType == 6) {
            $(".detail-content").css({"width":"640px","margin":"0 auto"})
        }else {
            $(".detail-content").css({"width":"100%"})
        }
        $('.capture-news-page').hide();
        $('.capture-news-detail-page').fadeIn();
        $('.bjj-cont-page').scrollTop(0);
    });
};
var toRubbish = function ($scope, $http, newsId, callback) {
    bjj.dialog.confirm('删除这条信息并上报垃圾', function () {
        bjj.http.ng.put($scope, $http, '/api/rubbish/news/' + newsId,{}, function () {
            $("."+ newsId ).slideUp();
            for(var i = 1;i<$scope.dataList.length; i++) {
                if($scope.dataList[i].id == newsId) {
                    $scope.dataList.splice(i,1)
                }
            }
            if($scope.dataList.length == 1) {
                $scope.dataListEmptyView = true;
                $scope.dataListBottomFinalView = false;
            }
            callback();
        }, function(res) {
            bjj.dialog.alert('danger', res.msg);
        });
    })
}
var addFavorite =function($scope, $http, newsId, groupId, callback) {
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
    });
}
var allAddFavorite = function($scope, $http, groupId, callback) {
    var ids = [];
    var newsIds = { "newsIds" : [] };
    _.forEach($scope.subjectNews, function (v, i) {
        ids.push($scope.subjectNews[i].id);
    });
    if (ids.length == 0){
        bjj.dialog.alert('danger', '请选中你要收藏的新闻！');
        return;
    };
    newsIds.newsIds = ids;
    bjj.http.ng.postBody($scope, $http, '/api/capture/news/favorite?_method=post', JSON.stringify({
        newsIds : newsIds.newsIds,
        groupId : groupId
    }), function (res) {
        bjj.dialog.alert('success', '收藏成功！');
        if(callback) {
            callback();
        }
        _.forEach($scope.subjectNews, function (v, i) {
            for(var key in newsIds) {
                if(($scope.subjectNews[i].id) == newsIds[key]) {
                    $scope.subjectNews[i].isCollection = true;
                }
            }
            $scope.subjectNews[i].isCollection = true;
        });
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    });
}
var delFavouriteNews = function($scope, $http, newsIds,callback) {
    bjj.http.ng.del($scope, $http, 'api/capture/news/collect/' + newsIds, {}, function (res) {
        bjj.dialog.alert('danger', "取消收藏！");
        callback();
    }, function (res) {
        bjj.dialog.alert('danger', res.msg);
    });
}