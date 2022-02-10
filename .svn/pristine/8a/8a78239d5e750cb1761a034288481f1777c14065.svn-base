/**
 * Author : YCSnail
 * Date : 2017-07-25
 * Email : liyancai1986@163.com
 */
var analysisCtrl = function ($scope, $http, $state) {
    /*一周综合传播测评*/
    bjj.http.ng.get($scope, $http, "/api/analysis/channel/weeklySummary" , {}, function (res) {
        $scope.list = res.list;
        $scope.sum = res.sum;
    },'weeklySummaryLoading');
    /*一周稿件排行*/
    bjj.http.ng.get($scope, $http, "/api/analysis/channel/weeklyNews" , {}, function (res) {
        $scope.weeklyNewsList = res.list;
    },function(res){
        bjj.dialog.alert('danger', res.msg);
    },'weeklyNewsLoading');
    /*最新稿件*/
    bjj.http.ng.get($scope, $http, "/api/analysis/channel/latestNews " , {}, function (res) {
        $scope.latestNewsList = res.list;
    },function(res){
        bjj.dialog.alert('danger', res.msg);
    },'latestNewsLoading');
    $scope.siteTypes = [{siteType: '网站', value: "1"}, {siteType: '微博', value: "2"}, {siteType: '微信', value: "3"}];

    bjj.http.ng.get($scope, $http, "/api/analysis/channel" ,{}, function (res) {
        $scope.orgName = res.msg.orgName;
    })
    /*点击渠道设置*/
    $scope.sitesSet = function() {
        bjj.http.ng.get($scope, $http, "/api/analysis/channel" ,{}, function (res) {
            $scope.orgName = res.msg.orgName;
            $scope.mediaSettingList = res.msg.sites;
            for(var i=0;i< $scope.mediaSettingList.length;i++){
                $scope.mediaSettingList[i].siteType = $scope.mediaSettingList[i].siteType +"";
                $scope.changeType()
            }
            if($scope.mediaSettingList.length < 1 ){
                var defaultMediaSetting = {siteType:"1",siteName:"",siteDomain:"",placeholder:'输入站点名称',placeholderDomain:'输入域名'};
                $scope.mediaSettingList.unshift(defaultMediaSetting)
            }
        });
    };
    /*改变类型的默认提示切换*/
    $scope.changeType = function() {
        for(var i=0;i<$scope.mediaSettingList.length;i++) {
            if($scope.mediaSettingList[i].siteType == 1) {
                $scope.mediaSettingList[i].placeholder = "输入站点名称";
                $scope.mediaSettingList[i].placeholderDomain = "输入域名";
            }else if($scope.mediaSettingList[i].siteType == 2) {
                $scope.mediaSettingList[i].placeholder = "输入微博账号";
            }else if($scope.mediaSettingList[i].siteType == 3) {
                $scope.mediaSettingList[i].placeholder = "输入公众号名称";
            }
        }
    };
    /*保存*/
    $scope.saveSetting = function(){
        var sites = [];
        var orgName = $scope.orgName;
        var size = $scope.mediaSettingList.length;
        for(var i=0;i< size;i++){
            var curSiteType = $scope.mediaSettingList[i].siteType;
            var curSiteName = $scope.mediaSettingList[i].siteName;
            var curSiteDomain = $scope.mediaSettingList[i].siteDomain;
            if (curSiteType == 1){
                if (curSiteName.trim() == "" ||  curSiteDomain.trim() == ""){
                    bjj.dialog.alert('info', '请将渠道信息填写完整！');
                    return;
                }else{
                    sites.push({siteType:parseInt(curSiteType),siteName:curSiteName,siteDomain:curSiteDomain})
                }
            }else {
                if(curSiteName.trim() != ""){
                    sites.push({siteType:parseInt(curSiteType),siteName:curSiteName})
                }else {
                    bjj.dialog.alert('info', '请将渠道信息填写完整！');
                    return;
                }
            }
        }
        if(orgName.trim() == ""){
            bjj.dialog.alert('info', '请填写机构名称');
            return
        }
        bjj.http.ng.put($scope, $http, '/api/analysis/channel?', {
            sites:JSON.stringify(sites),
            orgName:orgName
        }, function (response) {
            bjj.dialog.alert('success', '保存成功！', {
                callback:   function() {
                    window.reloadPage()
                }
            });
        });
    }
    $scope.deleteRecord = function(index){
        $scope.mediaSettingList.splice(index,1);
        if($scope.mediaSettingList.length == 0){
            bjj.dialog.alert('info', '最少添加一个渠道');
            var defaultMediaSetting = {siteType:"1",siteName:"",siteDomain:"",placeholder:"输入站点名称",placeholderDomain:"输入域名"};
            $scope.mediaSettingList.unshift(defaultMediaSetting)
        }
    }
    $scope.addRecord = function(){
        if($scope.mediaSettingList.length >= 5){
            bjj.dialog.alert('success', '最多设置5个');
            return
        }
        var defaultMediaSetting = {siteType:"1",siteName:"",siteDomain:"",placeholder:"输入站点名称",placeholderDomain:"输入域名"};
        $scope.mediaSettingList.push(defaultMediaSetting)
    }
};
