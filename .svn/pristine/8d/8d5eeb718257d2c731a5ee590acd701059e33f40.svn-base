var subscriptionExclusionCtrl = function ($rootScope, $scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/subscription/excludeWords ', {}, function (res) {
        $scope.excludeWords = res.excludeWords;
    });
    $scope.editSaveSite = function() {
        var excludeWords = $scope.excludeWords.trim().split(' ');
        var arr = [];
        for(var i = 0; i<=excludeWords.length; i++) {
            if(excludeWords[i]!= ''&&excludeWords[i]!= undefined) {
                arr.push(excludeWords[i]);
                for(var key in arr) {
                    if(arr[key].length<2||arr[key].length>10) {
                        bjj.dialog.alert('danger', '排除词长度不能小于2或者大于10！');
                        return;
                    }
                }
            }
        }
        bjj.http.ng.put($scope, $http, '/api/subscription/excludeWords ', {
            excludeWords: arr.join(',')
        }, function (res) {
            bjj.dialog.alert('success','保存成功！');
        }, function (res) {
            if(res.errorMsg) {
                bjj.dialog.alert('danger', res.errorMsg);
            }else {
                bjj.dialog.alert('danger', res.msg);
            }
        });
    }
};