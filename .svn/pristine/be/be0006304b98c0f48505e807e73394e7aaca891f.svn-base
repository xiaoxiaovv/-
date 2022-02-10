/**
 * Author : YCSnail
 * Date : 2017-07-27
 * Email : liyancai1986@163.com
 */
var materialListCtrl = function ($scope, $http, $sce) {
    $scope.siteType = '0';
    $scope.publicType = '0';
    $scope.materialActive = false;
    $scope.materialIds = [];
    bjj.scroll.loadList.init($scope, $http, '.bjj-content', '.material--module', getMaterialListData, 10);
    getMaterialListData($scope, $http);
    /*获得条数*/
    getAllNum($scope, $http)
    /*删除素材*/
    $scope.deleteMaterialListData = function (id) {
        bjj.dialog.confirm('确定删除？', function () {
            bjj.http.ng.del($scope, $http, '/api/compile/material/' + id + '?_method=delete', {}, function (res) {
                bjj.dialog.alert('success', '删除成功');
                $scope.materialActive = false;
                $scope.materialIds = [];
                getAllNum($scope, $http);
                $scope.pno = 1;
                getMaterialListData($scope, $http);
                bjj.scroll.loadList.init($scope, $http, '.bjj-content', '.material-body', getMaterialListData, 10);
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            }) 
        })
    };
    $scope.materialType = [{materialType: '未同步', value: "2"}, {materialType: '同步成功', value: "1"}, {materialType: '同步失败', value: "4"}];
    $scope.materialPublishType = [{publishType: '已发布', value: "1"}, {publishType: '未发布', value: "2"}];
    $scope.allMaterialsId = function() {
        $scope.materialActive = !$scope.materialActive;
        $scope.materialIds = [];
        _.forEach($scope.materials, function(data) {
            if($scope.materialActive) {
                data.active = true;
                $scope.materialIds.push(data.id)
            }else {
                data.active = false;
            }
        })
    }
    $scope.getMaterialsId = function() {
        this.item.active = !this.item.active;
        if($scope.materials.every( item => item.active === true)) {
            $scope.materialActive = true;
        }else {
            $scope.materialActive = false;
            $scope.materialIds = [];
        }
        _.forEach($scope.materials, function(data) {
            if(data.active) {
                $scope.materialIds.push(data.id)
            }
        })
    }
    $scope.deleteMaterials = function() {
        if($scope.materialIds.length == 0) {
            bjj.dialog.alert('danger', '请选择要删除的文稿');
            return;
        }
        bjj.dialog.confirm('确定删除所有文稿？', function(){
            bjj.http.ng.postBody($scope, $http, '/api/compile/material/remove', JSON.stringify({
                materialIds: $scope.materialIds.join(',')
            }), function (res) {
                $scope.materialActive = false;
                $scope.materialIds = [];
                $scope.pno = 1;
                getMaterialListData($scope, $http);
                getAllNum($scope, $http)
            }, function (res) {
            },function(res) {
                bjj.dialog.alert('danger', res.msg);
            })
        })
    }
    /*筛选*/
    $scope.materialState = function() {
        $scope.materialIds = [];
        $scope.materialActive = false;
        $scope.pno = 1;
        getMaterialListData($scope, $http);
        getAllNum($scope, $http);
    };
    $(window).keydown(function (event) {
        if (event.keyCode == 13) {
            $scope.pno = 1;
            $scope.materialIds = [];
            $scope.materialActive = false;
            getMaterialListData($scope, $http);
            getAllNum($scope, $http);
        };
    });
};
var getMaterialListData = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/compile/materials', {
        queryKeyWords : $scope.keyWord,
        isPublished     : $scope.siteType,
        isReleased      : $scope.publicType,
        pageNo      : $scope.pno,
        pageSize    : $scope.psize
    }, function (res) {
        var list = res.msg;
        bjj.scroll.loadList.callback($scope, list);
        $scope.materials = $scope.dataList;
        forEachImg($scope);
    }, 'materialLoading');
};
var getEditorPreviewData = function($scope, $http, $sce, id) {
    bjj.http.ng.get($scope, $http, '/api/compile/material/detail/' + id, {}, function (res) {
        $scope.data = res.msg.material;
        $scope.data.content = $sce.trustAsHtml($scope.data.content);
        $scope.previewArticle = {
            title      : res.msg.material.title,
            author     : res.msg.material.author ? res.msg.material.author : '匿名',
            updateTime : res.msg.material.updateTime,
            contentAbstract : res.msg.material.contentAbstract,
            picUrl     : res.msg.material.picUrl
        };
    });
};
var forEachImg = function ($scope) {
    $scope.materials.forEach(function(v,i) {
        if(v.picUrl == "") {
            v.picUrl = "/theme/" + $scope.appInfo.appKey + "/images/default-img.jpg"
        }
    })
};
var getAllNum = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/compile/materials/count', {
        queryKeyWords : $scope.keyWord,
        isPublished     : $scope.siteType,
        isReleased      : $scope.publicType
    }, function (res) {
        $scope.aLLNum = res.msg;
    });
};
