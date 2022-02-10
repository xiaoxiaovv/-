var accountMonitorModifyCtrl = function ($rootScope, $scope, $http, $state, $stateParams) {

    $scope.monitorId = $stateParams.monitorId;
    $scope.monitorEditPage = true;

    $scope.mediaSettingList = {
        whiteList:[ { websiteName : "", websiteDomain : "" } ],
        blackList:[ { websiteName : "", websiteDomain : "" } ]
    };
    $scope.accountCopyrightFilter = {
        whiteList : [],
        blackList : []
    };

    getCopyrightFilter($scope, $http);
    var publicTime = {
        elem:"#publicTime",
        event: 'click',
        format:"YYYY-MM-DD hh:mm:ss",
        istime: true,
        isclear: false,
        choose:function(v){
            $scope.accountMonitor.startDate = v;
        }
    };
    laydate(publicTime);
    $scope.titleInput = function(){
        if($scope.accountMonitor.title == undefined) {
            $scope.accountMonitor.title == '';
            return;
        } else {
            if($scope.accountMonitor.title.length > 140){
                $scope.accountMonitor.title = $scope.accountMonitor.title.substr(0, 140);
                bjj.dialog.alert('danger', '友情提示：超出最长度140，已自动截取！');
            }
        }
    };
    /* 查询*/
    $scope.getNewsByUrl = function () {
        bjj.http.ng.post($scope, $http, '/api/competitionAnalys/news?url=' + $scope.accountMonitor.url, {}, function (res) {
            if (res.title == undefined) {
                bjj.dialog.alert('danger', '没有查询到相关地址信息！');
                return;
            }
            $scope.accountMonitor.title = res.title;
            $scope.accountMonitor.contentAbstract = res.contentAbstract;
        });
    };
    /*设置*/
    $scope.addWhiteViewClick = function(){
        getBlackAndWhiteList($scope, $http);
    };
    $scope.addBlackViewClick = function(){
        getBlackAndWhiteList($scope, $http);
    };
    //白名单删除
    $scope.delWhiteRecord = function(index){
        var websiteDomain = $scope.mediaSettingList.whiteList[index].websiteDomain;
        bjj.http.ng.get($scope, $http, '/api/competitionAnalys/copyrightMonitor/copyRightFilter/isSiteMonitoring', {
            websiteDomain : websiteDomain
        }, function (res) {
            if(!res.isSiteMonitoring){
                $scope.mediaSettingList.whiteList.splice(index, 1);
                if($scope.mediaSettingList.whiteList.length == 0){
                    $scope.mediaSettingList.whiteList.unshift({ websiteName : "", websiteDomain : "" });
                }
            }else {
                bjj.dialog.alert('danger','站点正在监控中不能删除');
            }
        });
    };
    //黑名单删除
    $scope.delBlackRecord = function(index){
        var websiteDomain = $scope.mediaSettingList.blackList[index].websiteDomain;
        bjj.http.ng.get( $scope, $http, '/api/competitionAnalys/copyrightMonitor/copyRightFilter/isSiteMonitoring', {
            websiteDomain : websiteDomain
        }, function (res) {
            if(!res.isSiteMonitoring){
                $scope.mediaSettingList.blackList.splice(index, 1);
                if($scope.mediaSettingList.blackList.length == 0){
                    $scope.mediaSettingList.blackList.unshift({ websiteName : "", websiteDomain : "" });
                }
            }else {
                bjj.dialog.alert('danger','站点正在监控中不能删除');
            }
        });
    };
    $scope.addWhiteRecord = function(){
        if($scope.mediaSettingList.whiteList.length >= 8){
            bjj.dialog.alert('danger', "最多设置8个");
            return;
        }
        $scope.mediaSettingList.whiteList.push({ websiteName : "", websiteDomain : "" });
    };
    $scope.addBlackRecord = function(){
        if($scope.mediaSettingList.blackList.length >= 8){
            bjj.dialog.alert('danger', "最多设置8个");
            return;
        }
        $scope.mediaSettingList.blackList.push({ websiteName : "", websiteDomain : "" });
    };

    //黑白名单的保存
    $scope.saveBtn = function(){
        var whiteList = [];
        var blackList = [];
        var whiteListSize = $scope.mediaSettingList.whiteList.length;
        var blackListSize = $scope.mediaSettingList.blackList.length;
        for(var i = 0; i < whiteListSize; i++){
            var whiteWebsiteName   = $scope.mediaSettingList.whiteList[i].websiteName.trim();
            var whiteWebsiteDomain = $scope.mediaSettingList.whiteList[i].websiteDomain.trim();
            if((whiteWebsiteName == "" && whiteWebsiteDomain != "") || (whiteWebsiteName != "" && whiteWebsiteDomain == "")){
                bjj.dialog.alert('danger', '信息不全，请填写完整！');
                return;
            }else if(whiteWebsiteName != "" && whiteWebsiteDomain != ""){
                whiteList.push({websiteName : whiteWebsiteName, websiteDomain : whiteWebsiteDomain });
            }
        }
        for(var j = 0; j < blackListSize; j++){
            var blackWebsiteName = $scope.mediaSettingList.blackList[j].websiteName.trim();
            var blackWebsiteDomain = $scope.mediaSettingList.blackList[j].websiteDomain.trim();
            if((blackWebsiteName == "" && blackWebsiteDomain != "") || (blackWebsiteName != "" && blackWebsiteDomain == "")){
                bjj.dialog.alert('danger', '信息不全，请填写完整！');
                return;
            }else if(blackWebsiteName != "" && blackWebsiteDomain != ""){
                blackList.push({websiteName : blackWebsiteName, websiteDomain : blackWebsiteDomain });
            }
        }
        bjj.http.ng.put( $scope, $http, '/api/copyright/monitor/filter',{
                whiteList   : JSON.stringify(whiteList),
                blackList   : JSON.stringify(blackList)
            }, function (res) {
                $scope.accountCopyrightFilter.whiteList = whiteList;
                $scope.accountCopyrightFilter.blackList = blackList;
                bjj.dialog.alert( 'success', "保存成功！");
                $('.modal').modal('hide');
            }, function (res){
                bjj.dialog.alert('danger', res.msg);
            });
    };
    $scope.toggleBlackAndWhiteList = function () {
        this.item.active = !this.item.active;
    };
    /*删除作品*/
    $scope.deleteMonitor = function(){
        bjj.dialog.confirm('确定删除？', function(){
            bjj.http.ng.del($scope, $http,'/api/copyright/monitor/' + $scope.monitorId + '?_method=delete', {}, function (res) {
                bjj.http.ng.get($scope, $http, '/api/copyright/monitors', {}, function (res) {
                    $rootScope.monitorList = res.msg.list;
                    bjj.dialog.alert('success', '删除成功！');
                    $scope.isShowView = true;
                    $scope.monitorEditPage = false;
                });
            },function(res) {
                bjj.dialog.alert('danger',res.msg);
            });
        })
    };
    /*编辑的保存*/
    $scope.monitorEditSave = function () {
        if ($scope.accountMonitor.title == undefined || $scope.accountMonitor.title == "") {
            bjj.dialog.alert('danger', '保存失败，请填写作品名称！');
            return;
        }
        var params = {
            title       : $scope.accountMonitor.title.trim(),
            url         : $scope.accountMonitor.url.trim(),
            author      : $scope.accountMonitor.author.trim(),
            startDate   : $scope.accountMonitor.startDate,
            media       : $scope.accountMonitor.media.trim(),
            contentAbstract : $scope.accountMonitor.contentAbstract.trim(),
            whiteList   : JSON.stringify(_.map(_.filter($scope.accountCopyrightFilter.whiteList, {'active': true}), function (v, i) {
                return v.websiteDomain;
            })),
            blackList   : JSON.stringify(_.map(_.filter($scope.accountCopyrightFilter.blackList, {'active': true}), function (v, i) {
                return v.websiteDomain;
            }))
        };
        var getMonitorList = function (reloadPage) {
            bjj.http.ng.get($scope, $http, '/api/copyright/monitors', {}, function (res) {
                $rootScope.monitorList = res.msg.list;
            });
            if(reloadPage) {
                window.reloadPage(true);
            }
        };

        if($scope.monitorId == 0 || $scope.monitorId == undefined){
            bjj.http.ng.post($scope, $http, '/api/copyright/monitor?_method=post', params, function (res) {
                getMonitorList(true);
                bjj.http.ng.get($scope, $http, '/api/copyright/monitors', {}, function (res) {
                    $rootScope.monitorList = res.msg.list;
                })
                bjj.dialog.alert('success', '保存成功');
                $('.bjj-account-save-view .btn-primary').attr("disabled", true);
                setTimeout(function() {
                    $('.bjj-account-save-view .btn-primary').attr("disabled", false);
                }, 500);
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            });
        } else {
            bjj.http.ng.put($scope, $http, '/api/copyright/monitor/' + $scope.monitorId, params, function (res) {
                _.map($rootScope.monitorList, function (v, i) {
                    if(v.monitorId == $scope.monitorId) {
                        v.title = $scope.accountMonitor.title;
                    }
                });
                bjj.http.ng.get($scope, $http, '/api/copyright/monitors', {}, function (res) {
                    $rootScope.monitorList = res.msg.list;
                })
                bjj.dialog.alert('success','保存成功！');
            },function (res) {
                bjj.dialog.alert('danger', res.msg);
            });
        }
    };
};
var getAccountMonitor = function ($scope, $http) {
    if($scope.monitorId == undefined || $scope.monitorId == 0){
        $scope.accountMonitor = {
            monitorId   : '',
            title       : '',
            url         : '',
            author      : '',
            media       : '',
            startDate   : new Date().Format('yyyy-MM-dd hh:mm:ss'),
            whiteList   : [],
            blackList   : [],
            contentAbstract : ''
        };
        $scope.monitorEditPage = false;
        $(".account-monitor-list .nav-list li").removeClass('active');
    } else {
        getAccountMonitorDetail($scope, $http);
    }
};
var getAccountMonitorDetail = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/copyright/monitor/' + $scope.monitorId, {}, function (res) {
        $scope.accountMonitor = res.msg;
        $scope.accountMonitor.title = $scope.accountMonitor.title.length > 140 ? $scope.accountMonitor.title.substr(0, 140) : $scope.accountMonitor.title;
        $scope.accountMonitor.startDate = new Date(res.msg.startDate).Format('yyyy-MM-dd hh:mm:ss');

        if(!_.isEmpty($scope.accountCopyrightFilter.whiteList) && !_.isEmpty(res.msg.whiteList)){
            _.map($scope.accountCopyrightFilter.whiteList, function (v) {
                v.active = res.msg.whiteList.indexOf(v.websiteDomain) > -1;
            });
        }
        if(!_.isEmpty($scope.accountCopyrightFilter.blackList) && !_.isEmpty(res.msg.blackList)){
            _.map($scope.accountCopyrightFilter.blackList, function (v) {
                v.active = res.msg.blackList.indexOf(v.websiteDomain) > -1;
            });
        }
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    });
};
var getCopyrightFilter = function ($scope, $http, callback) {
    bjj.http.ng.get($scope, $http, '/api/competitionAnalys/copyrightMonitor/copyRightFilter', {}, function(res) {
        if(res.copyRightFilter != null){
            if(res.copyRightFilter.whiteList != null){
                $scope.accountCopyrightFilter.whiteList = res.copyRightFilter.whiteList;
            }
            if(res.copyRightFilter.blackList != null){
                $scope.accountCopyrightFilter.blackList = res.copyRightFilter.blackList;
            }
        }
        getAccountMonitor($scope, $http);
    });
};
//黑白名单列表
var getBlackAndWhiteList = function($scope,$http){
    bjj.http.ng.get($scope, $http, '/api/competitionAnalys/copyrightMonitor/copyRightFilter', {}, function(res) {
        if(res.copyRightFilter != null){
            if(res.copyRightFilter.whiteList.length > 0){
                $scope.mediaSettingList.whiteList = res.copyRightFilter.whiteList;
            }
            if(res.copyRightFilter.blackList.length > 0){
                $scope.mediaSettingList.blackList = res.copyRightFilter.blackList;
            }
        }
    }, function (res) {
        bjj.dialog.alert('danger', res.msg);
        return;
    });
};
