var reprintMediaMonitorListCtrl = function ($rootScope, $scope, $http, $state, $sce, $stateParams) {
    $scope.modalName = $stateParams.modal;
    $scope.needShow = true;
    $scope.highlightShow = false;
    $scope.riseRateMonitorShow = false;
    $scope.reprintMediaMonitorShow = true;
    $scope.timeShow = true;
    $scope.page = $stateParams.page;
    if($scope.highlightSiteFilter == undefined) {
        $scope.highlightSiteFilter = {
            heat        : 0,
            orientation : 0,
            hasPic      : 0,
            startTime   : '',
            endTime     : ''
        }
    }
    var start = {
        elem: '#siteStartTime',
        event: 'click',
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $scope.highlightSiteFilter.startTime = v;
            $scope.pno = 1;
            getNewsListData($scope, $http)
        }
    };
    var end = {
        elem:"#siteEndTime",
        event: 'click',
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $scope.highlightSiteFilter.endTime = v;
            $scope.pno = 1;
            getNewsListData($scope, $http)
        }
    };
    setTimeout(function(){
        laydate(start);
        laydate(end);
    },0)
    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.containerBox', getNewsListData, 10);
    getNewsListData($scope, $http)
    //热度
    $scope.hightlightNewsHeatListClick = function($event){
        var heat = $event.target.dataset.heat;
        $scope.highlightSiteFilter.heat = heat;
        $scope.pno = 1;
        getNewsListData($scope, $http)
    };
    //倾向性
    $scope.hightlightNewsOrientationListClick = function($event){
        var orientation = $event.target.dataset.orientation;
        $scope.highlightSiteFilter.orientation = orientation;
        $scope.pno = 1;
        getNewsListData($scope, $http)
    };
    //类别
    $scope.hightlightNewsHasPicListClick = function($event){
        var hasPic = $event.target.dataset.hasPic;
        $scope.highlightSiteFilter.hasPic = hasPic;
        $scope.pno = 1;
        getNewsListData($scope, $http);
    };
    //搜索
    $scope.keywordsSearchClick = function(){
        $scope.pno = 1;
        $scope.highlightSiteFilter.keywords = $scope.keywords;
        getNewsListData($scope, $http);
    };
    $scope.myKeyup = function(e) {
        if (e.keyCode == 13) {
            $scope.pno = 1;
            $scope.highlightSiteFilter.keywords = $scope.keywords;
            if($scope.highlightNewsListLoad) {
                $scope.highlightNewsListLoad = false;
                getNewsListData($scope, $http);
            }

        };
    }
    /*全选*/
    $scope.hightlightToggleAllNewsList = function () {
        $scope.active = !$scope.active;
        angular.forEach($scope.dataList, function (v,i) {
            if($scope.active) {
                v.active = true;
            }else {
                v.active = undefined;
            }
        });
        $scope.captureReprintNews = _.filter($scope.dataList, {'active' : true});
    };
    /*单选*/
    $scope.hightlightToggleNewsList = function () {
        this.item.active = !this.item.active;
        $scope.active = false;
        $scope.captureReprintNews = _.filter($scope.dataList, {'active' : true});
        if($scope.captureReprintNews.length == $scope.dataList.length){
            $scope.active = true;
        }
    };
    /*跳详情*/
    $scope.gotoDetail = function ($event, newsId) {
        $rootScope.writeNewsId($rootScope.user.userName, newsId, 'reprintMediaMonitorMore');
        $(".deleteNews").remove();
        gethighlightNewsDetail($scope, $http, $sce, newsId);
        $($event.target).parent().addClass('read');
    };
    $rootScope.back2NewsList = function () {
        $('.capture-news-detail-page').hide();
        $('.dashboard-list-body').fadeIn();
        $('.bjj-cont-page').scrollTop($scope.captureNewsScrollTop);
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
    $scope.favoriteGroup = function($event, groupId, newsIds) {
        $event.stopPropagation();
        addFavorite($scope, $http, newsIds, groupId, function() {
            $(".id"+ newsIds ).hide();
            for(var n in $scope.dataList){
                if(newsIds == $scope.dataList[n].id){
                    $scope.dataList[n].isCollection = true;
                }
            }
        });
    }
    angular.element(window).bind('click',function(){
        $(".listFavorite").hide();
        $(".showGroup").hide();
    });
    $scope.favoriteAll = function($event) {
        $(".listFavorite").hide();
        $event.stopPropagation();
        if($rootScope.normalGroup.length > 1) {
            $(".showGroup").show();
        }else {
            var groupId = $rootScope.normalGroup[0].id;
            allAddFavorite($scope, $http, groupId, function () {
            });
        }
    };
    $scope.favoriteAllGroup = function($event, groupId) {
        $event.stopPropagation();
        allAddFavorite($scope, $http, groupId, function() {
            $(".showGroup").hide();
        });
    };
    //批量推送
    $scope.pushAll = function(){
        $scope.captureReprintNews = _.filter($scope.dataList, {'active' : true});
        var ids = [];
        var newsIds = { "newsIds" : [] };
        _.forEach($scope.captureReprintNews, function (v, i) {
            ids.push($scope.captureReprintNews[i].id);
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
                _.forEach($scope.captureReprintNews, function (v, i) {
                    for(var key in newsIds) {
                        if(($scope.captureReprintNews[i].id) == newsIds[key]) {
                            $scope.captureReprintNews[i].isPush = true;
                        }
                    }
                    $scope.captureReprintNews[i].isPush = true;
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
    $scope.pushNews = function(newsId, news) {
        if(!news.isPush) {
            bjj.http.ng.postBody($scope, $http, '/api/capture/news/push?_method=post', JSON.stringify({
                newsIds: [newsId]
            }), function (res) {
                bjj.dialog.alert('success', '推送成功！');
                for(var n in $scope.dataList){
                    if(newsId == $scope.dataList[n].id){
                        $scope.dataList[n].isPush = true;
                    }
                }
                news.isPush = true;
            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            })
        }else {
            bjj.dialog.alert('danger', '请不要重复推送！');
        }
    };
    $scope.favoriteNews = function($event, newsId, news) {
        $event.stopPropagation();
        $scope.reprintNews = news;
        if(!news.isCollection) {
            if($rootScope.normalGroup.length > 1) {
                $(".detailFavorite").show();
            }else {
                var groupId = $rootScope.normalGroup[0].id;
                detailAddFavorite($scope, $http, newsId, groupId, function(){
                    news.isCollection = true;
                    for(var n in $scope.dataList){
                        if(newsId == $scope.dataList[n].id){
                            $scope.dataList[n].isCollection = true;
                        }
                    }
                });
            }
        }else {
            delFavouriteNews($scope, $http, newsId, function(){
                news.isCollection = false;
                for(var n in $scope.dataList){
                    if(newsId == $scope.dataList[n].id){
                        $scope.dataList[n].isCollection = false;
                    }
                }
            });
        }
    };
    $scope.detailFavoriteGroup = function($event, newsId, groupId) {
        $event.stopPropagation();
        detailAddFavorite($scope, $http, newsId, groupId, function() {
            $(".detailFavorite").hide();
            $scope.reprintNews.isCollection = true;
            for(var n in $scope.dataList){
                if(newsId == $scope.dataList[n].id){
                    $scope.dataList[n].isCollection = true;
                }
            }
        })
    }
    angular.element(window).bind('click',function(){
        $(".detailFavorite").hide()
    });
    /*返回*/
    $scope.detailBack = function () {
        $state.go('dashboard');
    }
}
var getNewsListData = function($scope, $http){
    bjj.http.ng.get($scope, $http,'/api/capture/dashboard/reprint/moreNews', {
        pageSize        : $scope.psize,
        pageNo          : $scope.pno,
        queryId         : $scope.pno > 1 ? $scope.queryId : '',
        keyWords        : $scope.highlightSiteFilter.keywords || '',
        hot             : $scope.highlightSiteFilter.heat,
        startTime       : $scope.highlightSiteFilter.startTime,
        endTime         : $scope.highlightSiteFilter.endTime,
        orientation     : $scope.highlightSiteFilter.orientation,
        hasPic          : $scope.highlightSiteFilter.hasPic,
        order           : $scope.highlightSiteFilter.newsListOrder
    }, function (res) {
        $scope.highlightNewsListLoad = true;
        if(res.newsList.length == 0) {
            $scope.createTime = '无'
        }
        if(res.newsList.length > 0 && $scope.pno == 1) {
            $scope.createTime = res.newsList[0].createTime
        }
        if(res.newsList) {
            var list = res.newsList;
            if(list.length < ($(window).height() - 320)/ 161) {
                $scope.highlightSiteFilter.containerSiteBox = true;
            }else {
                $scope.highlightSiteFilter.containerSiteBox = false;
            }
        }
        $scope.readNewsId($scope.user.userName, 'reprintMediaMonitorMore', list);
        bjj.scroll.loadList.callback($scope, list);
        $scope.queryId = res.queryId;
        $scope.listEmptyMsg = res.msg;
        if($scope.pno == 11) {
            $scope.dataListBottomFinalView = true;
        }
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    }, 'highlightNewsLoading');
};
var gethighlightNewsDetail = function ($scope, $http, $sce, newsId) {
    $scope.captureNewsScrollTop = $('.bjj-cont-page').scrollTop();
    bjj.http.ng.get($scope, $http, '/api/capture/newsDetail/' + newsId, {}, function (res) {
        $scope.news = res.msg;
        $scope.news.content = $sce.trustAsHtml($scope.news.content);
        if($scope.news.newsType == 6) {
            $(".detail-content").css({"width":"640px","margin":"0 auto"})
        }else {
            $(".detail-content").css({"width":"100%"})
        }
        $('.dashboard-list-body').hide();
        $('.capture-news-detail-page').fadeIn();
        $('.bjj-cont-page').scrollTop(0);
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
}
var allAddFavorite = function($scope, $http, groupId, callback) {
    var ids = [];
    var newsIds = { "newsIds" : [] };
    _.forEach($scope.captureReprintNews, function (v, i) {
        ids.push($scope.captureReprintNews[i].id);
    });
    if (ids.length == 0){
        bjj.dialog.alert('danger', '请选中你要收藏的新闻！');
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
        _.forEach($scope.captureReprintNews, function (v, i) {
            for(var key in newsIds) {
                if(($scope.captureReprintNews[i].id) == newsIds[key]) {
                    $scope.captureReprintNews[i].isCollection = true;
                }
            }
            $scope.captureReprintNews[i].isCollection = true;
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