/**
 * Author : YCSnail
 * Date : 2017-07-27
 * Email : liyancai1986@163.com
 */
var accountSubjectModifyCtrl = function ($rootScope, $scope, $http, $state, $stateParams, $timeout) {

    $scope.subjectId = $stateParams.subjectId;
    $scope.isShowView = false;
    $scope.subjectEditPage = true;
    $scope.expressionStatus = {
        newKeywordsTagValue : '',
        newKeywordsTagView  : false
    };

    if($scope.subjectId == undefined || $scope.subjectId == 0){
        $scope.subject = {
            subjectId   : '',
            keyWords    : '',
            titleKeywords: '',
            keywordsScope: 1,   //默认按照正文检索
            excludeWords: '',
            subjectName : ''
        };
        $scope.subjectEditPage = false;
        $scope.keywordsExpression = [];
        $(".account-subject-list .nav-list li").removeClass('active');
    } else {
        getAccountSubjectDetail($scope, $http);
    }

    $scope.save = function() {
        if($scope.subject.subjectName == "") {
            bjj.dialog.alert('danger', '请填写主题名称！');
            return;
        }

        var params =  {
            subjectName : encodeURIComponent($scope.subject.subjectName.trim()),
            keyWords    : encodeURIComponent(JSON.stringify($scope.keywordsExpression)),
            titleKeywords: encodeURIComponent(($scope.subject.titleKeywords || "").trim()),
            keywordsScope: $scope.subject.keywordsScope,
            excludeWords: encodeURIComponent(($scope.subject.excludeWords || "").trim())
        };

        if($scope.subjectEditPage) {
            $scope.updateSubject(params);
        } else {
            $scope.addSubject(params);
        }
    };
    $scope.addSubject = function (params) {
        bjj.http.ng.post($scope, $http, '/api/capture/subject', params, function (res) {
            bjj.dialog.alert('success', '保存成功！', {
                callback: function () {
                    $state.go("accountSubject", {reload: true, captureSubjectId: res.subjectId});
                }
            });
        }, function(res) {
            bjj.dialog.alert('danger', res.msg);
        });
    };
    $scope.updateSubject = function (params) {
        bjj.http.ng.put($scope, $http, '/api/capture/subject/' + $scope.subjectId, params, function (res) {
            bjj.http.ng.get($scope, $http, '/api/capture/subjects', {}, function (res) {
                $rootScope.subjectList = res.list;
            });
            bjj.dialog.alert('success', '保存成功！', {
                callback: function () {
                    $state.go("accountSubject.modify", { subjectId: $scope.subjectId });
                }
            });
        }, function(res) {
            bjj.dialog.alert('danger', res.msg);
        });
    };
    /*删除主题*/
    $scope.deleteSubject = function(){
        bjj.dialog.confirm('删除主题后，本主题下内容将全部删除，确定删除吗?', function(){
            bjj.http.ng.del($scope, $http, '/api/capture/subject/' + $scope.subjectId + '?_method=delete', {}, function (res) {
                bjj.dialog.alert('success', '删除成功！');
                $scope.isShowView = true;
                $scope.getSubjectList();
                $scope.isShowView = true;
            },function(res) {
                bjj.dialog.alert('danger', res.msg);
                $scope.isShowView = false;
            });
        });
    };
    $scope.getSubjectList = function () {
        bjj.http.ng.get($scope, $http, '/api/capture/subjects', {}, function (res) {
            $rootScope.subjectList = res.list;
            if($rootScope.subjectList.length > 0){
                setTimeout(function () {
                    $(".account-subject-list ul li").eq(0).addClass("active");
                    $state.go("accountSubject.modify", { subjectId: res.list[0].subjectId });
                }, 0);
            }
        }, function(res) {
            $rootScope.subjectList = [];
        });
    };

    // 关键词相关事件
    $scope.toggleOperator = function () {
        this.item.operator = this.item.operator == 'or' ? 'and' : 'or';
    };
    $scope.toggleSubOperator = function () {
        var that = this;
        $timeout(function () {
            that.item.subOperator = that.item.subOperator == 'or' ? 'and' : 'or';
        }, 200);
    };
    $scope.openEditMode = function () {
        this.item.editMode = true;
        var _index = this.$index;
        setTimeout(function () {
            $('.keywordsTag-' + _index).focus();
        }, 100);
    };
    $scope.updateKeywordsTag = function () {
        var value = $('.keywordsTag-' + this.$index).val().trim();
        if(value == '') {
            this.item.editMode = false;
            return;
        }
        value = value.replace(/\s+/g, ' ');
        this.item.keywords = value.split(' ');
        this.item.editMode = false;
    };
    $scope.delKeywordsTag = function () {
        var _index = this.$index;
        var length = $scope.keywordsExpression.length;
        if(length > 1) {
            if(_index % 2 == 1) return;
            if(_index == 0) {
                $scope.keywordsExpression.splice(_index + 1, 1);
                $scope.keywordsExpression.splice(_index, 1);
            } else {
                $scope.keywordsExpression.splice(_index, 1);
                $scope.keywordsExpression.splice(_index - 1, 1);
            }
        } else {
            $scope.keywordsExpression.splice(_index, 1);
        }
    };
    $scope.openNewKeywordsTag = function () {
        $scope.expressionStatus.newKeywordsTagView = true;
        setTimeout(function () {
            $('.newKeywordsTag').focus();
        }, 0);
    };
    $scope.doAddKeywords = function () {
        // 1. 处理关键词Tag变为数组
        var value = $scope.expressionStatus.newKeywordsTagValue.trim();
        if(value == '') {
            $scope.expressionStatus.newKeywordsTagView = false;
            $scope.expressionStatus.newKeywordsTagValue = '';
            return;
        }
        value = value.replace(/\s+/g, ' ');
        var tag = {
            subOperator : 'or',
            keywords    : value.split(' ')
        };
        // 2. 追加到表达式列表
        if($scope.keywordsExpression.length > 0){
            $scope.keywordsExpression.push({ operator: 'and'});
        }
        $scope.keywordsExpression.push(tag);
        // 3. 将输入框隐藏，新增按钮显示
        $scope.expressionStatus.newKeywordsTagView = false;
        // 4. newKeywordsTag 置空
        $scope.expressionStatus.newKeywordsTagValue = '';
    };
    $(window).keydown(function (event) {
        if (event.keyCode == 13) {
            $('.newKeywordsTag').blur();
            $('.textarea-keyword .item input').blur();
        };
    });
    $scope.toggleKeywordsScope = function (v) {
        $scope.subject.keywordsScope = v;
    }
};

var getAccountSubjectDetail = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/capture/subject/' + $scope.subjectId, {}, function (res) {
        $scope.subject = res.msg;
        try {
            $scope.keywordsExpression = JSON.parse(res.msg.keyWords);
        } catch(e){
            $scope.keywordsExpression = [];
        }
        _.map($scope.keywordsExpression, function (v) {
            v.editMode = false;
        })
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    });
};
