var dashboardBranchCtrl = function ($rootScope, $scope, $http, $state) {
    $scope.updateTime              = new Date;
    $scope.isShowEmpty             = false;
    $scope.hasHighlightNews        = true;
    $scope.hasHotNews              = true;
    $scope.hasWechatNews           = true;
    $scope.hasSubjectNews          = true;
    $scope.hasWeiboNews            = true;
    $scope.hasRiseRateMonitor      = true;
    $scope.hasReprintMediaMonitor  = true;
    $scope.imgNewsShow             = false;
    $scope.dashboardFavoriteGroup = function($event, groupId) {
        $(".newsFavoriteBox").hide();
        $event.stopPropagation();
        var newsIds = $event.target.dataset.newsId;
        dashboardFavorite($scope, $http, newsIds, groupId, function() {
            $(".id"+ newsIds).hide();
            $scope.item.isCollection = true;
        })
    };
    angular.element(window).bind('click',function(){
        $(".newsFavoriteBox").hide();
    });
    /*设置站点*/
    $scope.highlightSet =function($event){
        $(".site-group").hide();
        $(".newsFavoriteBox").hide();
        var module   = 'highlightNews';
        var list     = $scope.highlightNewsList;
        var listNull = module + '-null';
        var listShow =  module + '-content';
        dashboardSet($scope, $http, $state, $event, module, list, listNull, listShow, getHighlightNews);
    }
    $scope.hotNewsSet = function($event){
        $(".site-group").hide();
        $(".newsFavoriteBox").hide();
        var module   = 'hotNews';
        var list     = $scope.hotNewsSiteList;
        var listNull = module + '-null';
        var listShow =  module + '-content';
        dashboardSet($scope, $http, $state, $event, module, list, listNull, listShow, getHotNewsList);
    };
    $scope.subjectNewsSet =function($event){
        $(".site-group").hide();
        $(".newsFavoriteBox").hide();
        var module = 'subjectNews';
        var list     = $scope.subjectSiteList;
        var listNull = module + '-null';
        var listShow =  module + '-content';
        dashboardSet($scope, $http, $state, $event, module, list, listNull, listShow, getSubjectNews);
    };
    $scope.weiboNewsSet = function($event) {
        $(".site-group").hide();
        $(".newsFavoriteBox").hide();
        var module = 'weiboNews';
        var list     = $scope.weiboSiteList;
        var listNull = module + '-null';
        var listShow =  module + '-content';
        dashboardSet($scope, $http, $state, $event, module, list, listNull, listShow, getWeiBoNews);
    };
    $scope.wechatNewsSet =function($event){
        $(".site-group").hide();
        $(".newsFavoriteBox").hide();
        var module = 'wechatNews';
        var list     = $scope.highLightWeChartList;
        var listNull = module + '-null';
        var listShow =  module + '-content';
        dashboardSet($scope, $http, $state, $event, module, list, listNull, listShow, getWechatList);
    };
    $scope.riseRateSet =function($event){
        $(".site-group").hide();
        $(".newsFavoriteBox").hide();
        var module = 'riseRateMonitor';
        var list     = $scope.riseRateMonitorList;
        var listNull = module + '-null';
        var listShow =  module + '-content';
        dashboardSet($scope, $http, $state, $event, module, list, listNull, listShow, getRiseNews);
    };
    $scope.reprintSet =function($event){
        $(".site-group").hide();
        $(".newsFavoriteBox").hide();
        var module = 'reprintMediaMonitor';
        var list     = $scope.reprintMediaMonitorList;
        var listNull = module + '-null';
        var listShow =  module + '-content';
        dashboardSet($scope, $http, $state, $event, module, list, listNull, listShow, getReprintNews);
    };
    $scope.weiboItemList = function($event) {
        var randomNum = Math.random();
        $state.go('captureSite.weibolist', {
            currentSiteId   : $event.target.dataset.siteId,
            siteId      : $event.target.dataset.siteId,
            siteName    : $event.target.dataset.siteName,
            randomNum   : randomNum,
            siteType    : 3
        });
    };
    /*修改站点名称*/
    $scope.toggleSite = function() {
        this.item.active = !this.item.active;
    };
    $scope.subjectSitesSet = function() {
        $scope.subjectNewsShow   = false;
        getSiteList($scope, $http);
    };
    $scope.highLightWeChartSet = function() {
        $scope.weChatNewsShow      = true;
        getSiteList($scope, $http);
    };
    $scope.wechatNewsClick = function($event, newsId) {
        $rootScope.writeNewsId($rootScope.user.userName, newsId, 'wechatNews');
        $($event.target).parent().addClass('read');
    }
    $scope.subjectNewsClick = function($event, newsId) {
        $rootScope.writeNewsId($rootScope.user.userName, newsId, 'subjectNews');
        $($event.target).parent().addClass('read');
    }
    $scope.hotNewsClick = function($event, newsId) {
        $rootScope.writeNewsId($rootScope.user.userName, newsId, 'hotNews');
        $($event.target).parent().addClass('read');
    }
    $scope.riseRateMonitorClick = function($event, newsId) {
        $rootScope.writeNewsId($rootScope.user.userName, newsId, 'riseRateMonitor');
        $($event.target).parent().addClass('read');
    }
    $scope.reprintMediaMonitorClick = function($event, newsId) {
        $rootScope.writeNewsId($rootScope.user.userName, newsId, 'reprintMediaMonitor');
        $($event.target).parent().addClass('read');
    }
};
/*获得热点新闻*/
var getHotNewsList = function($scope, $http, $state, siteIds) {
    bjj.http.ng.postBody($scope, $http, '/api/capture/dashboard/hot/news', {
        siteIds : siteIds
    }, function (res) {
        $scope.imageNewsList = res.imageNewsList;
        $scope.hotNewsList = res.newsList;
        $scope.arrImg  = [];
        $scope.hotNewsArrId   = [];
        $scope.readNewsId($scope.user.userName, 'hotNews', $scope.hotNewsList);
        angular.forEach(res.imageNewsList, function(data){
            $scope.hotNewsArrId.push(data.id);
        });
        if($scope.hotNewsList.length == 0) {
            $('.hotNews-null').show();
            $('.hotNews-content').hide();
            return;
        }else {
            $('.hotNews-null').hide();
            $('.hotNews-content').show();
        }
        if($scope.imageNewsList.length > 0) {
            if($scope.imageNewsList[0] == undefined ) {
                $scope.isShowEmptyBigImage = true;
            }else {
                $scope.isShowEmptyBigImage = false;
            }
            if($scope.imageNewsList[1] == undefined ) {
                $scope.isShowEmptySmallLeftImage = true;
            }else {
                $scope.isShowEmptySmallLeftImage = false;
            }
            if($scope.imageNewsList[2] == undefined ) {
                $scope.isShowEmptySmallRightImage = true;
            }else {
                $scope.isShowEmptySmallRightImage = false;
            }
            if($scope.imageNewsList.length >= 1) {
                $('.hot-news-list .right-images .first-images a').css({'backgroundImage' : 'url('+$scope.imageNewsList[0].cover+')'});
            }
            if($scope.imageNewsList.length >= 2) {
                $('.hot-news-list .right-images .second-images .second-left-images .left-image a').css({'backgroundImage' : 'url('+$scope.imageNewsList[1].cover+')'});
            }
            if($scope.imageNewsList.length == 3) {
                $('.hot-news-list .right-images .second-images .second-right-images .right-image a').css({'backgroundImage' : 'url('+$scope.imageNewsList[2].cover+')'});
            }
        }
        if($scope.imageNewsList.length == 0) {
            $scope.isShowEmptyBigImage = true;
            $scope.isShowEmptySmallRightImage = true;
            $scope.isShowEmptySmallLeftImage = true;
        }
    },'hotNewsLoading');
};
/*获得全网监控*/
var getSubjectNews = function($scope, $http, $state, subjectIds) {
    bjj.http.ng.get($scope, $http, '/api/capture/dashboard/subject/news', {
        subjectIds : subjectIds
    }, function (res) {
        $scope.subjectImageNewsList = res.imageNewsList;
        $scope.subjectNewsList = res.newsList;
        $scope.arrImg  = [];
        $scope.subjectNewsArrId   = [];
        $scope.readNewsId($scope.user.userName, 'subjectNews', $scope.subjectNewsList);
        angular.forEach($scope.subjectImageNewsList, function(data){
            $scope.subjectNewsArrId.push(data.id);
        });
        if($scope.subjectNewsList.length == 0) {
            $('.subjectNews-null').show();
            $('.subjectNews-content').hide();
            return;
        }else {
            $('.subjectNews-null').hide();
            $('.subjectNews-content').show();
        }
        if($scope.subjectImageNewsList.length > 0) {
            if($scope.subjectImageNewsList[0] == undefined ) {
                $scope.isShowSubjectNewsEmptyBigImage = true;
            }else {
                $scope.isShowSubjectNewsEmptyBigImage = false;
            }
            if($scope.subjectImageNewsList[1] == undefined ) {
                $scope.isShowSubjectNewsEmptySmallLeftImage = true;
            }else {
                $scope.isShowSubjectNewsEmptySmallLeftImage = false;
            }
            if($scope.subjectImageNewsList[2] == undefined ) {
                $scope.isShowSubjectNewsEmptySmallRightImage = true;
            }else {
                $scope.isShowSubjectNewsEmptySmallRightImage = false;
            }
            if($scope.subjectImageNewsList.length >= 1) {
                $('.subject-news-list .right-images .first-images a').css({'backgroundImage' : 'url('+$scope.subjectImageNewsList[0].cover+')'});
            }
            if($scope.subjectImageNewsList.length >= 2) {
                $('.subject-news-list .right-images .second-images .second-left-images .left-image a').css({'backgroundImage' : 'url('+$scope.subjectImageNewsList[1].cover+')'});
            }
            if($scope.subjectImageNewsList.length == 3) {
                $('.subject-news-list .right-images .second-images .second-right-images .right-image a').css({'backgroundImage' : 'url('+$scope.subjectImageNewsList[2].cover+')'});
            }
        }

        if($scope.subjectImageNewsList.length == 0) {
            $scope.isShowSubjectNewsEmptyBigImage = true;
            $scope.isShowSubjectNewsEmptySmallLeftImage = true;
            $scope.isShowSubjectNewsEmptySmallRightImage = true;
        }
    },'subjectNewsLoading');
};
/*获得微博*/
var getWeiBoNews = function($scope, $http, $state, weiboSiteId) {
    bjj.http.ng.postBody($scope, $http, '/api/capture/dashboard/weibo/news', {
        siteIds : weiboSiteId
    }, function (res) {
        $scope.weiboNewsList  = res.newsList;
        if($scope.weiboNewsList.length == 0) {
            $('.weiboNews-null').show();
            $('.weiboNews-content').hide();
        }else{
            $('.weiboNews-null').hide();
            $('.weiboNews-content').show();
        }
        $scope.weiboNewsList = _.chunk(res.newsList, 3);
        $scope.weiboImagesUrl = [];

        setTimeout(function () {
            _.forEach(res.newsList, function(v, i){
                $scope.weiboImagesUrl.push(v.imgUrls);
                new Viewer(document.getElementById('img-viewer-' + v.id), { title: false });
            });
        }, 0);
    }, 'weiboNewsLoading');
};
/*获得热门微信*/
var getWechatList = function($scope, $http, $state, weChartSiteIds) {
    bjj.http.ng.postBody($scope, $http, '/api/capture/dashboard/wechat/news', {
        siteIds : weChartSiteIds
    }, function (res) {
        $scope.weChatimageNewsList = res.imageNewsList;
        $scope.highWeChartList = res.newsList;
        $scope.arrImg  = [];
        $scope.weChatArrId   = [];
        $scope.readNewsId($scope.user.userName, 'wechatNews', $scope.highWeChartList);
        angular.forEach($scope.weChatimageNewsList, function(data){
            $scope.weChatArrId.push(data.id);
        });
        if($scope.highWeChartList.length == 0) {
            $('.weChatNews-null').show();
            $('.weChatNews-content').hide();
            return;
        }else {
            $('.weChatNews-null').hide();
            $('.weChatNews-content').show();
        }
        if(res.imageNewsList.length > 0) {
            if($scope.weChatimageNewsList[0] == undefined ) {
                $scope.isShowWeChatNewsEmptyBigImage = true;
            }else {
                $scope.isShowWeChatNewsEmptyBigImage = false;
            }
            if($scope.weChatimageNewsList[1] == undefined ) {
                $scope.isShowWeChatNewsEmptySmallLeftImage = true;
            }else {
                $scope.isShowWeChatNewsEmptySmallLeftImage = false;
            }
            if($scope.weChatimageNewsList[2] == undefined ) {
                $scope.isShowWeChatNewsEmptySmallRightImage = true;
            }else {
                $scope.isShowWeChatNewsEmptySmallRightImage = false;
            }
            if($scope.weChatimageNewsList.length >= 1) {
                $('.wechat-news-list .right-images .first-images a').css({'backgroundImage' : 'url('+$scope.weChatimageNewsList[0].cover+')'});
            }
            if($scope.weChatimageNewsList.length >= 2) {
                $('.wechat-news-list .right-images .second-images .second-left-images .left-image a').css({'backgroundImage' : 'url('+$scope.weChatimageNewsList[1].cover+')'});
            }
            if($scope.weChatimageNewsList.length == 3) {
                $('.wechat-news-list .right-images .second-images .second-right-images .right-image a').css({'backgroundImage' : 'url('+$scope.weChatimageNewsList[2].cover+')'});
            }
        }
        if($scope.highWeChartList.length == 0) {
            $scope.isShowWeChatNewsEmptyBigImage = true;
            $scope.isShowWeChatNewsEmptySmallLeftImage = true;
            $scope.isShowWeChatNewsEmptySmallRightImage = true;
        }
    }, 'wechatNewsLoading');
};
/*获得头条数据*/
var getHighlightNews = function ($scope, $http, $state, highNews) {
    bjj.http.ng.get($scope, $http, '/api/capture/dashboard/highlight/news', {
        siteIds: highNews,
        pageNo: 1,
        pageSize: 12
    }, function (res) {
        if(res.newsList.length == 0) {
            $('.highlightNews-null').show();
            $('.highlightNews-content').hide();
        }
        var flipSets = [];
        $("#leftRotate").children(".hexaflip-cube").remove();
        $("#middleRotate").children(".hexaflip-cube").remove();
        $("#rightRotate").children(".hexaflip-cube").remove();
        angular.forEach(res.newsList, function( v, i){
            flipSets.push(
                '<div class="newsContent" data-class="' + v.id + '">' +
                '<div class="titleTop">'+
                '<i class="site_title">'+ v.siteName + '-首页要闻</i>' +
                '<small class="label label-warning">top</small>' +
                '</div>'+
                '<span class="pull-right small-none">' + v.publishTime + '</span>' +
                '<i class="fa fa-clock-o pull-right" aria-hidden="true"></i>' +
                '<p class="bjj-ellipsis-2 contentAbstract">【' + v.title + '】' + v.contentAbstract + '</p>' +
                '</div>');
        });

        var leftRotate, middleRotate, rightRotate;
        var flipOptions = { size: 108 };
        leftRotate =    new HexaFlip(document.getElementById('leftRotate'),     {set: _.filter(flipSets, function (v, i) {return i % 3 == 0;}) }, flipOptions);
        middleRotate =  new HexaFlip(document.getElementById('middleRotate'),   {set: _.filter(flipSets, function (v, i) {return i % 3 == 1;}) }, flipOptions);
        rightRotate =   new HexaFlip(document.getElementById('rightRotate'),    {set: _.filter(flipSets, function (v, i) {return i % 3 == 2;}) }, flipOptions);

        $("#leftRotate .hexaflip-front").css("background",'#993333');
        $("#leftRotate .hexaflip-bottom").css("background",'#993333');
        $("#leftRotate .hexaflip-back").css("background",'#335c33');
        $("#leftRotate .hexaflip-top").css("background",'#ad855c');
        $("#middleRotate .hexaflip-front").css("background",'#ad855c');
        $("#middleRotate .hexaflip-bottom").css("background",'#ad855c');
        $("#middleRotate .hexaflip-back").css("background",'#993333');
        $("#middleRotate .hexaflip-top").css("background",'#335c33');
        $("#rightRotate .hexaflip-front").css("background",'#335c33');
        $("#rightRotate .hexaflip-bottom").css("background",'#335c33');
        $("#rightRotate .hexaflip-back").css("background",'#ad855c');
        $("#rightRotate .hexaflip-top").css("background",'#993333');
        $(".hexaflip-left").hide();
        $(".hexaflip-right").hide();
        var rotateTime;
        rotateTime = setInterval(function(){
            leftRotate.flipBack();
            middleRotate.flipBack();
            rightRotate.flipBack();
        }, 5000);
        var heightRotate = $(".everyList").css('width');
        $(".hexaflip-cube").css('width',heightRotate);
        window.onblur = function() {
            clearInterval(rotateTime);
            window.onfocus = function() {
                rotateTime = setInterval(function(){
                    leftRotate.flipBack();
                    middleRotate.flipBack();
                    rightRotate.flipBack();
                }, 5000);
            }
        }
        $(".everyList").on("click", '.newsContent', function() {
            $scope.toDetailId = $(this).attr('data-class');
            $state.go('dashboardNewsDetail', { newsId: $scope.toDetailId});
        });
    });
};
/*获得上升最快数据*/
var getRiseNews = function($scope, $http, $state, riseNews) {
    bjj.http.ng.postBody($scope, $http, '/api/capture/dashboard/rise/news', {
        siteIds: riseNews,
        pageNo: 1,
        pageSize: 5,
        startTime: new Date(new Date().getTime() -  0.5 * 60 * 60 * 1000).Format("yyyy-MM-dd hh:mm:ss"),
        endTime: new Date().Format('yyyy-MM-dd hh:mm:ss')
    }, function (res) {
        $scope.riseNewsList = res.newsList;
        if($scope.riseNewsList.length == 0) {
            $('.riseRateMonitor-null').show();
            $('.riseRateMonitor-content').hide();
        }
        $scope.readNewsId($scope.user.userName, 'riseRateMonitor', $scope.riseNewsList);
    },'riseNewsLoading')
}
/*获得热点预测数据*/
var getReprintNews = function($scope, $http, $state, reprintMediaNewsIds) {
    bjj.http.ng.postBody($scope, $http, '/api/capture/dashboard/reprint/news', {
        siteIds: reprintMediaNewsIds,
        pageNo: 1,
        pageSize: 5,
        startTime: new Date(new Date().getTime() -  60 * 60 * 1000).Format("yyyy-MM-dd hh:mm:ss"),
        endTime: new Date().Format('yyyy-MM-dd hh:mm:ss')
    }, function (res) {
        $scope.reprintNewsList = res.newsList;
        $scope.readNewsId($scope.user.userName, 'reprintMediaMonitor', $scope.reprintNewsList);
        if(reprintMediaNewsIds.length == 0) {
            $scope.msg = '查询无数据';
        }else if(reprintMediaNewsIds.length != 0 && $scope.reprintNewsList.length == 0) {
            $scope.msg = '60分钟内数据无更新';
        }
        if($scope.reprintNewsList.length == 0) {
            $('.reprintMediaMonitor-null').show();
            $('.reprintMediaMonitor-content').hide();
        }
    },'reprintNewsLoading')
};
/*获得政协动态*/
var getPpcNews = function($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/capture/dashboard/ppc/news', {}, function (res) {
        $scope.titleNewsList = res.news.titleNews;
        $scope.picNewsList = res.news.picNews;
        if($scope.picNewsList) {
            $scope.imgNewsShow = true;
        }
    },'ppcNewsLoading')
};
/*设置列表的站点*/
var getSiteList = function($rootScope, $scope, $http, $state) {
    var weiboSiteId         = [];
    var highlightNewsIds    = [];
    var siteIds             = [];
    var weChartSiteIds      = [];
    var subjectIds          = [];
    var riseNewsIds         = [];
    var reprintMediaNewsIds = [];
    bjj.http.ng.get($scope, $http, '/api/custom/dashboard/module/setting', {}, function (res) {
        $scope.weiboSiteList            = res.weiboNews;
        $scope.highlightNewsList        = res.highlightNews;
        $scope.hotNewsSiteList          = res.hotNews;
        $scope.subjectSiteList          = res.subjectNews;
        $scope.highLightWeChartList     = res.wechatNews;
        $scope.riseRateMonitorList      = res.riseRateMonitor;
        $scope.reprintMediaMonitorList  = res.reprintMediaMonitor;
        $scope.hasHighlightNews = true;
        if($scope.appInfo.appKey == 'ppc') {
            $scope.hasHighlightNews = false;
        }
        if($scope.hotNewsSiteList.length == 0 ) {
            $scope.hasHotNews = false;
        }
        if($scope.highLightWeChartList.length == 0 ) {
            $scope.hasWechatNews = false;
        }
        if($scope.subjectSiteList.length == 0 ) {
            $scope.hasSubjectNews = false;
        }
        if($scope.riseRateMonitorList.length == 0 ) {
            $scope.hasRiseRateMonitor = false;
        }
        if($scope.reprintMediaMonitorList.length == 0 ) {
            $scope.hasReprintMediaMonitor = false;
        }
        if($scope.weiboSiteList.length == 0 ) {
            $scope.hasWeiboNews = false;
        }
        angular.forEach($scope.weiboSiteList, function(data){
            if(data.active == true) {
                weiboSiteId.push(data.siteId);
            }
        });
        angular.forEach($scope.highlightNewsList, function(data){
            if(data.active == true) {
                highlightNewsIds.push(data.siteId);
            }
        });
        angular.forEach($scope.hotNewsSiteList, function(data){
            if(data.active == true) {
                siteIds.push(data.siteId);
            }
        });
        $scope.highWeChartList        = res.wechatNews;
        angular.forEach($scope.subjectSiteList, function(data){
            if(data.active == true) {
                subjectIds.push(data.subjectId);
            }
        });
        angular.forEach($scope.highLightWeChartList, function(data){
            if(data.active == true) {
                weChartSiteIds.push(data.siteId);
            }
        });
        angular.forEach($scope.riseRateMonitorList, function(data){
            if(data.active == true) {
                riseNewsIds.push(data.siteId);
            }
        });
        angular.forEach($scope.reprintMediaMonitorList, function(data){
            if(data.active == true) {
                reprintMediaNewsIds.push(data.siteId);
            }
        });
        if($scope.weiboSiteList.every( item => item.active === true)) {
            $scope.weiboSiteActive = true;
        }else {
            $scope.weiboSiteActive = false;
        }
        if($scope.hotNewsSiteList.every( item => item.active === true)) {
            $scope.hotNewsActive = true;
        }else {
            $scope.hotNewsActive = false;
        }
        if($scope.subjectSiteList.every( item => item.active === true)) {
            $scope.subjectActive = true;
        }else {
            $scope.subjectActive = false;
        }
        if($scope.highLightWeChartList.every( item => item.active === true)) {
            $scope.wechatNewsActive = true;
        }else {
            $scope.wechatNewsActive = false;
        }
        if($scope.riseRateMonitorList.every( item => item.active === true)) {
            $scope.riseRateMonitorActive = true;
        }else {
            $scope.riseRateMonitorActive = false;
        }
        if($scope.reprintMediaMonitorList.every( item => item.active === true)) {
            $scope.reprintMediaMonitorActive = true;
        }else {
            $scope.reprintMediaMonitorActive = false;
        }
        riseNewsIds         = riseNewsIds.join(',');
        reprintMediaNewsIds = reprintMediaNewsIds.join(',');
        highlightNewsIds    = highlightNewsIds.join(',');
        siteIds             = siteIds.join(',');
        subjectIds          = subjectIds.join(',');
        weChartSiteIds      = weChartSiteIds.join(',');
        weiboSiteId         = weiboSiteId.join(',');
        if(weiboSiteId.length == 0) {
            $('.weiboNews-null').show();
            $('.weiboNews-content').hide();
        }else {
            $('.weiboNews-null').hide();
            $('.weiboNews-content').show();
        }
        if(siteIds.length == 0) {
            $('.hotNews-null').show();
            $('.hotNews-content').hide();
        }else {
            $('.hotNews-null').hide();
            $('.hotNews-content').show();
        }
        if(weChartSiteIds.length == 0) {
            $('.weChatNews-null').show();
            $('.weChatNews-content').hide();
        }else {
            $('.weChatNews-null').hide();
            $('.weChatNews-content').show();
        }
        if(subjectIds.length == 0) {
            $('.subjectNews-null').show();
            $('.subjectNews-content').hide();
        }else {
            $('.subjectNews-null').hide();
            $('.subjectNews-content').show();
        }
        if(riseNewsIds.length == 0) {
            $(".riseRateMonitor-null").show();
            $(".riseRateMonitor-content").hide();
        }else {
            $(".riseRateMonitor-null").hide();
            $(".riseRateMonitor-content").show();
        }
        if(reprintMediaNewsIds.length == 0) {
            $('.reprintMediaMonitor-null').show();
            $('.reprintMediaMonitor-content').hide();
        }else {
            $('.reprintMediaMonitor-null').hide();
            $('.reprintMediaMonitor-content').show();
        }
        $scope.subjectSiteList      = res.subjectNews;
        $scope.highLightWeChartList = res.wechatNews;

        _.forEach(_.filter($scope.dashboardModuleList, {'isShow': true}), function (v) {
            switch (v.dashboard) {
                case 'highlightNews' :
                    getHighlightNews($scope, $http, $state, highlightNewsIds);
                    break;
                case 'hotNews' :
                    getHotNewsList( $scope, $http, $state, siteIds);
                    break;
                case 'subjectNews' :
                    getSubjectNews($scope, $http, $state, subjectIds);
                    break;
                case 'weiboNews' :
                    getWeiBoNews($scope, $http, $state, weiboSiteId);
                    break;
                case 'wechatNews' :
                    getWechatList ($scope, $http, $state, weChartSiteIds);
                    break;
                case 'riseRateMonitor' :
                    getRiseNews($scope, $http, $state, riseNewsIds);
                    break;
                case 'reprintMediaMonitor' :
                    getReprintNews($scope, $http, $state, reprintMediaNewsIds);
                    break;
                case 'ppcNews' :
                    getPpcNews($scope, $http);
                    break;
            }
        });
    });
};
var dashboardSet = function($scope, $http, $state, $event, module, list, listNull, listShow, callback) {
    var siteId = [];
    $($event.target).next(".site-group").show();
    $scope.isGetList = true;
    $event.stopPropagation();
    angular.element(window).bind('click',function(event){
        if(event.target.className == 'siteName bjj-ellipsis ng-binding ng-scope' || event.target.className == 'fa fa-square-o fa-lg active' || event.target.className == 'fa fa-square-o fa-lg') {
        }else {
            if($scope.isGetList ) {
                $(".site-group").hide();
                var siteIds = [];
                angular.forEach(list, function (v, i) {
                    if(v.active == true) {
                        siteIds.push(v.siteId);
                    }
                    if(list == $scope.subjectSiteList) {
                        if(v.active == true) {
                            siteIds.push(v.subjectId);
                        }
                    }
                });
                if(siteIds.length == 0) {
                    $("." + listNull).show();
                    $("." + listShow).hide();
                }else {
                    $("." + listNull).hide();
                    $("." + listShow).show();
                }
                angular.forEach(siteIds, function (v, i) {
                    if(v!= undefined) {
                        siteId.push(v);
                        siteIds = siteId ;
                    }
                });
                siteIds = siteIds.join(',');
                bjj.http.ng.putBody($scope, $http, "/api/custom/dashboard/"+ module +"/setting", {
                    ids : siteIds
                }, function (res) {
                });
                callback($scope, $http, $state, siteIds);
                $scope.isGetList = false;
                angular.element(window).unbind();
            }
        }
        $('window').unbind('click')
    });
};
