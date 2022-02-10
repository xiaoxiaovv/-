/**
 * Created by guolimei on 2017/9/18.
 */
var loginSource = $.cookie('loginSource');
var messageNoticeCtrl = function ($scope, $http) {
    if (loginSource == 2) {
        $('#message-amount').hide();
    }
    bjj.scroll.loadList.init($scope, $http, '.bjj-content', '.message-continer', getMessages, 10);
    getMessages($scope, $http);
};
var getMessages = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/system/messages', {
        pageNo      : $scope.pno,
        pageSize    : $scope.psize,
        lastTime    : $scope.lastTime,
        prevTime    : $scope.prevTime,
        loginSource : loginSource
    }, function (res) {
        $scope.lastTime = res.lastTime;
        if(res.list.length > 0){
            $scope.prevTime = res.list[res.list.length-1].createTime;
        }
        var list = res.list;
        _.map(list, function (v, i) {
            v.content = v.content.replace('{appTelephone}', $scope.appInfo.telephone);
        });
        bjj.scroll.loadList.callback($scope, list);
        $scope.messageList = $scope.dataList;
        setTimeout(function () {
            $('.btn-tip-redirect').click(function(){
                var siteId = $(this).attr('value');
                goto('#!/account/site/modify?captureSiteId='+siteId)
            })
        },0);
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    });
};

