var evaluateReportCtrl = function ($rootScope, $scope, $http, $state) {
    $rootScope.minBar = true;
    $scope.evaluateNameShow = false;
    $scope.channelsNameShow = false;
    $scope.addTeamTime = true;
    $scope.addChannelTime = true;
    $scope.defaultMediaSetting = {siteType:"1",siteName:"",siteDomain:"",placeholder:'输入站点名称',placeholderDomain:'输入域名'};
    $scope.editDefaultMediaSetting = {siteType:"1",siteName:"",siteDomain:"",placeholder:'输入站点名称',placeholderDomain:'输入域名'};
    $scope.defaultSearchMediaSetting = {siteType:"0"};
    evaluateTimeList($scope, $http)
    evaluateTeamList($scope, $http)
    getChannelAndTeamList($scope, $http);
    getEvaluateChannel($scope, $http);
    $scope.modifyChannel = function() {
        $scope.addChannelTime = true;
        $scope.modifyChannelName = '添加渠道';
        $scope.defaultMediaSetting.siteName = '';
        $scope.defaultMediaSetting.siteDomain = '';
        $scope.team = '0';
        $("#modifyChannelModal").modal("show");
    }
    $scope.AddTeam = function() {
        $scope.addTeamTime = true;
        $scope.addTeamName = '';
        $scope.teamName = '';
        if($scope.allTeam.length == 50) {
            $("#modifyTeamModal").modal("hide");
            bjj.dialog.alert('danger', '最多创建50个分组');
            return;
        }else {
            $("#modifyTeamModal").modal("show");
        }
    };
    $scope.changeSiteType = function () {
        $scope.pno = 1;
        getChannelAndTeamList($scope, $http)
    }
    /*改变类型的默认提示切换*/
    $scope.changeType = function() {
        if($scope.defaultMediaSetting.siteType == 1) {
            $scope.defaultMediaSetting.placeholder = "输入站点名称";
            $scope.defaultMediaSetting.placeholderDomain = "输入域名";
        }else if($scope.defaultMediaSetting.siteType == 2) {
            $scope.defaultMediaSetting.placeholder = "输入公众号名称";
        }else {
            $scope.defaultMediaSetting.placeholder = "输入微博账号";
        }
    };
    $scope.editChangeType = function() {
        if($scope.editDefaultMediaSetting.siteType == 1) {
            $scope.editDefaultMediaSetting.placeholder = "输入站点名称";
            $scope.editDefaultMediaSetting.placeholderDomain = "输入域名";
        }else if($scope.editDefaultMediaSetting.siteType == 2) {
            $scope.editDefaultMediaSetting.placeholder = "输入公众号名称";
        }else {
            $scope.editDefaultMediaSetting.placeholder = "输入微博账号";
        }
    };
    //添加分组
    $scope.listSaveTeam = function () {
        if($scope.addTeamTime) {
            bjj.http.ng.post($scope, $http, '/api/evaluate/team', {
                teamName: $scope.teamName,
            }, function (res) {
                $("#modifyTeamModal").modal("hide");
                evaluateTeamList($scope, $http);
                getChannelAndTeamList($scope, $http)
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            })
        }else {
            bjj.http.ng.put($scope, $http, '/api/evaluate/team/' + $scope.editTeamId, {
                teamName  : $scope.teamName
            }, function (res) {
                $("#modifyTeamModal").modal("hide");
                evaluateTeamList($scope, $http);
                getChannelAndTeamList($scope, $http)
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            });
        }
    }
    //添加渠道
    $scope.listSaveChannel = function () {
        if($scope.addChannelTime) {
            bjj.http.ng.post($scope, $http, '/api/evaluate/channel', {
                siteType: $scope.defaultMediaSetting.siteType,
                siteName:$scope.defaultMediaSetting.siteName,
                siteDomain:$scope.defaultMediaSetting.siteDomain,
                evaluateTeamId:$scope.team
            }, function (res) {
                $("#modifyChannelModal").modal("hide");
                getChannelAndTeamList($scope, $http)
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            })
        }else {
            bjj.http.ng.put($scope, $http, '/api/evaluate/channel/' + $scope.editChannelId, {
                siteType: $scope.editDefaultMediaSetting.siteType,
                siteName:$scope.editDefaultMediaSetting.siteName,
                siteDomain:$scope.editDefaultMediaSetting.siteDomain,
                evaluateTeamId:$scope.team
            }, function (res) {
                $("#modifyChannelModal").modal("hide");
                getChannelAndTeamList($scope, $http)
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            })
        }
    }
    $scope.toggleAllUser = function(id) {
        $(".operationMoveBtn").removeClass('moveBtn');
        var _this = this;
        this.team.active = !this.team.active;
        if(this.team.active) {
            _.forEach($("."+id).children("li:visible"), function(v,i) {
                _.forEach(_this.team.evaluateChannelList, function(i) {
                    if(i.id == v.className) {
                        i.active = true;
                    }
                })
            })
        }else {
            _.forEach(this.team.evaluateChannelList, function(i) {
                i.active = false;
            })
        }
        $scope.trueUser = _.filter(this.team.evaluateChannelList,{'active':true});
        $scope.editList = _.filter($scope.usersList,{'active':true});
        if($scope.trueUser.length == 0) {
            $(".operationMoveBtn").addClass('moveBtn');
        }
        if($scope.editList.length > 0) {
            $(".operationMoveBtn").removeClass('moveBtn');
        }
    }
    $scope.toggleUser = function() {
        $(".operationMoveBtn").removeClass('moveBtn');
        this.account.active = !this.account.active;
        var trueId = this.account.evaluateTeamId;
        $scope.thisTeam = _.filter($scope.usersList,{'evaluateTeamId':this.account.evaluateTeamId})
        $scope.trueTeam = _.filter($scope.thisTeam,{'active':true});
        if($scope.trueTeam.length == 0) {
            $(".operationMoveBtn").addClass('moveBtn');
        }
        if($scope.thisTeam.length == $scope.trueTeam.length) {
            _.forEach($scope.teamList, function(i) {
                if(i.id == trueId) {
                    i.active = true;
                }
            })
        }else {
            _.forEach($scope.teamList, function(i) {
                if(i.id == trueId) {
                    i.active = false;
                }
            })
        }
        $scope.editList = _.filter($scope.usersList,{'active':true});
        if($scope.editList.length > 0) {
            $(".operationMoveBtn").removeClass('moveBtn');
        }
    }
    $scope.deleteChannel = function(id) {
        bjj.dialog.confirm('是否确认删除该渠道？', function () {
            bjj.http.ng.del($scope, $http, '/api/evaluate/channel/' + id +'?_method=DELETE', {}, function (res) {
                $(".operationMoveBtn").addClass('moveBtn');
                evaluateTeamList($scope, $http)
                getChannelAndTeamList($scope, $http)
                bjj.dialog.alert('success', res.msg);
            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            });
        })
    }
    //编辑渠道
    $scope.editChannel = function() {
        $scope.addChannelTime = false;
        $scope.modifyChannelName = '编辑渠道';
        $scope.editChannelId = this.account.id;
        bjj.http.ng.get($scope, $http, '/api/evaluate/channel/' + this.account.id, {}, function (res) {
            $scope.editDefaultMediaSetting.siteType = res.info.siteType;
            $scope.editDefaultMediaSetting.siteName = res.info.siteName;
            $scope.editDefaultMediaSetting.siteDomain = res.info.siteDomain;
            $scope.team = res.info.evaluateTeamId;
            $scope.editChangeType();
            $("#modifyChannelModal").modal("show");
        })
    }
    //编辑组
    $scope.editTeamName = function () {
        $scope.addTeamTime = false;
        $scope.editTeamId = this.team.id;
        bjj.http.ng.get($scope, $http, '/api/evaluate/team/' + this.team.id, {}, function (res) {
            $scope.teamName = res.evaluateTeam.teamName;
            $("#modifyTeamModal").modal("show");
        })
    }
    $scope.moveOutTeam = function(id) {
        bjj.http.ng.put($scope, $http, '/api/evaluate/channel/team', {
            teamId  : 0,
            channelIds : id
        }, function (res) {
            getChannelAndTeamList($scope, $http);
            $(".operationMoveBtn").addClass('moveBtn');
            bjj.dialog.alert('success', '移出成功');
        }, function (res) {
            bjj.dialog.alert('danger', res.msg);
        });
    }
    $scope.moveTeamClick = function($event) {
        $scope.editList = _.filter($scope.usersList,{'active':true})
        if($scope.editList.length == 0) {
            return;
        }else {
            $(".setTeamBox").show();
        }
        $event.stopPropagation();
    }
    $scope.moveTeam = function($event, teamId) {
        $event.stopPropagation();
        $(".setTeamBox").hide();
        $scope.ids = [];
        var userTeamId;
        _.forEach($scope.editList,function(i) {
            $scope.ids.push(i.id);
            userTeamId = i.evaluateTeamId
        })
        if(teamId == userTeamId) {
            bjj.dialog.alert('danger', '不允许同组移动');
            return;
        }
        $scope.ids = $scope.ids.join(',');
        bjj.http.ng.put($scope, $http, '/api/evaluate/channel/team', {
            teamId  : teamId,
            channelIds : $scope.ids
        }, function (res) {
            getChannelAndTeamList($scope, $http)
            bjj.dialog.alert('success', '修改成功');
            $(".operationMoveBtn").addClass('moveBtn');
        }, function (res) {
            bjj.dialog.alert('danger', res.msg);
        });
    }
    $scope.creatEvaluate = function() {
        if($scope.evaluateName == '' || $scope.evaluateName == undefined) {
            $scope.evaluateNameShow = true;
            $scope.channelsNameShow = false;
            return;
        }
        if($scope.channelsName == '' || $scope.channelsName == undefined) {
            $scope.evaluateNameShow = false;
            $scope.channelsNameShow = true;
            return;
        }
        $scope.evaluateNameShow = false;
        $scope.channelsNameShow = false;
        bjj.dialog.confirm('请确认好渠道设置(如果设置的渠道有误，测评中将不显示该渠道数据），并且一天只能创建一次测评，是否确认创建？', function () {
            bjj.http.ng.post($scope, $http, '/api/evaluate/report', {
                evaluateName  : $scope.evaluateName,
                channelsName : $scope.channelsName
            }, function (res) {
                $state.go('evaluateManagement');
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            })
        })
    }
    $scope.cancleEvaluate = function() {
        $state.go('evaluateManagement');
    }
    $scope.deleteTeam = function () {
        teamDelete($scope, $http, this.team.id)
    }
    $scope.keywordsSearchClick = function (e) {
        $scope.pno = 1;
        getChannelAndTeamList($scope, $http)
    }
    $scope.siteNameKeyup = function (e) {
        if (e.keyCode == 13) {
            $scope.pno = 1;
            getChannelAndTeamList($scope, $http)
        }
    };
}
//渠道类型
let getEvaluateChannel = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, "/api/evaluate/channel/type", {}, function (res) {
        $scope.siteTypes = res.list;
    })
}

//获取测评时间段
let evaluateTimeList = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/evaluate/time', {}, function (res) {
        $scope.starTime = res.starTime;
        $scope.endTime = res.endTime;
    })
}
//获取分组列表
let evaluateTeamList = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/evaluate/team/list', {}, function (res) {
        $scope.allTeam = res.list;
    })
}
//获取渠道和渠道分组列表
let getChannelAndTeamList = function ($scope, $http) {
    $scope.usersList = [];
    bjj.http.ng.get($scope, $http, '/api/evaluate/channel/list', {
        siteType: $scope.defaultSearchMediaSetting.siteType? $scope.defaultSearchMediaSetting.siteType: 0,
        siteName: $scope.siteName? $scope.siteName: ''
    }, function (res) {
        $scope.teamList = res.list;
        _.forEach($scope.teamList, function (i, v) {
            _.forEach(i.evaluateChannelList, function (i, v) {
                $scope.usersList.push(i)
            })
        })
    })
}
let teamDelete= function ($scope, $http, id) {
    bjj.dialog.confirm('确定删除？', function(){
        bjj.http.ng.del($scope, $http,'/api/evaluate/team/' + id + '?_method=delete', {}, function (res) {
            getChannelAndTeamList($scope, $http);
            evaluateTeamList($scope, $http);
        },function(res) {
            bjj.dialog.alert('danger',res.msg);
        });
    })
}