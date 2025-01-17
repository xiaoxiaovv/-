/**
 * Author : YCSnail
 * Date : 2017-07-27
 * Email : liyancai1986@163.com
 */
var weiboListCtrl = function ($rootScope, $scope, $http, $sce, $stateParams, $state) {
    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.containerBox', getWeiboNewsListData, 10);
    var siteList = _.filter($rootScope.siteList,{ siteType : 3 });
    var siteId = $stateParams.siteId;
    var siteName = $stateParams.siteName;

    if($rootScope.captureSiteFilter == undefined){
        $rootScope.captureSiteFilter = {
            siteId      : siteId,
            siteName    : siteName,
            heat        : 0,
            orientation : 0,
            hasPic      : 0,
            startTime   : '',
            endTime     : '',
        }
    }
    if($rootScope.captureWeiboFilter == undefined){
        $rootScope.captureWeiboFilter = {
            keywords      : ''
        }
    }
    $rootScope.captureSiteFilter.siteId = siteId;
    $rootScope.captureSiteFilter.siteName = siteName;
    if ( $stateParams.siteId == undefined) {
        $rootScope.captureSiteFilter.siteId = -3;
    }else {
        $rootScope.captureSiteFilter.siteId = $stateParams.siteId;
    }
    bjj.http.ng.get($scope, $http, '/api/capture/site/weiboKeyWords', {},function(res) {
        $scope.weiboKeyWords = res.weiboKeyWords;
        if($scope.weiboKeyWords.length == 0) {
            var weiboSiteSetting = { keyword: ''};
            $scope.weiboKeyWords.unshift(weiboSiteSetting)
        }
    })
    var weiboStart = {
        elem: '#weiboStartTime',
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $scope.captureSiteFilter.startTime = v;
            $scope.pno = 1;
            getWeiboNewsListData($scope, $http);
        }
    };
    var weiboEnd = {
        elem:"#weiboEndTime",
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $scope.captureSiteFilter.endTime = v;
            $scope.pno = 1;
            getWeiboNewsListData($scope, $http);
        }
    };
    setTimeout(function(){
        laydate(weiboStart);
        laydate(weiboEnd);
    },0);
    $scope.clearWeiBoTimeClick = function() {
        $('#weiboStartTime').val('');
        $('#weiboEndTime').val('');
        $scope.captureSiteFilter.startTime = '';
        $scope.captureSiteFilter.endTime = '';
        $scope.pno = 1;
        getWeiboNewsListData($scope, $http);
    };
    /*删除组*/
    $scope.deleteRecord = function(index){
        $scope.weiboKeyWords.splice(index,1);
        if($scope.weiboKeyWords.length == 0){
            var weiboSiteSetting = { keyword: ''};
            $scope.weiboKeyWords.unshift(weiboSiteSetting)
        }
    }
    /*添加组*/
    $scope.addRecord = function(){
        if($scope.weiboKeyWords.length >= 5){
            bjj.dialog.alert('success', '最多设置5个');
            return
        }
        var weiboSiteSetting ={ keyword: ''};
        $scope.weiboKeyWords.push(weiboSiteSetting)
    }
    /*保存组*/
    $scope.saveSetting = function(){
        var KeyWords = [];
        angular.forEach($scope.weiboKeyWords, function( v, i){
            KeyWords.push(v.keyword)
        })
        bjj.http.ng.put($scope, $http, '/api/capture/site/weiboKeyWords?', {
            weiboKeyWords: KeyWords
        }, function (response) {
            bjj.dialog.alert('success', '保存成功！', {
                callback:   function() {
                    $('#myModal').modal('hide')
                }
            });
        });
    }
    //热度
    $scope.newsHeatListClick = function($event){
        var heat = $event.target.dataset.heat;
        $rootScope.captureSiteFilter.heat = heat;
        $scope.pno = 1;
        getWeiboNewsListData($scope, $http)
    };
    //倾向性
    $scope.newsOrientationListClick = function($event){
        var orientation = $event.target.dataset.orientation;
        $rootScope.captureSiteFilter.orientation = orientation;
        $scope.pno = 1;
        getWeiboNewsListData($scope, $http)
    };
    //类别
    $scope.newsHasPicListClick = function($event){
        var hasPic = $event.target.dataset.hasPic;
        $rootScope.captureSiteFilter.hasPic = hasPic;
        $scope.pno = 1;
        getWeiboNewsListData($scope, $http)
    };
    //关键词
    $scope.keywordsClick = function($event,index){
        if($event.target.className == 'conditions weiboConditions ng-binding ng-scope') {
            if($scope.keywords != undefined) {
                var keywords = $event.target.dataset.hasKeywords;
                $scope.captureWeiboFilter.keywords = keywords + ' ' + $scope.keywords;
                $scope.pno = 1;
                getWeiboNewsListData($scope, $http)
            }else {
                var keywords = $event.target.dataset.hasKeywords;
                $scope.captureWeiboFilter.keywords = keywords;
                $scope.pno = 1;
                getWeiboNewsListData($scope, $http)
            }
        }else {
            if($scope.keywords != undefined) {
                $scope.captureSiteFilter.keywords = $scope.keywords;
                $scope.pno = 1;
                getWeiboNewsListData($scope, $http);
            }else {
                $scope.captureSiteFilter.keywords = '';
                $scope.pno = 1;
                getWeiboNewsListData($scope, $http);
            }
        }
        if(!$('.weiboConditions:eq('+index+')').hasClass('on')){
            $scope.captureWeiboFilter.keywords = '';
        }
    };
    $scope.pageSource = "captureSite.newsDetail";
    $scope.newsListOrder = 1;
    $scope.siteNewsListLoad = true;
    if($rootScope.captureSiteFilter != undefined){
        if($rootScope.captureSiteFilter.keywords != undefined && $rootScope.captureSiteFilter.keywords != ''){
            $scope.keywords = $rootScope.captureSiteFilter.keywords;
        }
        if($rootScope.captureSiteFilter.newsListOrder != undefined){
            $scope.newsListOrder = $rootScope.captureSiteFilter.newsListOrder;
        }
    }
    getWeiboNewsListData($scope, $http);

    //搜索
    $scope.keywordsSearchClick = function(){
        console.log($scope.keywords)
        if($('.news-condition .conditions .capture-weiboConditions .on').html() != undefined) {
            $scope.captureSiteFilter.keywords = $scope.keywords +' '+ $('.news-condition .conditions .capture-weiboConditions .on').html();
            $scope.captureWeiboFilter.keywords = $scope.keywords +' '+ $('.news-condition .conditions .capture-weiboConditions .on').html();
        }else {
            $scope.captureSiteFilter.keywords = $scope.keywords;
            $scope.captureWeiboFilter.keywords = $scope.keywords;
        }
        $scope.pno = 1;
        getWeiboNewsListData($scope, $http);
    };
    $scope.myKeyup = function(e) {
        if (e.keyCode == 13) {
            $scope.pno = 1;
            if($('.news-condition .conditions .capture-weiboConditions .on').html() != undefined) {
                $rootScope.captureSiteFilter.keywords = $scope.keywords +' '+ $('.news-condition .conditions .capture-weiboConditions .on').html();
                $rootScope.captureWeiboFilter.keywords = $scope.keywords +' '+ $('.news-condition .conditions .capture-weiboConditions .on').html();
            }else {
                $rootScope.captureSiteFilter.keywords = $scope.keywords;
                $rootScope.captureWeiboFilter.keywords = $scope.keywords;
            }
            if($scope.newsListLoad) {
                $scope.newsListLoad = false;
                getWeiboNewsListData($scope, $http);
            }

        };
    };
    //排序
    $scope.orderClick = function($event){
        var order = $event.target.dataset.order;
        $scope.newsListOrder = $event.target.dataset.order;
        $('#dropdown-toggle .order').text($event.target.innerHTML);
        $scope.pno = 1;
        $scope.captureSiteFilter.newsListOrder = order;
        getWeiboNewsListData($scope, $http);
    };
    /*单选*/
    $scope.toggleNewsList = function () {
        this.item.active = !this.item.active;
        $scope.active = false;
        $scope.weiboNews = _.filter($scope.dataList, {'active' : true});
        if($scope.weiboNews.length == $scope.dataList.length){
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
        $scope.weiboNews = _.filter($scope.dataList, {'active' : true});
    };
    /*批量收藏*/
    $scope.favoriteAll = function($event) {
        $(".listFavorite").hide();
        $(".news-favorite-box").hide();
        $event.stopPropagation();
        if($rootScope.normalGroup.length > 1) {
            $(".showGroup").show();
        }else {
            var groupId = $rootScope.normalGroup[0].id;
            allAddWeiboFavorite($scope, $http, groupId);
        }
    };
    $scope.favoriteAllGroup = function($event, groupId) {
        $event.stopPropagation();
        allAddWeiboFavorite($scope, $http, groupId, function() {
            $(".showGroup").hide();
        });
    };
    $scope.goAddSite = function() {
        $rootScope.fromRouter = false;
        $state.go('accountSite.add');
    }
    //收藏
    $scope.favoriteClick = function($event, newsIds, item) {
        var clickHeight = $event.pageY;
        var allHeight = $(".bjj-content").height();
        $scope.item = item;
        if(!item.isCollection) {
            if($scope.normalGroup.length < 8) {
                if( allHeight - clickHeight < $scope.normalGroup.length * 35) {
                    $('.news-favorite-box').css('margin-top','-'+($scope.normalGroup.length * 35 + 38) + 'px' )
                }else {
                    $('.news-favorite-box').css('margin-top', 0 )
                }
            }else {
                if( allHeight - clickHeight < 8 * 35) {
                    $('.news-favorite-box').css('margin-top','-'+( 8 * 35 + 38) + 'px' )
                }else {
                    $('.news-favorite-box').css('margin-top', 0 )
                }
            }
            $(".news-favorite-box").hide();
            $(".showGroup").hide();
            $event.stopPropagation();
            if($rootScope.normalGroup.length > 1) {
                $($event.target).next(".news-favorite-box").show()
                $($event.target).parent().next().children(".news-favorite-box").show();
            }else {
                var groupId = $rootScope.normalGroup[0].id;
                addFavorite($scope, $http, newsIds, groupId, function() {
                    item.isCollection = true;
                });
            }
        }else {
            delFavouriteNews($scope, $http, newsIds, function(){
                item.isCollection = false;
            });
        }
    };
    angular.element(window).bind('click',function(){
        $(".news-favorite-box").hide();
    });
    $scope.favoriteGroup = function($event, groupId) {
        $event.stopPropagation();
        var newsIds = $event.target.dataset.newsId;
        addFavorite($scope, $http, newsIds, groupId, function() {
            $(".id"+ newsIds ).hide();
            $scope.item.isCollection = true;
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
        _.forEach($scope.weiboNews, function (v, i) {
            ids.push($scope.weiboNews[i].id);
        });
        if (ids.length == 0){
            bjj.dialog.alert('danger', '请选中你要收藏的新闻！');
            return;
        };
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
                _.forEach($scope.weiboNews, function (v, i) {
                    for(var key in newsIds) {
                        if(($scope.weiboNews[i].id) == newsIds[key]) {
                            $scope.weiboNews[i].isPush = true;
                        }
                    }
                    $scope.weiboNews[i].isPush = true;
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
    $scope.gotoNewsDetail = function (newsId) {
        getCaptureWeiBoNewsDetail($rootScope, $scope, $http, $sce, newsId)
    };
    $rootScope.back2NewsList = function () {
        $('.capture-news-detail-page').hide();
        $('.capture-news-page').fadeIn();
        $('.bjj-cont-page').scrollTop($scope.captureNewsScrollTop);
    };
    if(siteId == -3) {
        setTimeout(function () {
            $(".site-left-cont .capture-list .weibo-site-list li").eq(0).addClass('active');
        },100);
    }
};
var getWeiboNewsListData = function($scope, $http){
    bjj.http.ng.get($scope, $http,'/api/capture/site/'+ $scope.captureSiteFilter.siteId +'/weiboNews', {
        pageSize        : $scope.psize,
        pageNo          : $scope.pno,
        queryId         : $scope.pno > 1 ? $scope.queryId : '',
        keyWords        : $scope.captureWeiboFilter.keywords,
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
        bjj.scroll.loadList.callback($scope, list);
        $scope.queryId = res.queryId;
        $scope.listEmptyMsg = res.msg;
        if($scope.dataListEmptyView) {
            $scope.dataListBottomFinalView = false;
        }

        setTimeout(function () {
            _.forEach(res.newsList, function(v, i){
                new Viewer(document.getElementById('weibo-list-img-viewer-' + v.id), { title: false });
            });

            $('.btn-tip-redirect').click(function(){
                var siteId = $(".btn-tip-redirect").attr("value");
                goto('#!/account/site/modify?captureSiteId='+siteId)
            })
        }, 0);
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    }, 'captureWeiboNewsLoading');
};
var getCaptureWeiBoNewsDetail = function ($rootScope, $scope, $http, $sce, newsId) {
    $scope.captureNewsScrollTop = $('.bjj-cont-page').scrollTop();
    bjj.http.ng.get($scope, $http, '/api/capture/news/' + newsId, {}, function (res) {
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
var allAddWeiboFavorite = function($scope, $http, groupId, callback) {
    if(callback) {
        callback();
    }
    var ids = [];
    var newsIds = { "newsIds" : [] };
    _.forEach($scope.weiboNews, function (v, i) {
        ids.push($scope.weiboNews[i].id);
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
        _.forEach($scope.weiboNews, function (v, i) {
            for(var key in newsIds) {
                if(($scope.weiboNews[i].id) == newsIds[key]) {
                    $scope.weiboNews[i].isCollection = true;
                }
            }
            $scope.weiboNews[i].isCollection = true;
        });
    }, function(res) {
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
