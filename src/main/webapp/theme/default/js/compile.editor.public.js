var editorPublicCtrl = function ($rootScope, $scope, $http, $stateParams, $interval, $sce) {
    $scope.id = $stateParams.materialId;
    $scope.urlAddress = 'http://'+window.location.href.split("/")[2] +'/compile/article.html?id=' + $scope.id;
    bjj.http.ng.get($scope, $http, '/api/compile/material/article/' + $scope.id, {}, function (res) {
        $scope.data = res.msg;
        $scope.data.content = $sce.trustAsHtml($scope.data.material.content);
        $scope.previewArticle = {
            title      : res.msg.title,
            author     : res.msg.author ? res.msg.author : '匿名',
            createTime : res.msg.createTime,
            contentAbstract : res.msg.contentAbstract,
            picUrl     : res.msg.picUrl
        };
        $('#share-img').html("");
        $('#share-img').qrcode({ width: 130, height: 130, text: 'http://'+window.location.href.split("/")[2] +'/compile/article.html?id=' + $scope.id});
    }, function(res) {
        $('.fail-public-page').removeClass('bjj-hidden');
        $('.success-public-page').removeClass('bjj-hidden');
    });
    $scope.copyUrl = function(evt) {
        var Url=document.getElementById("urlAddress");
        Url.select();
        var tag = document.execCommand("Copy");
        if(tag) {
            bjj.dialog.alert('success', '复制成功！')
        }
    };
};