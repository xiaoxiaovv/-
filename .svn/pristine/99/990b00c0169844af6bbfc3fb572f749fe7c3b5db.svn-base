var manageCtrl = function ($scope, $http) {
    $scope.userWords = '';
    getOrganizationInfo($scope, $http);
    teamList($scope, $http);
    getTeamList($scope, $http);
    $scope.listAddTeam = function() {
        $scope.addTeamName = '';
        if($scope.allTeam.length == 5) {
            $("#modifyTeamModal").modal("hide");
            bjj.dialog.alert('danger', '最多创建5个分组');
        }else {
            $("#modifyTeamModal").modal("show");
        }
    };
    $scope.listSaveTeam = function() {
        if($scope.addTeamName == '' || $scope.addTeamName == undefined) {
            bjj.dialog.alert('danger', '请填写分组名');
            return;
        }
        bjj.http.ng.post($scope, $http, '/api/team', {
            teamName : $scope.addTeamName
        }, function(res) {
            bjj.dialog.alert('success', '保存成功！', {
                callback:   function() {
                    getTeamList($scope, $http);
                    teamList($scope, $http)
                    $("#modifyTeamModal").modal("hide");
                }
            });
        }, function(res) {
            if( res.msg == '组名重复！') {
                bjj.dialog.alert('danger','组名重复！');
                return;
            }
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
            userTeamId = i.teamId
        })
        if(teamId == userTeamId) {
            bjj.dialog.alert('danger', '不允许同组移动');
            return;
        }
        $scope.ids = $scope.ids.join(',');
        bjj.http.ng.put($scope, $http, '/api/account/orgUserTeam', {
            teamId  : teamId,
            userIds : $scope.ids
        }, function (res) {
            getTeamList($scope, $http)
            bjj.dialog.alert('success', '修改成功');
            $(".operationMoveBtn").addClass('moveBtn');
        }, function (res) {
            bjj.dialog.alert('danger', res.msg);
        });
    }
    $scope.moveOutTeam = function(id) {
        bjj.http.ng.put($scope, $http, '/api/account/orgUserTeam', {
            teamId  : 0,
            userIds : id
        }, function (res) {
            getTeamList($scope, $http);
            $(".operationMoveBtn").addClass('moveBtn');
            bjj.dialog.alert('success', '移除成功');
        }, function (res) {
            bjj.dialog.alert('danger', res.msg);
        });
    }
    angular.element(window).bind('click',function(){
        $(".setTeamBox").hide();
    });
    $scope.deleteTeam = function(teamId) {
        bjj.dialog.confirm('删除后该组成员将自动到未分组，确定删除整组吗？', function () {
            bjj.http.ng.del($scope, $http, '/api/team/' + teamId +'?_method=DELETE', {}, function (res) {
                teamList($scope, $http)
                getTeamList($scope, $http)
                $(".operationMoveBtn").addClass('moveBtn');
                bjj.dialog.alert('success', res.msg);
            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            });
        })
    }
    $scope.deleteUser = function(userId) {
        bjj.dialog.confirm('删除后该账号将无法登录，是否确认删除？', function () {
            bjj.http.ng.del($scope, $http, '/api/account/orgAccount/' + userId +'?_method=DELETE', {}, function (res) {
                $(".operationMoveBtn").addClass('moveBtn');
                teamList($scope, $http)
                getTeamList($scope, $http)
                bjj.dialog.alert('success', res.msg);
            }, function(res) {
                bjj.dialog.alert('danger', res.msg);
            });
        })
    }
    $scope.toggleAllUser = function(id) {
        $(".operationMoveBtn").removeClass('moveBtn');
        var _this = this;
        this.team.active = !this.team.active;
        if(this.team.active) {
            _.forEach($("."+id).children("li:visible"), function(v,i) {
                _.forEach(_this.team.accountList, function(i) {
                    if(i.id == v.className) {
                        i.active = true;
                    }
                })
            })
        }else {
            _.forEach(this.team.accountList, function(i) {
                i.active = false;
            })
        }
        $scope.trueUser = _.filter(this.team.accountList,{'active':true});
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
        var trueId = this.account.teamId;
        $scope.thisTeam = _.filter($scope.usersList,{'teamId':this.account.teamId})
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
    $scope.editTeamName = function(team) {
        var teamName = team.teamName;
        team.editName = true;
        setTimeout(function () {
            $(".member-list .inputTeamName").select();
        },0)
        $scope.blurTeamName = function(team) {
            if(team.teamName.trim() == '') {
                team.teamName = teamName;
            }
            editTeamName($scope, $http,team.id, team.teamName.trim(), team, teamName);
        }
    }
    $scope.channel = function($event, id) {
        var clickHeight = $event.pageY;
        var allHeight = $(".bjj-content").height();
        if( allHeight - clickHeight < 170) {
            $('.channels').css('margin-top','-227px' )
        }else {
            $('.channels').css('margin-top', 0 )
        }
        $('.user'+id).show();
    }
    $scope.leaveChannel = function(id) {
        $('.user'+id).hide();
    }
}
/*得到管理员信息*/
var getOrganizationInfo = function($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/organization/info', {}, function (res) {
        $scope.orgInfo = res;
    })
}
/*得到分组*/
var teamList = function($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/team/list', {}, function (res) {
        $scope.allTeam = res.list;
    })
}
/*得到用户以及组名*/
var getTeamList = function($scope, $http) {
    var userId = [];
    $scope.usersList = [];
    bjj.http.ng.get($scope, $http, '/api/account/list', {}, function (res) {
        $scope.teamList = res.list;
        _.forEach($scope.teamList, function(i,v) {
            _.forEach(i.accountList, function(i,v) {
                $scope.usersList.push(i)
                userId.push(i.id)
            })
        })
        userId = userId.join(',');
        bjj.http.ng.postBody($scope, $http, '/api/share/channels', {userIds : userId} , function(res) {
            $scope.channelMap = res.list;
            $scope.userChannelMap = {};
            _.forEach($scope.channelMap, function(v, k) {
                var channelList = _.groupBy(v, function(item) {
                    return item.channelType;
                });
                $scope.userChannelMap[k] = channelList;
            })
        });
    })
}
/*编辑组*/
var editTeamName = function($scope, $http, id, teamName, team, oldTeamName) {
    bjj.http.ng.put($scope, $http,'/api/team/'+id, {
        teamName: teamName == '' ? '' : teamName
    }, function(res) {
        bjj.http.ng.get($scope, $http, '/api/team/list', {}, function (res) {
            $scope.allTeam = res.list;
        })
        team.editName = false;
    }, function(res) {
        if( res.msg == '组名重复！' ) {
            bjj.dialog.alert('danger', res.msg);
            team.teamName = oldTeamName;
            team.editName = false;
        }else {
            bjj.dialog.alert('danger', res.msg);
        }
    });
};

