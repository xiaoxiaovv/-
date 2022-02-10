var accountMonitorCtrl = function ($rootScope, $scope, $http, $state) {
    bjj.http.ng.get($scope, $http, '/api/copyright/monitors', {}, function (res) {
        $rootScope.monitorList = res.msg.list;
        if ($rootScope.monitorList.length > 0) {
            $state.go('accountMonitor.modify', {monitorId: $rootScope.monitorList[0].monitorId});
            setTimeout(function () {
                $(".account-monitor-list .nav-list li").eq(0).addClass('active');
            }, 0);
        };
        $(".account-monitor-list .nav-list").on("click", "li", function() {
            $(this).addClass("active").siblings().removeClass("active");
        });
    }, function (res) {
        bjj.dialog.alert('warning', res.msg)
    }, 'copyrightMonitorLoading');
};