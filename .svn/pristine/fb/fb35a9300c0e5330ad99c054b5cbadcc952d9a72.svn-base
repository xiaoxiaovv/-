var accountSubscriptionModifyCtrl = function ($rootScope, $scope, $http, $stateParams, $state) {
    $scope.subjectId = $stateParams.subjectId;
    $scope.subscriptionEditPage = true;
    $scope.EditPage = true;
    $scope.wechatType = false;
    $scope.webType = true;
    $scope.siteType = 1;
    if($scope.siteType == 1) {
        $scope.wechatType = false;
        $scope.webType = true;
        $scope.urlTypeShow = true;
        $scope.matchTypeShow = false;
        if($scope.urlType == '' || $scope.urlType == undefined) {
            $scope.urlType = '1';
        }
        if($scope.showON == true) {
            $scope.matchType = '2';
            $('#modifyModal .form-group .toggle').removeClass('toggle-OFF');
        }else {
            $scope.matchType = '1';
            $('#modifyModal .form-group .toggle').addClass('toggle-OFF');
        }
    }else {
        $scope.urlTypeShow = false;
        $scope.matchTypeShow = false;
    }
    if($scope.subjectId != 0) {
        getSubjectDetail($scope, $http);
        $scope.EditPage = true;
    }else {
        $scope.EditPage = false;
        $(".account-subscription-list .nav-list li").removeClass('active');
    }
    getallSites($scope, $http);
    var siteStartTime = {
        elem: '#siteStartTime',
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $scope.startTime = v;
        }
    };
    var siteEndTime = {
        elem:"#siteEndTime",
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $scope.endTime = v;
        }
    };
    setTimeout(function(){
        laydate(siteStartTime);
        laydate(siteEndTime);
    },0)
    $scope.addSite = function() {
        $scope.accountId = '';
        $scope.siteDomain = '';
        $scope.siteName = '';
        $scope.isSiteDomain = false;
        $scope.isAccountId = false;
        $scope.isSiteName = false;
    }
    $scope.saveSubscription = function() {
        var trueSubscription = _.filter($scope.allSite, {'active' : true});
        $scope.siteIds = [];
        _.forEach(trueSubscription, function(v) {
            $scope.siteIds.push(v.siteId)
        })
        $scope.siteIds = $scope.siteIds.join(',');
        console.log($scope.startTime)
        if($scope.startTime == undefined || $scope.startTime == '') {
            bjj.dialog.alert('danger', '请先选择开始时间');
            return;
        }
        if($scope.endTime == undefined || $scope.endTime == '') {
            bjj.dialog.alert('danger', '请先选择结束时间');
            return;
        }
        if($scope.subjectName == undefined || $scope.subjectName == '') {
            bjj.dialog.alert('danger', '请填写主题名称');
            return;
        }
        if($scope.siteIds == '') {
            bjj.dialog.alert('danger', '请选择站点');
            return;
        }
        var nowTime = new Date().getTime();
        var startTime = new Date($scope.startTime).getTime();
        var params = {
            subjectName : $scope.subjectName,
            expression: $scope.expression,
            excludeWords: $scope.excludeWords == undefined ? '' : $scope.excludeWords,
            siteIds: $scope.siteIds == undefined ? '' : $scope.siteIds,
            startTime: $scope.startTime,
            endTime: $scope.endTime
        }
        if($scope.subjectId == 0) {
            if(startTime < nowTime) {
                bjj.dialog.alert('danger', '开始时间不能小于当前时间');
                return;
            }
            bjj.http.ng.post($scope, $http, '/api/subscription/subject?_method=post', params, function (res) {
                bjj.dialog.alert('success', '保存成功');
                reloadPage();
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            });
        }else {
            if($scope.status == 2) {
                bjj.dialog.alert('danger', '主题已完成，拒绝更新');
                return;
            }
            bjj.http.ng.put($scope, $http, '/api/subscription/subject/'+$scope.subjectId, params, function (res) {
                bjj.http.ng.get($scope, $http, '/api/subscription/subject/subjectsList', {}, function (res) {
                    $rootScope.subscriptionSubjectList = res.msg;
                })
                bjj.dialog.alert('success', '修改成功');
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            });
        }
    }
    $scope.deleteSubscription = function() {
        bjj.dialog.confirm('确定删除主题吗？', function () {
            bjj.http.ng.del($scope, $http, '/api/subscription/subject?subjectId=' + $scope.subjectId, {}, function (res) {
                bjj.http.ng.get($scope, $http, '/api/subscription/subject/subjectsList', {}, function (res) {
                    $rootScope.subscriptionSubjectList = res.msg;
                    if($rootScope.subscriptionSubjectList.length == 0) {
                        $state.go("accountSubscription.modify",{ subjectId: 0 });
                    }
                    bjj.dialog.alert('success', '删除主题成功');
                    $scope.isSubscription = true;
                    $scope.subscriptionEditPage = false;
                });

            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            });
        })
    }
    $scope.deleteSite = function($event) {
        var _this = $event.target;
        var subjectId = _this.dataset.siteId;
        bjj.dialog.confirm('确定删除站点吗？', function () {
            bjj.http.ng.del($scope, $http, '/api/subscription/site?siteId=' + subjectId, {}, function (res) {
                getSubjectDetail($scope, $http);
                getallSites($scope, $http)
                bjj.dialog.alert('success', res.msg);
            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            });
        })
    }
    /*单选*/
    $scope.toggleSubscription = function () {
        this.site.active = !this.site.active;
    };
    $scope.choiceType = function(){
        $scope.isSiteDomain = false;
        $scope.isAccountId = false;
        $scope.isSiteName = false;
        $scope.siteName = '';
        if($scope.siteType == 1) {
            $scope.wechatType = false;
            $scope.webType = true;
            $scope.urlTypeShow = true;
            $scope.matchTypeShow = false;
            if($scope.urlType == '' || $scope.urlType == undefined) {
                $scope.urlType = '1'
            }
        }
        if($scope.siteType == 2) {
            $scope.siteDomain = '';
            $scope.wechatType = true;
            $scope.webType = false;
            $scope.urlTypeShow = false;
            $scope.matchTypeShow = false;
            $scope.matchType = '';
            $scope.urlType = '';
        }
        if($scope.siteType == 3) {
            $scope.siteDomain = '';
            $scope.wechatType = false;
            $scope.webType = false;
            $scope.urlTypeShow = false;
            $scope.matchTypeShow = false;
            $scope.matchType = '';
            $scope.urlType = '';
        }
    }
    $scope.urlTypeChange = function() {
        if($scope.urlType == 2) {
            $scope.matchTypeShow = true;
            $scope.showON = true;
            $scope.matchType = '2';
            $('#modifyModal .form-group .toggle').removeClass('toggle-OFF');
        }else {
            $scope.matchTypeShow = false;
            $scope.showON = false;
            $scope.matchType = '';
        }
    }
    $scope.toggle = function() {
        $scope.showON = !$scope.showON;
        if($scope.showON == true) {
            $scope.matchType = '2';
            $('#modifyModal .form-group .toggle').removeClass('toggle-OFF');
        }else {
            $scope.matchType = '1';
            $('#modifyModal .form-group .toggle').addClass('toggle-OFF');
        }
    }
    $scope.SaveSite = function() {
        if($scope.siteType == 1) {
            if($scope.siteDomain == '' || $scope.siteDomain == undefined) {
                $scope.isSiteDomain = true;
                return;
            }
        }
        if($scope.siteType == 2) {
            if($scope.accountId == '' || $scope.accountId == undefined) {
                $scope.isAccountId = true;
                return;
            }
        }
        if($scope.siteName == '' || $scope.siteName == undefined) {
            $scope.isSiteDomain = false;
            $scope.isAccountId = false;
            $scope.isSiteName = true;
            return;
        }
        $scope.isSiteDomain = false;
        $scope.isAccountId = false;
        $scope.isSiteName = false;
        bjj.http.ng.post($scope, $http, '/api/subscription/site?_method=post', {
            siteType : $scope.siteType,
            siteName : $scope.siteName,
            siteDomain : $scope.siteDomain == undefined? '': $scope.siteDomain,
            accountId : $scope.accountId == undefined? '': $scope.accountId,
            urlType    : $scope.urlType,
            matchType   : $scope.matchType
        }, function (res) {
            getallSites($scope, $http)
            getSubjectDetail($scope, $http);
            $("#modifyModal").modal("hide");
            bjj.dialog.alert('success', res.msg);
        }, function (res) {
            bjj.dialog.alert('danger', res.msg);
        });
    }
}
var getallSites  = function($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/subscription/allSites', {}, function (res) {
        $scope.allSite = res.list;
    })
}
var getSubjectDetail = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/subscription/subject?subjectId=' + $scope.subjectId, {}, function (res) {
        var SubjectDetail = res.subject;
        var setSiteIds = SubjectDetail.siteIds.split(',');
        bjj.http.ng.get($scope, $http, '/api/subscription/allSites', {}, function (res) {
            for(var i = 0;i<setSiteIds.length; i++) {
                _.forEach(res.list, function(v) {
                    if(setSiteIds[i] == v.siteId) {
                        v.active = true;
                    }
                })
            }
            $scope.allSite = res.list;
        })
        $scope.status = SubjectDetail.status;
        $scope.subjectName = SubjectDetail.subjectName;
        $scope.expression = SubjectDetail.expression;
        $scope.excludeWords = SubjectDetail.excludeWords;
        $scope.startTime = SubjectDetail.startTime;
        $scope.endTime = SubjectDetail.endTime;

    }, function(res) {
        if(res.msg == '查询主题失败') {
            return;
        }
        bjj.dialog.alert('danger', res.msg);
    });
};