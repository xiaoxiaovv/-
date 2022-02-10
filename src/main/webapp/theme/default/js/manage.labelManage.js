let labelManageCtrl = function ($rootScope, $scope, $http) {
	$scope.editPage = false;
	$scope.labelType = '';
	$scope.showInput = false;
	$scope.LabelList = [];
	$scope.labelId = '';
	$scope.labelName = '';
	$scope.checkList = '';
	getlLabelList($scope, $http);
	$scope.editLabel = function () {
		$scope.editPage = true;
		$scope.labelType = '';
		$scope.showInput = false;
		$scope.labelName = '';
		$scope.labelId = '';
		$scope.checkList = '';
	}
	$scope.editLabelDetail = function (id) {
		$scope.editPage = true;
		$scope.labelId = id;
		getlLabelDetail($scope, $http, id)
	}
	$scope.deleteLabel = function (id) {
		bjj.dialog.confirm(' 此操作可能会引起数据丢失，是否继续执行？', function () {
			LabelDelete($scope, $http, id);
		});	
	}
	$scope.typeChange = function () {
		console.log($scope.labelType);
		$scope.showInput = false;
		$scope.checkList = '';
		if($scope.labelType == 'checkbox' || $scope.labelType == 'drop'){
			$scope.checkList = ['',''];
			$scope.showInput = true;
		}
	}
	$scope.changeInput = function (t) {
		$scope.checkList[t.$index] = t.item;
	}
	$scope.addValue = function () {
		$scope.checkList.push('');
	}
	$scope.removeValue = function (i) {
		if(i<=1){
			bjj.dialog.alert('danger', '不能少于两个');
			return;
		}
		$scope.checkList.splice(i,1);
	}
	$scope.cancelEdit = function () {
		$scope.editPage = false;
	}
	$scope.addOrEdit = function () {
		if($scope.labelName == ''){
			bjj.dialog.alert('danger', '标签名称不能为空');
			return;
		}
		if($scope.labelType == ''){
			bjj.dialog.alert('danger', '请选择类型');
			return;
		}
		if($scope.labelType == 'checkbox' || $scope.labelType == 'drop'){
			for(var i = 0;i<$scope.checkList.length;i++){
				if($scope.checkList[i] == ''){
					bjj.dialog.alert('danger', '不能有空白项');
					return;
				}
			}
		}
		if($scope.labelId) {
			editLabel($scope, $http, $scope.labelId)
		}
		else
		{
			addLabelList($scope, $http)
		}
	}
}
let getlLabelList = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/tags/list', {}, function (res) {
        $scope.LabelList = res.list;
        _.map($scope.LabelList,function(v){
        	if(v.tagType == 'drop'){
        		v.tagType = '下拉'
        	}
        	if(v.tagType == 'checkbox'){
        		v.tagType = '复选'
        	}
        	if(v.tagType == 'text'){
        		v.tagType = '文本'
        	}
        	if(v.tagType == 'date'){
        		v.tagType = '日期'
        	}
        	if(v.tagType == 'area'){
        		v.tagType = '地域'
        	}
        })
    },function(res){
    	bjj.dialog.alert('danger', res.msg);
    });
};
let LabelDelete = function ($scope, $http, id) {
    bjj.http.ng.del($scope, $http, '/api/tags/delete/' + id, {}, function (res) {
        bjj.dialog.alert('success', '删除成功');
        getlLabelList($scope, $http);
    },function(res){
    	bjj.dialog.alert('danger', res.msg);
    });
};
let getlLabelDetail = function ($scope, $http, id) {
    bjj.http.ng.get($scope, $http, '/api/tags/detail/' + id, {}, function (res) {
        console.log(res);
        $scope.labelName = res.tag.tagName;
        $scope.labelType = res.tag.tagType;
        $scope.checkList = res.tag.tagValue;
        if($scope.checkList && $scope.checkList.length == 0){
        	$scope.checkList = '';
        }
        $scope.showInput = true;
    },function(res){
    	bjj.dialog.alert('danger', res.msg);
    });
};
let addLabelList = function ($scope, $http) {
    bjj.http.ng.post($scope, $http, '/api/tags/add', {
    	tagName:$scope.labelName,
    	tagType:$scope.labelType,
    	tagValue:$scope.checkList,
    }, function (res) {
       bjj.dialog.alert('success', res.msg, {
       			callback: function () {
       				$scope.editPage = false;
			       	getlLabelList($scope, $http);
       				}
       			});
       
    },function(res){
    	bjj.dialog.alert('danger', res.msg);
    });
};
let editLabel = function ($scope, $http, id) {
    bjj.http.ng.put($scope, $http, '/api/tags/update/' + id, {
    	tagName:$scope.labelName,
    	tagType:$scope.labelType,
    	tagValue:$scope.checkList,
    }, function (res) {
        bjj.dialog.alert('success', res.msg, {
       			callback: function () {
       				$scope.editPage = false;
			       	getlLabelList($scope, $http);
       				}   	
       			});
    },function(res){
    	bjj.dialog.alert('danger', res.msg);
    });
};
