var modifyAccountCtrl = function ($scope, $http, $stateParams) {
    $scope.userNameShow = false;
    $scope.passwordShow = false;
    $scope.phoneShow = false;
    $scope.truePhoneShow = false;
    $scope.userLengthShow = false;
    $scope.userId = $stateParams.id;
    bjj.http.ng.get($scope, $http, '/api/team/list', {}, function (res) {
        $scope.team = res.list;
    })
    if($scope.userId != undefined) {
        $scope.editShow = true;
        $scope.toEdit = true;
        bjj.http.ng.get($scope, $http, '/api/account/orgAccount', {
            userId : $scope.userId
        }, function (res) {
            $scope.orgUserName = res.msg.userName;
            $scope.orgPassword = res.msg.password;
            $scope.realName = res.msg.realName;
            $scope.phoneNumber = res.msg.phoneNumber;
            $scope.teamName = res.msg.teamId
        })
    }else {
        $scope.teamName = '';
        $scope.toEdit = false;
    }
    $scope.addTeam = function() {
        $scope.addTeamName = '';
        if($scope.team.length == 5) {
            $("#modifyTeamModal").modal("hide");
            bjj.dialog.alert('danger', '最多创建5个分组');
        }else {
            $("#modifyTeamModal").modal("show");
        }
    };
    $scope.saveTeam = function() {
        if( $scope.addTeamName == '' || $scope.addTeamName == undefined) {
            bjj.dialog.alert('danger', '请填写分组名');
            return;
        }
        bjj.http.ng.post($scope, $http, '/api/team', {
            teamName : $scope.addTeamName
        }, function(res) {
            bjj.dialog.alert('success', '保存成功！', {
                callback:   function() {
                    bjj.http.ng.get($scope, $http, '/api/team/list', {}, function (res) {
                        $scope.team = res.list;
                    })
                    $("#modifyTeamModal").modal("hide");
                }
            });
        },function(res) {
            bjj.dialog.alert('danger',res.msg);
        });
    };

    $('#inputPw').keyup(function() {
        verifyPassword($scope);
    });
    $('#inputPw').blur(function() {
        if(isPassword($scope.orgPassword) == 1) {
            $(".dangerPassword").show();
            $(".formatPassword").hide();
            $scope.dangerPasswordShow = true;
        }else if(isPassword($scope.orgPassword) == -1 || isPassword($scope.orgPassword) == 0 && $scope.orgPassword != undefined) {
            $(".formatPassword").show();
            $(".passwordShow").hide();
            $(".dangerPassword").hide();
            $scope.formatPasswordShow = true;
        }else {
            $(".dangerPassword").hide();
            $(".formatPassword").hide();
            $scope.dangerPasswordShow = false;
            $scope.formatPasswordShow = false;
        }
    })
    $scope.submitUser = function() {
        var userName = /^[0-9a-zA-Z_]{1,}$/;
        if($scope.orgUserName == '' || $scope.orgUserName == undefined) {
            $scope.userNameShow = true;
            $scope.userLengthShow = false;
            return;
        }
        $scope.userNameShow = false;
        if($scope.orgUserName != "" && !userName.test($scope.orgUserName)) {
            $scope.userLengthShow = true;
            $scope.userNameShow = false;
            return;
        }
        $scope.userLengthShow = false;
        if($scope.orgPassword == '' || $scope.orgPassword == undefined) {
            $scope.passwordShow = true;
            $(".passwordShow").show();
            $(".formatPassword").hide();
            $(".dangerPassword").hide();
            return;
        }
        $scope.passwordShow = false;
        if($scope.dangerPasswordShow || $scope.formatPasswordShow) {
            return;
        }
        if($scope.phoneNumber == '' || $scope.phoneNumber == undefined) {
            $scope.phoneShow = true;
            $scope.truePhoneShow = false;
            return;
        }
        $scope.phoneShow = false;
        if($scope.phoneNumber != "" && $scope.phoneNumber != undefined ) {
            var phoneNumbers = /^1[345678]\d{9}$/;
            if($scope.phoneNumber != "" && !phoneNumbers.test($scope.phoneNumber)) {
                $scope.truePhoneShow = true;
                $scope.phoneShow = false;
                return;
            }
        }
        $scope.truePhoneShow = false;
        var params = {
            userId : $scope.userId,
            userName: $scope.orgUserName,
            password: $scope.orgPassword,
            realName: $scope.realName,
            phoneNumber: $scope.phoneNumber,
            teamId: $scope.teamName
        }
        if($scope.userId == undefined) {
            bjj.http.ng.post($scope, $http, '/api/account/orgAccount?_method=post', params, function (res) {
                bjj.dialog.alert('success', '保存成功');
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            });
        }else {
            bjj.http.ng.put($scope, $http, '/api/account/orgAccount', params, function (res) {
                bjj.dialog.alert('success', '修改成功');
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            });
        }
    }
}
var verifyPassword = function($scope) {
    $(".weakPassword").removeClass('weak');
    $(".middlePassword").removeClass('middle');
    $(".strongPassword").removeClass('strong');
    if($scope.orgPassword == '' || $scope.password == undefined) {
        $(".weakPassword").removeClass('weak')
        $(".middlePassword").removeClass('middle');
        $(".strongPassword").removeClass('strong');
    }
    if(isPassword($scope.orgPassword) == 1) {
        $(".weakPassword").addClass('weak');
        $(".middlePassword").removeClass('middle');
        $(".strongPassword").removeClass('strong');
    }
    if(isPassword($scope.orgPassword) == 2) {
        $(".weakPassword").addClass('weak');
        $(".middlePassword").addClass('middle');
        $(".strongPassword").removeClass('strong');
    }
    if(isPassword($scope.orgPassword) == 3) {
        $(".weakPassword").addClass('weak');
        $(".middlePassword").addClass('middle');
        $(".strongPassword").addClass('strong');
    }
}