/**
 * Author : YCSnail
 * Date : 2017-07-27
 * Email : liyancai1986@163.com
 */
var siteNewsListCtrl = function ($rootScope, $scope, $http, $sce, $stateParams) {
    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.containerBox', getSiteNewsListData, 10, $rootScope);
    $scope.pageSource = "captureSite.newsDetail";
    $scope.newsListOrder = 1;
    $scope.queryScope = 1;
    $scope.siteNewsListLoad = true;
    if($rootScope.captureSiteFilter != undefined){
        // if($rootScope.captureSiteFilter.keywords != undefined && $rootScope.captureSiteFilter.keywords != ''){
        //     $scope.keywords = $rootScope.captureSiteFilter.keywords;
        // }
        if($rootScope.captureSiteFilter.newsListOrder != undefined){
            $scope.newsListOrder = $rootScope.captureSiteFilter.newsListOrder;
        }
        // if($rootScope.captureSiteFilter.queryScope != undefined){
        //     $scope.queryScope = $rootScope.captureSiteFilter.queryScope;
        // }
    }
    if($stateParams.heatOrder == 2) {
        $scope.captureSiteFilter.newsListOrder = $stateParams.heatOrder;
        $scope.newsListOrder = $stateParams.heatOrder;
    }
    getSiteNewsListData($scope, $http);

    //搜索
    $scope.keywordsSearchClick = function(){
        $scope.pno = 1;
        // $rootScope.captureSiteFilter.keywords = $scope.keywords;
        getSiteNewsListData($scope, $http);
    };
    $scope.myKeyup = function(e) {
        if (e.keyCode == 13) {
            $scope.pno = 1;
            // $rootScope.captureSiteFilter.keywords = $scope.keywords;
            if($scope.newsListLoad) {
                $scope.newsListLoad = false;
                getSiteNewsListData($scope, $http);
            }
        };
    }
    //排序
    $scope.orderClick = function($event){
        var order = $event.target.dataset.order;
        $scope.newsListOrder = $event.target.dataset.order;
        $('#dropdown-toggle .order').text($event.target.innerHTML);
        $scope.pno = 1;
        $rootScope.captureSiteFilter.newsListOrder = order;
        getSiteNewsListData($scope, $http);
    };
    // 标题 正文切换
    $scope.queryScopeClick = function(v){
        $scope.queryScope = v;
        // $rootScope.captureSiteFilter.queryScope = v;
    };
    /*单选*/
    $scope.toggleNewsList = function () {
        this.item.active = !this.item.active;
        $scope.active = false;
        $scope.captureNews = _.filter($scope.dataList, {'active' : true});
        if($scope.captureNews.length == $scope.dataList.length){
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
        $scope.captureNews = _.filter($scope.dataList, {'active' : true});
    };
    /*标为垃圾*/
    $scope.newsToRubbish = function (newsId) {
        toRubbish($scope, $http, newsId, getSiteNewsListData)
    }
    $rootScope.deleteNewsToRubbish = function (newsId) {
        toRubbish($scope, $http, newsId, $rootScope.back2NewsList)
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
        var newsIds = item.id;
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
            delFavouriteNews( $scope,$http,newsIds,function(){
                item.isCollection = false;
            });
        }
    };
    $scope.favoriteGroup = function($event, groupId) {
        $event.stopPropagation();
        var newsIds = $event.target.dataset.newsId;
        addFavorite($scope, $http, newsIds, groupId, function() {
            $(".id"+ newsIds ).hide()
            for(var key in $scope.dataList) {
                if(newsIds == $scope.dataList[key].id) {
                    $scope.dataList[key].isCollection = true;
                }
            }
        });
    }
    angular.element(window).bind('click',function(){
        $(".listFavorite").hide();
        $(".showGroup").hide();
    });
    //批量推送
    $scope.pushAll = function(){
        var ids = [];
        var newsIds = { "newsIds" : [] };
        _.forEach($scope.captureNews, function (v, i) {
            ids.push($scope.captureNews[i].id);
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
                _.forEach($scope.captureNews, function (v, i) {
                    for(var key in newsIds) {
                        if(($scope.captureNews[i].id) == newsIds[key]) {
                            $scope.captureNews[i].isPush = true;
                        }
                    }
                    $scope.captureNews[i].isPush = true;
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
        $rootScope.writeNewsId($rootScope.user.userName, newsId, 'site');
        getCaptureNewsDetail($rootScope, $scope, $http, $sce, newsId);
        $($event.target).parent().addClass('read');
    };
    $rootScope.back2NewsList = function () {
        $('.capture-news-detail-page').hide();
        $('.capture-news-page').fadeIn();
        $('.bjj-cont-page').scrollTop($scope.captureNewsScrollTop);
    };
};

var getSiteNewsListData = function($scope, $http){
    bjj.http.ng.get($scope, $http,'/api/capture/site/' + $scope.captureSiteFilter.siteId +'/news', {
        pageSize        : $scope.psize,
        pageNo          : $scope.pno,
        queryId         : $scope.pno > 1 ? $scope.queryId : '',
        queryScope      : $scope.queryScope,
        keyWords        : $scope.keywords,
        hot             : $scope.captureSiteFilter.heat,
        startTime       : $scope.captureSiteFilter.startTime,
        endTime         : $scope.captureSiteFilter.endTime,
        orientation     : $scope.captureSiteFilter.orientation,
        hasPic          : $scope.captureSiteFilter.hasPic,
        order           : $scope.captureSiteFilter.newsListOrder
    }, function (res) {
        $scope.newsListLoad = true;
        var list = res.newsList;
        if(list.length < ($(window).height() - 320)/ 161) {
            $scope.captureSiteFilter.containerSiteBox = true;
        }else {
            $scope.captureSiteFilter.containerSiteBox = false;
        }
        $scope.readNewsId($scope.user.userName, 'site', list);
        bjj.scroll.loadList.callback($scope, list);
        $scope.queryId = res.queryId;
        $scope.listEmptyMsg = res.msg;
        if($scope.dataListEmptyView) {
            $scope.dataListBottomFinalView = false;
        }
        setTimeout(function () {
            $('.btn-tip-redirect').click(function(){
                var siteId = $(".btn-tip-redirect").attr("value");
                goto('#!/account/site/modify?captureSiteId='+siteId)
            })
        }, 0);
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
var toRubbish = function($scope, $http, newsId, callback ) {
    bjj.dialog.confirm('删除这条信息并上报垃圾', function () {
        bjj.http.ng.put($scope, $http, '/api/rubbish/news/' + newsId,{}, function () {
            $("."+ newsId ).slideUp();
            for(var i = 1;i<$scope.dataList.length; i++) {
                if($scope.dataList[i].id == newsId) {
                    $scope.dataList.splice(i,1);
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
};
var addFavorite = function($scope, $http, newsId, groupId, callback) {
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
};
var allAddFavorite = function($scope, $http, groupId, callback) {
    var ids = [];
    var newsIds = { "newsIds" : [] };
    _.forEach($scope.captureNews, function (v, i) {
        ids.push($scope.captureNews[i].id);
    });
    if (ids.length == 0){
        bjj.dialog.alert('danger', '请选中你要收藏的新闻！');
        if(callback) {
            callback();
        }
        return;
    };
    newsIds.newsIds = ids;
    bjj.http.ng.postBody($scope, $http, '/api/capture/news/favorite?_method=post', JSON.stringify({
        newsIds: newsIds.newsIds,
        groupId: groupId
    }), function (res) {
        bjj.dialog.alert('success', '收藏成功！');
        if(callback) {
            callback();
        }
        _.forEach($scope.captureNews, function (v, i) {
            for(var key in newsIds) {
                if(($scope.captureNews[i].id) == newsIds[key]) {
                    $scope.captureNews[i].isCollection = true;
                }
            }
            $scope.captureNews[i].isCollection = true;
        });
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    });
};
var delFavouriteNews = function( $scope, $http, newsIds,callback) {
    bjj.http.ng.del($scope, $http, 'api/capture/news/collect/' + newsIds, {}, function (res) {
        bjj.dialog.alert('danger', "取消收藏！");
        callback();
    }, function (res) {
        bjj.dialog.alert('danger', res.msg);
    });
}

