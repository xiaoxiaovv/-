var subscriptionNewListCtrl = function ($rootScope, $scope, $http, $sce, $state, $stateParams) {
    var subjectId   = $stateParams.subjectId;
    var subjectName = $stateParams.subjectName;
    var count       = $stateParams.count;
    var randomNum   = Math.random();
    if($scope.subScriptionSubjectIdFilter == undefined){
        $scope.subScriptionSubjectIdFilter = {
            subjectId      : subjectId,
            subjectName    : subjectName,
            count           : count
        }
    }
    $scope.subScriptionSubjectIdFilter.subjectId = subjectId;
    $scope.subScriptionSubjectIdFilter.subjectName = subjectName;
    $scope.subjectName = subjectName;
    $scope.count        = count;
    $scope.newsListOrder = 1;
    $scope.newsListLoad = true;
    var isShowNewsPhoto = true;
    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.subscription-manage-list', getSubscriptionNewsListData, 10);
    getSubscriptionNewsListData($scope, $http);
    $scope.gotoNewsDetail = function ($event, newsId) {
        $rootScope.writeNewsId($rootScope.user.userName, newsId, 'subscription');
        getSubscriptionNewsDetail($rootScope, $scope, $http, $sce, newsId, subjectId, isShowNewsPhoto);
        $($event.target).parent().addClass('read');
    };
    $rootScope.back2NewsList = function () {
        $('.capture-news-detail-page').hide();
        $('.subscript-news-page').fadeIn();
        $('.bjj-cont-page').scrollTop($scope.captureNewsScrollTop);
    };
};
var getSubscriptionNewsListData = function($scope, $http){
    bjj.http.ng.get($scope, $http,'/api/subscription/subject/'+  $scope.subScriptionSubjectIdFilter.subjectId +'/news' , {
        pageSize        : $scope.psize,
        pageNo          : $scope.pno
    }, function (res) {
        $scope.newsListLoad = true;
        var list = res.list;
        for(var key in list) {
            if(list[key].news.hasImg == true&&list[key].news.imgUrls[0] == null) {
                list[key].news.imgUrls[0] = [];
            }
        }
        if(list.length > 0) {
            $('.subscription-manage-list .push-count').show();
        }
        $scope.newsListLoad = true;
        var key = $scope.user.userName + "_subscription";
        var newsIds = JSON.parse(localStorage.getItem(key));
        if(newsIds) {
            _.forEach(newsIds,function(v) {
                _.forEach(list,function(value) {
                    if(v == value.newsId) {
                        value.isRead = true;
                    }
                })
            })
        }
        bjj.scroll.loadList.callback($scope, list);
        $scope.listEmptyMsg = res.msg;
        if($scope.dataListEmptyView) {
            $scope.dataListBottomFinalView = false;
        }
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    }, 'subscriptionNewsLoading');
};
var getSubscriptionNewsDetail = function ($rootScope, $scope, $http, $sce, newsId, subjectId, isShowNewsPhoto) {
    $scope.captureNewsScrollTop = $('.bjj-cont-page').scrollTop();
    bjj.http.ng.get($scope, $http, '/api/subscription/news/newsDetail', {
        newsId    : newsId,
        subjectId : subjectId
    }, function (res) {
        $rootScope.news = res.msg.news;
        $rootScope.news.content = $sce.trustAsHtml($rootScope.news.content);
        if($scope.news.newsType == 6) {
            $(".detail-content").css({"width":"640px","margin":"0 auto"})
        }else {
            $(".detail-content").css({"width":"100%"})
        }
        $('.subscript-news-page').hide();
        $('.capture-news-detail-page').fadeIn();
        $('.bjj-cont-page').scrollTop(0);
        if(isShowNewsPhoto) {
            $('.news-photo').hide();
        }else {
            $('.news-photo').show();
        }
    });
};