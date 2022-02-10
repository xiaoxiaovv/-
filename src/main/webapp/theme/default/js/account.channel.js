/**
 * Author : YCSnail
 * Date : 2017-07-26
 * Email : liyancai1986@163.com
 */
var accountChannelCtrl = function ($scope, $http) {

    const _channelMap = { 1 : 'weibo', 2 : 'wechat', 3 : 'toutiao', 4 : 'qqom' };

    getChannelList($scope, $http);

    $scope.addChannel = function (channelType) {
        var list = _.filter($scope.channelList, {channelType: channelType});
        if(list.length >= 2) {
            bjj.dialog.alert('warning', '该账号最多绑定两个账号！');
            return;
        }

        bjj.http.ng.get($scope, $http, '/api/' + _channelMap[channelType] + '/oauth/url', {}, function (res) {
            var newTab = window.open('about:blank');
            newTab.location.href = res.url;
        });
    };
    $scope.delChannel = function () {
        var channelId = this.channel.id;
        bjj.dialog.confirm('你确定要取消授权吗？', function () {
            bjj.http.ng.del($scope, $http, '/api/share/channel/' + channelId, {}, function (res) {
                bjj.dialog.alert('success', '取消授权成功！', {
                    callback : function () {
                        $scope.updateList();
                    }
                });
            });
        });
    };
    $scope.updateList = function(){
        getChannelList($scope, $http);
    }
    $scope.splitChannelList = function () {
        $scope.list1 = _.filter($scope.channelList, {channelType: 1});
        $scope.list2 = _.filter($scope.channelList, {channelType: 2});
        $scope.list3 = _.filter($scope.channelList, {channelType: 3});
        $scope.list4 = _.filter($scope.channelList, {channelType: 4});
    };
};

var getChannelList = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/share/channels', {}, function (res) {
        $scope.channelList = res.list;
        $scope.splitChannelList();
    });
};
