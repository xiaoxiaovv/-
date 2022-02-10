var statisticsManageCtrl = function ($scope, $http) {
    $scope.pushUserWords = '';
    $scope.isShowSpecificationList = false;
    var currentDate = new Date();
    $scope.startDate   =  new Date(new Date().setHours(0,0,0,0).valueOf()).Format("yyyy-MM-dd hh:mm:ss");
    $scope.endDate     =  new Date(currentDate.valueOf()).Format("yyyy-MM-dd hh:mm:ss");
    $scope.pushStartDate = new Date(new Date().getTime()-(1000*60*60*24)).Format("yyyy-MM-dd");
    $scope.pushEndDate   =  new Date(new Date().getTime()-(1000*60*60*24)).Format("yyyy-MM-dd");
    teamNameData($scope, $http);
    getPublishStatistics($scope, $http);
    var startTime = {
        elem: '#detailStartTime',
        format:"YYYY-MM-DD hh:mm:ss",
        min: '2018-01-01',
        max: new Date(new Date(laydate.now()).getTime()).Format("yyyy-MM-dd"),
        istoday: true,
        istime: true,
        isclear: false,
        choose:function(v){
            $scope.startDate = v;
            endTime.min = v;
            var maxTime = new Date(v).getTime() + 1000*60*60*24*30;
            if(maxTime > new Date(laydate.now()).getTime()) {
                endTime.max = laydate.now();
                $scope.pno = 1;
                specificationListData($scope, $http);
            }else {
                endTime.max = new Date(maxTime).Format("yyyy-MM-dd");
            }
        }
    };
    var endTime = {
        elem:"#detailEndTime",
        format:"YYYY-MM-DD hh:mm:ss",
        min: '2018-01-01',
        max: new Date(new Date(laydate.now()).getTime()).Format("yyyy-MM-dd"),
        istime: true,
        isclear: false,
        istoday: true,
        choose:function(v){
            $scope.endDate = v;
            startTime.max = v;
            var mixTime = new Date(v).getTime() - 1000*60*60*24*30;
            if(mixTime < new Date('2018-01-01').getTime()) {
                startTime.min = '2018-01-01';
            }else {
                startTime.min = new Date(mixTime).Format("yyyy-MM-dd");
            }
            $scope.pno = 1;
            specificationListData($scope, $http);
        }
    };
    var startDate = {
        elem: '#startDate',
        format:"YYYY-MM-DD",
        min: '2018-03-28',
        max: new Date(new Date(laydate.now()).getTime()-1000*60*60*24).Format("yyyy-MM-dd"),
        isclear: false,
        istoday: true,
        choose:function(v){
            $scope.pushStartDate = v;
            endDate.min = v;
            $scope.pno = 1;
            getPublishStatistics($scope, $http);
        }
    };
    var endDate = {
        elem:"#endDate",
        format:"YYYY-MM-DD",
        min: new Date(new Date(laydate.now()).getTime()-1000*60*60*24).Format("yyyy-MM-dd"),
        max: new Date(new Date(laydate.now()).getTime()-1000*60*60*24).Format("yyyy-MM-dd"),
        isclear: false,
        istoday: true,
        choose:function(v){
            $scope.pushEndDate = v;
            startDate.max = v;
            $scope.pno = 1;
            getPublishStatistics($scope, $http);
        }
    };
    setTimeout(function(){
        laydate(startTime);
        laydate(endTime);
        laydate(startDate);
        laydate(endDate);
    },0);
    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.bjj-statistics', specificationListData, 20);
    teamNameData($scope, $http);
    getPublishStatistics($scope, $http);
    getPublish($scope, $http);
    $scope.getData = function() {
        bjj.http.ng.get($scope, $http, '/api/statistics/publish/publishTrend', {
            trendType : $scope.trendType
        }, function (res) {
            var data = res.data;
            var dataArray = res.timeRange;
            var dataobj = {};
            var seriesDate = [];
            var titleDate = [];
            var num = 0;
            var colorDate = ['#a66bbe','#fcad38','#19b698','#ea6153','#5091fd'];
            _.forEach(data,function(v,i) {
                dataobj = {
                    name:i,
                    type:'line',
                    data:v,
                    color:[colorDate[num++]]
                }
                seriesDate.push(dataobj)
                titleDate.push(i)
            })
            drawPublishTrend(dataArray, titleDate, seriesDate)
        })
    }
    $scope.getData()
    $scope.getMonitorIdReprintData = function() {
        $scope.isShowSpecificationList = true;
        $scope.pno = 1;
        specificationListData($scope, $http);
    };
    $scope.listClick = function () {
        $scope.isShowSpecificationList = false;
    }
    $scope.getPushTeamName = function() {
        $scope.pno = 1;
        getPublishStatistics($scope, $http);
    }
    $scope.getTeamName = function() {
        $scope.pno = 1;
        specificationListData($scope, $http);
    }
    $scope.searchClick = function() {
        $scope.pno = 1;
        specificationListData($scope, $http);
    }
    $scope.searchPublisher = function() {
        $scope.pno = 1;
        specificationListData($scope, $http);
    }
    $scope.getPublisher = function() {
        $scope.pno = 1;
        getPublishStatistics($scope, $http);
    }
    $scope.publisherKeyup = function($event) {
        if ($event.keyCode == 13) {
            $scope.pno = 1;
            getPublishStatistics($scope, $http);
        };
    }
    $scope.myKeyup = function(e) {
        if (e.keyCode == 13) {
            $scope.pno = 1;
            specificationListData($scope, $http);
        };
    }
    $scope.countUp = function($event) {
        $($event.target).parent().html(toThousands($scope.publishNumber))
    }
    $scope.yesterdayCountUp = function($event) {
        $($event.target).parent().html(toThousands($scope.yesterdayPublishNumber))
    }
}
var getPublish = function($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/statistics/publish/today', {}, function (res) {
        var publishNum = res.count;
          $scope.publishNumber = publishNum.publishNumber;
          $scope.cmsCount = publishNum.cmsCount;
          $scope.weiboCount = publishNum.weiboCount;
          $scope.weixinCount = publishNum.weixinCount;
          $scope.toutiaoCount = publishNum.toutiaoCount;
          $scope.qqomCount = publishNum.qqomCount;
    })
    bjj.http.ng.get($scope, $http, '/api/statistics/publish/yesterday', {}, function (res) {
        var yesterdayPublishNum = res.count;
        $scope.yesterdayPublishNumber = yesterdayPublishNum.publishNumber;
        $scope.yesterdayCmsCount = yesterdayPublishNum.cmsCount;
        $scope.yesterdayWeiboCount = yesterdayPublishNum.weiboCount;
        $scope.yesterdayWeixinCount = yesterdayPublishNum.weixinCount;
        $scope.yesterdayToutiaoCount = yesterdayPublishNum.toutiaoCount;
        $scope.yesterdayQqomCount = yesterdayPublishNum.qqomCount;
    })
    bjj.http.ng.get($scope, $http, '/api/organization/orgName', {}, function (res) {
        $scope.orgName = res.orgName
    })
}
var drawPublishTrend = function (dataArray, titleDate, seriesDate) {
    var myChart = echarts.init(document.getElementById('publish-trend'));
    myChart.setOption({
        tooltip : {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor: '#6a7985'
                }
            }
        },
        legend: {
            data:titleDate
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: dataArray
        },
        yAxis: {
            type: 'value',
            minInterval: 1
        },
        series : seriesDate
    });
};
var getPublishStatistics = function($scope, $http) {
    bjj.http.ng.get($scope, $http,'/api/statistics/publish/publishStatistics', {
        startDate : $scope.pushStartDate,
        endDate   : $scope.pushEndDate,
        teamName  : $scope.pushTeamName,
        publisher : $scope.pushUserWords
    },function(res) {
        $scope.teamList = res.list;
    })
}
var specificationListData = function($scope, $http) {
    bjj.http.ng.get($scope, $http,'/api/statistics/publish/publishDetail', {
        pageSize        : $scope.psize,
        pageNo          : $scope.pno,
        startDate       : $scope.startDate,
        endDate         : $scope.endDate,
        channelType     : $scope.channelType,
        teamName        : $scope.teamName,
        publisher        : $scope.publisher
    }, function (res) {
        var list = res.list;
        bjj.scroll.loadList.callback($scope, list);
        for(var key in $scope.dataList) {
            if($scope.dataList.length>0) {
                if($scope.dataList[key].teamName == ''|| $scope.dataList[key].teamName == null) {
                    $scope.dataList[key].teamName = "--"
                }
            }
        }
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    }, 'maintenanceListLoading');
};
var teamNameData = function($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/statistics/publish/teamNameList', {}, function (res) {
        $scope.teamNames = res.list;
    })
};
var toThousands =  function (num) {
    var result = [ ], counter = 0;
    num = (num || 0).toString().split('');
    for (var i = num.length - 1; i >= 0; i--) {
        counter++;
        result.unshift(num[i]);
        if (!(counter % 3) && i != 0) { result.unshift(','); }
    }
    return result.join('');
}