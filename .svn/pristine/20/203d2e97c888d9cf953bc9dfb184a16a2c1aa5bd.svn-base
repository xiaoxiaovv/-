/**
 * Author : YCSnail
 * Date : 2017-07-27
 * Email : liyancai1986@163.com
 */
var accountSubjectCtrl = function ($rootScope, $scope, $http, $state, $stateParams) {
    bjj.http.ng.get($scope, $http, '/api/capture/subjects', {}, function (res) {
        $rootScope.subjectList = res.list;
        if($stateParams.captureSubjectId) {
            _.forEach($rootScope.subjectList, function(v, i) {
                if(v.subjectId == $stateParams.captureSubjectId) {
                    setTimeout(function () {
                        $(".account-subject-list .nav-list li").eq(i).addClass("active");
                        $state.go("accountSubject.modify", { subjectId: v.subjectId });
                    }, 0);
                }
            })
        }else{
            if($rootScope.subjectList.length > 0){
                setTimeout(function () {
                    $(".account-subject-list .nav-list li").eq(0).addClass('active');
                    $state.go('accountSubject.modify', { subjectId : res.list[0].subjectId });
                }, 0);
            }
        }
        $(".account-subject-list .nav-list").on("click", "li", function() {
            $(this).addClass("active").siblings().removeClass("active");
        });
    }, function(res) {
    }, 'accountSubjectLoading');

    $scope.transmit = function($event) {
        var _this = $event.target;
        var subjectId = _this.dataset.siteId;
        $state.go("accountSubject.modify", { subjectId: subjectId });
    };
};
