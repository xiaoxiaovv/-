var subscriptionCtrl = function ($rootScope, $scope, $http, $state, $stateParams) {
    bjj.http.ng.get($scope, $http, '/api/subscription/subject/subjectsList ', {}, function (res) {
        $scope.subscriptionSubjectList = res.msg;
        bjj.http.ng.get($scope, $http, '/api/subscription/subject/newsCount', {
            subjectId : $scope.subscriptionSubjectList[0].subjectId
        }, function (res) {
            var count = res.count;
            if($scope.subscriptionSubjectList.length > 0) {
                $state.go('subscription.newList', {
                    subjectId : $scope.subscriptionSubjectList[0].subjectId,
                    subjectName: $scope.subscriptionSubjectList[0].subjectName,
                    count       : count,
                    randomNum   : Math.random()
                });
            }
        });
        setTimeout(function () {
            $(".bjj-cont-sidebar .subscription-list .nav-list li").eq(0).addClass('active');
            $(".bjj-cont-sidebar .subscription-list").on("click", ".nav-list li", function() {
                $(this).addClass("active").siblings().removeClass("active");
            });
        }, 0);
    }, 'subscriptSubjectLoading')
    $scope.subjectClick = function (subjectId, subjectName) {
        var randomNum = Math.random();
        var subjectId = subjectId;
        var subjectName = subjectName;
        getCount($scope, $http, $state, subjectId, subjectName, randomNum);
    };
};

var getCount = function($scope, $http, $state, subjectId, subjectName, randomNum) {
    bjj.http.ng.get($scope, $http, '/api/subscription/subject/newsCount', {
        subjectId: subjectId
    }, function (res) {
        var count = res.count;
        $state.go('subscription.newList', {subjectId: subjectId , subjectName: subjectName, randomNum: randomNum, count : count});
    });
};