var accountSubscriptionCtrl = function ($rootScope, $scope, $http, $state, $stateParams) {
    bjj.http.ng.get($scope, $http, '/api/subscription/subject/subjectsList', {}, function (res) {
        $rootScope.subscriptionSubjectList = res.msg;
        if($rootScope.subscriptionSubjectList.length > 0){
            setTimeout(function () {
                $(".account-subscription-list .nav-list li").eq(0).addClass('active');
                $state.go('accountSubscription.modify', { subjectId : res.msg[0].subjectId });
            }, 0);
        }
        $(".account-subscription-list .nav-list").on("click", "li", function() {
            $(this).addClass("active").siblings().removeClass("active");
        });
    })
    $scope.editSite = function ($event) {
        var _this = $event.target;
        var subjectId = _this.dataset.siteId;
        $state.go("accountSubscription.modify",{ subjectId: subjectId });
    }
};