var accountPushLogCtrl = function ($rootScope, $scope, $http, $state) {
    $scope.channels = [
        {'channel': '新闻', 'value': '1'},
        {'channel': '微信', 'value': '2'},
        {'channel': '微博', 'value': '3'},
        {'channel': '我的文稿', 'value': '4'}
    ];
    $scope.pushMethods = [
        {'value': '手动推送'},
        {'value': '自动推送'},
    ];
    var currentDate = new Date();
    $scope.startTime   =  new Date(new Date().setHours(0,0,0,0).valueOf()).Format("yyyy-MM-dd hh:mm:ss");
    $scope.endTime     =  new Date(currentDate.valueOf()).Format("yyyy-MM-dd hh:mm:ss");
    var start = {
        elem: '#siteStartTime',
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $scope.startTime = v;
            $scope.pno = 1;
            getPushNewsListData($scope, $http, $state);
        }
    };
    var end = {
        elem:"#siteEndTime",
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $scope.endTime = v;
            $scope.pno = 1;
            getPushNewsListData($scope, $http, $state);
        }
    };
    setTimeout(function(){
        laydate(start);
        laydate(end);
    },0);

    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.view-news-list', getPushNewsListData, 10);
    pushHistoryCount($scope, $http);
    pushUsers($scope, $http);
    getPushNewsListData($scope, $http, $state);

    $scope.pushNewsListDataClick = function() {
        $scope.pno = 1;
        getPushNewsListData($scope, $http, $state);
    };
    $scope.closeTitle = function() {
        $('.push-count-title').slideUp();
    };
};

var getPushNewsListData = function($scope, $http, $state){
    bjj.http.ng.get($scope, $http,'/api/capture/news/pushHistory', {
        pageSize        : $scope.psize,
        pageNo          : $scope.pno,
        pushUser       : $scope.pushUser,
        autoPushType    : $scope.autoPushType ,
        pushChannel      : $scope.pushChannel,
        startTime        : $scope.startTime,
        endTime          : $scope.endTime
    }, function (res) {
        $scope.count = res.count;
        if($scope.count==0) {
            $('.push-count-title').hide();
        }else {
            $('.push-count-title').show();
        }
        $scope.newsListLoad = true;
        var list = res.list;
        bjj.scroll.loadList.callback($scope, list);
        $scope.listEmptyMsg = res.msg;
        if($scope.dataListEmptyView) {
            $scope.dataListBottomFinalView = false;
        }
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    }, 'pushLogListLoading');
};
var pushHistoryCount = function($scope, $http){
    bjj.http.ng.get($scope, $http,'/api/capture/news/pushHistoryCount', {}, function (res) {
        $scope.teamPushCount = res.teamPushCount;
        $scope.userPushCount = res.userPushCount;
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    }, 'pushLogListLoading');
};
var pushUsers = function($scope, $http){
    bjj.http.ng.get($scope, $http,'/api/team/memberList', {}, function (res) {
        $scope.pushUsers = res.list;
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    });
};