/**
 * Author : YCSnail
 * Date : 2017-07-24
 * Email : liyancai1986@163.com
 */
var captureSubjectCtrl = function ($rootScope, $scope, $http, $state, $stateParams) {
    var pageType = $stateParams.type;
    bjj.http.ng.get($scope, $http, '/api/capture/subjects ', {}, function (res) {
        $scope.subjectList = res.list;
        if(pageType == 'list' && $scope.subjectList.length > 0){
            $state.go('captureSubject.news', {
                subjectId : $scope.subjectList[0].subjectId,
                subjectName: $scope.subjectList[0].subjectName,
                randomNum   : Math.random()
            });
            setTimeout(function () {
                $(".bjj-cont-sidebar .subject-list .nav-list li").eq(0).addClass('active');
            }, 0);
        }
        setTimeout(function () {
            $(".bjj-cont-sidebar .subject-list").on("click", ".nav-list li", function() {
                $(this).addClass("active").siblings().removeClass("active");
            });
            $scope.loadSubjectCount();
        }, 0);
    }, 'captureSubjectLoading');

    $scope.loadSubjectCount = function () {
        var recountList = _.filter($scope.subjectList, function (v) {
            return (v.countTime == null
                || v.resetTime == null ||
                v.resetTime > v.countTime ||
                (new Date().getTime() - v.countTime > 10 * 60 * 1000));
        });
        for(let i = 0; i< recountList.length; i++) {
            setTimeout(function () {
                bjj.http.ng.get($scope, $http, '/api/capture/subject/' + recountList[i].subjectId + '/count', {}, function (res) {
                    recountList[i].count = res.msg.count;
                });
            }, (i + 1) * 1000);
        }
    };
    $scope.resetSubjectCount = function (subjectId) {
        var loginSource = $.cookie('loginSource');
        if (loginSource == 2){
            bjj.http.ng.del($scope, $http, '/api/capture/subject/' + subjectId + '/count', {}, function (res) {
                _.map($scope.subjectList, function (v) {
                    if (v.subjectId == subjectId) {
                        v.count = 0;
                        v.resetTime = new Date().getTime();
                    }
                });
            });
        }
    };

    $scope.subjectClick = function (subjectId, subjectName) {
        $scope.subjectId = subjectId;
        $scope.resetSubjectCount(subjectId)
        var randomNum = Math.random();
        $state.go('captureSubject.news', {subjectId: subjectId , subjectName: subjectName, randomNum : randomNum});
    };
    $scope.goAccountSubject = function() {
        $state.go('accountSubject', {
            captureSubjectId :  $scope.subjectId
        });
    }
};

