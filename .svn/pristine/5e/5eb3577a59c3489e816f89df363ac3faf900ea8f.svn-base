/**
 * Author : YCSnail
 * Date : 2017-07-26
 * Email : liyancai1986@163.com
 */
var modifyPwdCtrl = function ($scope, $http) {
    $scope.old = false;
    $scope.nullOld = false;
    $scope.nullNew = false;
    $scope.nullConfirm = false;
    $scope.passwordConfirm = false;
    $scope.errorPassword = false;
    $scope.passwordShort  = false;

    $scope.checkPassword = function() {
        if($scope.oldPassword == undefined || $scope.oldPassword == '') {
            $scope.nullOld = true;
            $scope.nullNew = false;
            $scope.nullConfirm = false;
            $scope.passwordConfirm = false;
            $scope.errorPassword = false;
            return;
        }else {
            $scope.nullOld = false;
        }
    };
    $scope.checkNewPassword = function() {
        if($scope.oldPassword == undefined || $scope.oldPassword == '') {
            $scope.nullOld = true;
            return;
        }else {
            $scope.nullOld = false;
        }
        if($scope.newPassword == undefined || $scope.newPassword == '') {
            $scope.nullNew = true;
            $scope.errorPassword = false;
            return;
        }else {
            $scope.nullNew = false;
        }
        if(isPassword($scope.newPassword) == -1) {
            $scope.weakColor = '';
            $scope.middleColor = '';
            $scope.middle = '';
            $scope.strongColor = '';
            $scope.errorPassword = true;
            $scope.passwordShort = false;
            $scope.nullNew = false;
            $scope.repeat = false;
            $scope.nullConfirm = false;
            $scope.passwordConfirm = false;
            return;
        }else {
            $scope.errorPassword = false;
        }
        if(isPassword($scope.newPassword) == 0) {
            $scope.errorPassword = true;
            $scope.nullNew        = false;
            $scope.passwordShort = false;
            $scope.repeat = false;
            $scope.nullConfirm = false;
            $scope.passwordConfirm = false;
            $scope.weakColor = '';
            $scope.middle = '';
            $scope.middleColor = '';
            $scope.strongColor = '';
            return;
        }else {
            $scope.errorPassword = false;
        }
        if(isPassword($scope.newPassword) == 1) {
            $scope.passwordShort  = true;
            $scope.errorPassword = false;
            $scope.nullConfirm = false;
            $scope.passwordConfirm = false;
            $scope.weakColor = 'weak';
            $scope.middleColor = '';
            $scope.middle = '';
            $scope.strongColor = '';
            return;
        }else {
            $scope.weakColor = '';
            $scope.passwordShort = false;
        }
        if(isPassword($scope.newPassword) == 2) {
            $scope.weakColor = 'weak';
            $scope.middleColor = 'middle';
            $scope.middle = 'middle';
            $scope.strongColor = '';
        }else {
            $scope.middle = '';
            $scope.middleColor = '';
        }
        if(isPassword($scope.newPassword) == 3) {
            $scope.weakColor = 'weak';
            $scope.middleColor = 'middle';
            $scope.strongColor = 'strong';
        }else {
            $scope.middleColor = '';
            $scope.strongColor = '';
        }
        };
    $scope.checkConfirmPwd = function() {
        if($scope.oldPassword == undefined || $scope.oldPassword == '') {
            $scope.nullOld = true;
            return;
        }else {
            $scope.nullOld = false;
        }
        if($scope.newPassword == undefined || $scope.newPassword == '') {
            $scope.nullNew = true;
            $scope.errorPassword = false;
            return;
        }else {
            $scope.nullNew = false;
        }
        if($scope.confirmPassword != $scope.newPassword) {
            $scope.passwordConfirm = true;
            $scope.errorPassword = false;
        }else {
            $scope.passwordConfirm = false;
        }
        };

    $scope.submit = function(){
        var oldPassword= $scope.oldPassword;
        var newPassword = $scope.newPassword;
        var confirmPassword = $scope.confirmPassword;
        if(oldPassword == undefined || oldPassword == '') {
            $scope.nullOld = true;
            return;
        }else {
            $scope.nullOld = false;
        }
        if(newPassword == undefined || newPassword == '') {
            $scope.nullNew = true;
            $scope.errorPassword = false;
            return;
        }else {
            $scope.nullNew = false;
        }
        if(isPassword($scope.newPassword) == -1) {
            $scope.weakColor = '';
            $scope.middleColor = '';
            $scope.middle = '';
            $scope.strongColor = '';
            $scope.errorPassword = true;
            $scope.passwordShort  = false;
            $scope.nullNew = false;
            $scope.repeat = false;
            $scope.nullConfirm = false;
            $scope.passwordConfirm = false;
            return;
        }else {
            $scope.errorPassword = false;
        }
        if(isPassword($scope.newPassword) == 0) {
            $scope.errorPassword = true;
            $scope.nullNew        = false;
            $scope.repeat = false;
            $scope.nullConfirm = false;
            $scope.passwordConfirm = false;
            $scope.passwordShort  = false;
            $scope.weakColor = '';
            $scope.middle = '';
            $scope.middleColor = '';
            $scope.strongColor = '';
            return;
        }else {
            $scope.errorPassword = false;
        }
        if(isPassword($scope.newPassword) == 1) {
            $scope.passwordShort  = true;
            $scope.errorPassword = false;
            $scope.nullConfirm = false;
            $scope.passwordConfirm = false;
            $scope.weakColor = 'weak';
            $scope.middleColor = '';
            $scope.middle = '';
            $scope.strongColor = '';
            return;
        }else {
            $scope.weakColor = '';
            $scope.errorPassword = false;
        }
        if(isPassword($scope.newPassword) == 2) {
            $scope.weakColor = 'weak';
            $scope.middleColor = 'middle';
            $scope.middle = 'middle';
            $scope.strongColor = '';
        }else {
            $scope.middle = '';
            $scope.middleColor = '';
        }
        if(isPassword($scope.newPassword) == 3) {
            $scope.weakColor = 'weak';
            $scope.middleColor = 'middle';
            $scope.strongColor = 'strong';
        }else {
            $scope.middleColor = '';
            $scope.strongColor = '';
        }
        if(confirmPassword == undefined || confirmPassword == '') {
            $scope.nullConfirm = true;
            $scope.errorPassword = false;
            $scope.passwordConfirm = false;
            return;
        }else {
            $scope.nullConfirm = false;
        }
        if(oldPassword == newPassword) {
            $scope.repeat = true;
            $scope.errorPassword = false;
            $scope.nullNew       = false;
            $scope.errorPassword = false;
            $scope.passwordConfirm = false;
            $scope.nullConfirm = false;
            return;
        }else {
            $scope.repeat = false;
        }
        bjj.http.ng.put($scope, $http, '/api/account/password?_method=PUT', {
            oldPassword     : getValue(oldPassword),
            newPassword     : getValue(newPassword),
            confirmPassword : getValue(confirmPassword)
        },function(res) {
            if(oldPassword == newPassword) {
                $scope.repeat = true;
                return;
            }else {
                $scope.repeat = false;
                bjj.dialog.alert('success', res.msg);
            }
        },function(res) {
            if(res.status == 1) {
                if(oldPassword == undefined || oldPassword == "") {
                    $scope.nullOld = true;
                }
                if(newPassword == undefined || newPassword== "") {
                    $scope.nullNew = true;
                    $scope.errorPassword = false;
                }
                if(confirmPassword == undefined || confirmPassword == "") {
                    $scope.nullConfirm = true;
                    $scope.errorPassword = false;
                }else {
                    $scope.nullOld = true;
                    $scope.nullNew = true;
                    $scope.nullConfirm = true;
                    if(typeof(oldPassword) == 'undefined'){
                        $scope.nullNew = false;
                        $scope.nullConfirm = false;
                    }
                }
            }
            if(res.status == 2) {
                $scope.passwordConfirm = true;
                $scope.nullConfirm = false;
            }else {
                $scope.passwordConfirm = false;
            }
            if(res.status == 3) {
                $scope.old = true;
                $scope.nullOld = false;
                $scope.nullNew = false;
                $scope.nullConfirm = false;
                $scope.passwordConfirm = false;
                $scope.errorPassword = false;
            }else {
                $scope.old = false;
            }
            if(res.status == 4 || res.status == 5 ) {
                $scope.errorPassword = true;
                $scope.repeat = false;
                $scope.passwordShort  = true;
                $scope.passwordConfirm = false;
            }else {
                $scope.errorPassword = false;
            }
        })

    }
};
function getValue(value) {
    return value == undefined ? "" : value
}

