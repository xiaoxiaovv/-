/**
 * Author : YCSnail
 * Date : 2017-07-25
 * Email : liyancai1986@163.com
 */
var copyrightCtrl = function ($scope, $http, $state, $stateParams) {

    var pageType = $stateParams.type;
    bjj.http.ng.get($scope, $http, '/api/copyright/monitors', {}, function (res) {
        $scope.copyrightList = res.msg.list;
        if(pageType == 'list' && $scope.copyrightList.length > 0){
            $state.go('copyright.monitor', { monitorId : $scope.copyrightList[0].monitorId });
            setTimeout(function () {
                $(".copyright-list .nav-list li").eq(0).addClass('active');
            }, 0);
        }
        $(".nav-list").on("click", "li", function() {
            $(this).addClass("active").siblings().removeClass("active");
        });
    }, 'copyrightLoading');
    $scope.monitorClick = function (monitorId) {
        $state.go('copyright.monitor', {monitorId: monitorId});
    };
};