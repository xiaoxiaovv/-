/**
 * Author : YCSnail
 * Date : 2017-12-15
 * Email : liyancai1986@163.com
 */
var accountDashboardCtrl = function ($rootScope, $scope, $http) {
    getDashboardModules($scope, $http);

    $scope.save = function () {
        var accountModuleArr = _.map($("#account-modules span"), function (v) {
            return v.dataset.module
        });
        bjj.http.ng.put($scope, $http, '/api/custom/dashboard/modules', {
            content: accountModuleArr.join(',')
        }, function (res) {
            bjj.dialog.alert('success', res.msg);
        }, function (res) {
            bjj.dialog.alert('danger', res.msg);
        });
    };
    $scope.addModule = function () {
        this.item.isShow = true;
        $scope.accountModuleList.push(this.item);
    };
    $scope.removeModule = function () {
        this.item.isShow = false;
        $scope.accountModuleList.splice(this.$index, 1);
    };
};
var getDashboardModules = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/custom/dashboard/modules', {}, function (res) {
        $scope.dashboardModuleList = res.list;
        $scope.accountModuleList = _.filter($scope.dashboardModuleList, {'isShow': true});
        bindSortable4DashboardModules();
    });
};
var bindSortable4DashboardModules = function () {
    Sortable.create(document.getElementById('account-modules'), {
        group: 'account-modules',
        animation: 100,
        handle: "span",
        sort: true
    });
};


