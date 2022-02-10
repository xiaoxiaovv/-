var copyrightMonitorCtrl = function ($rootScope, $scope, $http, $state, $stateParams) {
    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.containerBox', getMonitorNewsListData, 10);
    $scope.monitorId = $stateParams.monitorId;
    $scope.monitorListQueryType = 0;
    $scope.monitorListQuery = '全部';
    getMonitor($scope, $http);
    getMonitorNewsListData($scope, $http);

    $scope.listClick = function(){
        $scope.show = false;
    }
    /*单选*/
    $scope.toggleNewsList = function () {
        this.item.active = !this.item.active;
        $scope.active = false;
        $scope.news = _.filter($scope.monitorList, {'active' : true});
        if($scope.news.length == $scope.monitorList.length){
            $scope.active = true;
        }
    };

    /*全选*/
    $scope.toggleAllNewsList = function () {
        $scope.active = !$scope.active;
        angular.forEach($scope.monitorList, function (v,i) {
            if($scope.active) {
                v.active = true;
            }else {
                v.active = undefined;
            }
        });
        $scope.news = _.filter($scope.monitorList, {'active' : true});
    };
    //下载
    $scope.downLoad = function(){
        var copyRightNewsIds = [];
        _.forEach($scope.news, function (v, i) {
            copyRightNewsIds.push(v._id);
        });
        if (copyRightNewsIds.length == 0) {
            bjj.dialog.alert('danger', '请选择你要下载的新闻');
            return;
        };
        bjj.http.ng.postBody($scope, $http, '/api/copyright/monitor/'+$scope.monitorId+'/news/excel', JSON.stringify({
            copyRightNewsIds: copyRightNewsIds.join(",")
        }), function(res){
            window.location.href = '/api/copyright/monitor/' + $scope.monitorId + '/news/excel/' + res.token;

            bjj.dialog.alert('success', '下载成功！');
            angular.forEach($scope.monitorList, function (v, i) {
               $scope.active = false;
               v.active = undefined;
            });
        },function(res){
            bjj.dialog.alert('danger', res.msg);
        });
    };
    /* 删除*/
    $scope.delete = function(){
        var newsIds=[];
        _.forEach($scope.news, function (v, i) {
            newsIds.push(v._id);
        });
        if(newsIds.length == 0){
            bjj.dialog.alert('danger', '请选择你要删除的新闻列表');
            return;
        };
        bjj.dialog.confirm('确定删除？', function () {
            bjj.http.ng.del($scope, $http, '/api/copyright/monitor/' + $scope.monitorId + '/news/' + newsIds.join(',') + '?_method=DELETE', {}, function (res) {
                var list = $scope.dataList;
                for (var i = list.length-1; i >= 0; i--) {
                    if (newsIds.indexOf(list[i]._id) > -1) {
                        $scope.dataList.splice(i, 1);
                    }
                }
                if($scope.dataList.length <= 0){
                    $scope.dataListBottomFinalView = false;
                    $scope.dataListEmptyView = true;
                }
                bjj.dialog.alert('success', res.msg);
                $scope.active = false;
            }, function (res) {
                bjj.dialog.alert('danger', res.msg);
            });
        });
    };
    /*筛选*/
    $scope.getMonitorNewsList = function($event){
        $scope.monitorListQueryType  = $event.target.dataset.queryType ;
        if($scope.monitorListQueryType == 0) {
            $scope.monitorListQuery = '全部'
        }
        if($scope.monitorListQueryType == 2) {
            $scope.monitorListQuery = '重点监控'
        }
        if($scope.monitorListQueryType == 3) {
            $scope.monitorListQuery = '疑似侵权'
        }
        $('.input-group .total#total').text($event.target.innerHTML);
        $scope.pno = 1;
        getMonitorNewsListData($scope, $http);
    };

    /*传播分析*/
    $scope.getMonitorIdReprintData = function(){
        $scope.show = true;

        getAnalysisSummaryData($scope, $http);
        getReprintTrendData($scope, $http);
        getChannelReprintSummaryData($scope, $http);
    };
};
var getMonitor = function($scope, $http){
    bjj.http.ng.get($scope, $http, '/api/copyright/monitor/' + $scope.monitorId, {}, function (res) {
        $scope.monitor = res.msg;
        $scope.jumpCopy = function() {
            window.open($scope.monitor.url);
        }
    });
};
var getMonitorNewsListData = function($scope, $http){
    bjj.http.ng.get($scope, $http,'/api/copyright/monitor/'+ $scope.monitorId + '/news', {
        pageSize    : $scope.psize,
        pageNo      : $scope.pno,
        queryType   : $scope.monitorListQueryType
    }, function (res) {
        var list = res.msg;
        if(list.length <= ($(window).height() - 287)/ 247 ) {
            $scope.containerMonitorBox = true;
        }else {
            $scope.containerMonitorBox = false;
        }
        bjj.scroll.loadList.callback($scope, list);
        $scope.monitorList = $scope.dataList;
    });
};

var getAnalysisSummaryData = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/copyright/monitor/' + $scope.monitor.monitorId + '/analysis/summary', {}, function (res) {
        var result = res.msg;
        var getShowColor = function (yes, no) {
            var colors = ['#d0278a', '#ff3259', '#ffb630', '#04c3e7', '#91db29'];
            no = no >= 0 ? no : 0;
            var pre = parseInt(yes*100 / (yes + no));
            var c_index = 0;
            if(pre <= 25) c_index = 0;
            else if(pre > 25 && pre <= 50) c_index = 1;
            else if(pre > 50 && pre <= 75) c_index = 2;
            else if(pre > 75 && pre < 100) c_index = 3;
            else if(pre >= 100) c_index = 4;
            return colors[c_index];
        };
        var list = [
            {x: '12.5%', y: '25%', color: '#1159d8', yes: result.reprintMediaCount, no: result.reprintMediaAverage - result.reprintMediaCount},
            {x: '37.5%', y: '25%', color: '#5b0cd8', yes: result.reprintCount, no: result.reprintAverage - result.reprintCount},
            {x: '62.5%', y: '25%', color: '#a90899', yes: result.searchEngineCount, no: result.searchEngineAverage - result.searchEngineCount},
            {x: '87.5%', y: '25%', color: '#f6104e', yes: result.keyChannelCount, no: result.keyChannelAverage - result.keyChannelCount},
            {x: '12.5%', y: '75%', color: '#fd6934', yes: result.weiboReprintCount, no: result.weiboReprintAverage - result.weiboReprintCount},
            {x: '37.5%', y: '75%', color: '#fda826', yes: result.weMediaReadCount, no: result.weMediaReadAverage - result.weMediaReadCount},
            {x: '62.5%', y: '75%', color: '#138fc7', yes: result.commentCount, no: result.commentAverage - result.commentCount},
            {x: '87.5%', y: '75%', color: '#13b8c7', yes: result.likeCount, no: result.likeAverage - result.likeCount}
        ];
        var legendData = ['转载媒体数', '文章数', '搜索引擎收录', '重点频道刊载', '微博转发', '自媒体阅读', '评论总量', '点赞量'];
        var seriesObj = _.map(list, function (v, i) {
            return {
                type : 'pie',
                clockwise: true,
                center : [v.x, v.y],
                radius : [45, 60],
                minAngle: 0,
                stillShowZeroSum: true,
                label : {
                    normal : {
                        show : true,
                        formatter: v.yes + '',
                        textStyle: {
                            fontSize: 14,
                            color: '#999'
                        }
                    }
                },
                data : [{
                    name        : legendData[i],
                    value       : v.yes,
                    itemStyle   : {
                        normal : {
                            color: v.color,
                            label : {
                                show : true,
                                position : 'center',
                                formatter : '{b}',
                                textStyle: {
                                    fontSize: 14,
                                    color: '#999'
                                }
                            }
                        }
                    }
                }, {
                    name        : 'other',
                    value       : v.no < 0 ? 0 : v.no,
                    itemStyle   : {
                        normal : {
                            color: '#ddd',
                            label : {
                                show : true,
                                position : 'center'
                            }
                        },
                        emphasis: {
                            color: 'rgba(0,0,0,.1)'
                        }
                    }
                }]
            };
        });
        drawAnalysisSummary(legendData, seriesObj);
    }, function (res) {
        bjj.dialog.alert('danger', res.msg);
    }, 'copyrightAnalysisLoading');
};
var getReprintTrendData = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/copyright/monitor/' + $scope.monitor.monitorId + '/analysis/reprintTrend', {}, function (res) {
        var result = res.msg;
        result = _.sortBy(result, function (v) { return v.date; });
        var legendData = ['发稿量'];
        var seriesData = _.map(result, function (v) {
            return parseInt(v.count);
        });
        var seriesObj = [{
            name: legendData[0],
            type:'line',
            data: seriesData,
            markPoint : {
                data : [
                    {type : 'max', name: '最大值'},
                    {type : 'min', name: '最小值'}
                ]
            },
            markLine : {
                data : [
                    {type : 'average', name: '平均值'}
                ]
            }
        }];
        var xAxisData = _.map(result, function (v) {
            return new Date(parseInt(v.date)).Format('M月d日');
        });
        drawReprintTrend(legendData, seriesObj, xAxisData);
    }, function(res){
        bjj.dialog.alert('danger', res.msg);
    }, 'copyrightAnalysisTrendLoading');
};
var getChannelReprintSummaryData = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/copyright/monitor/' + $scope.monitor.monitorId + '/analysis/channelReprintSummary', {}, function (res) {
        var result = res.msg;
        result = _.sortBy(result, function (v) { return v.percentage; });

        var legendData = _.map(result, function (v) {
            var percentage = parseInt(v.percentage * 100), name = v.channelName;
            return percentage + '%' + name;
        });
        var seriesObj = _.map(result, function (v, i) {
            var percentage = parseInt(v.percentage * 100), name = v.channelName;
            return {
                name: name,
                type: 'pie',
                clockWise: false,
                radius : [i*25 + 50,  (i+1)*25 + 50],
                itemStyle : {
                    normal: {
                        label: {show:false},
                        labelLine: {show:false}
                    }
                },
                data:[{
                    value: percentage,
                    name: percentage + '%' + name
                },{
                    value: 100 - percentage,
                    name:'invisible',
                    itemStyle : {
                        normal : {
                            color: 'rgba(0,0,0,0)',
                            label: {show:false},
                            labelLine: {show:false}
                        },
                        emphasis : {
                            color: 'rgba(0,0,0,0)'
                        }
                    }
                  }
                ]
            };
        });
        drawChannelReprintSummary(legendData, seriesObj);
    },function(res){
        bjj.dialog.alert('danger', res.msg);
    }, 'copyrightAnalysisSummaryLoading');
};
var drawAnalysisSummary = function (legendData, seriesObj) {
    var myChart = echarts.init(document.getElementById('news-analysis-summary'));
    myChart.setOption({
        legend: {
            x : 'center',
            y : 'center',
            data: legendData,
            selectedMode: false
        },
        series : seriesObj
    });
};
var drawReprintTrend = function (legendData, seriesObj, xAxisData) {
    var myChart = echarts.init(document.getElementById('news-analysis-weekly-reprint-trend'));
    myChart.setOption({
        title : {
            text: '传播走势',
        },
        color: ["#5578c2"],
        tooltip : {
            trigger: 'axis'
        },
        legend: {
            data: legendData
        },
        calculable : true,
        xAxis : [
            {
                type : 'category',
                boundaryGap : false,
                data : xAxisData
            }
        ],
        yAxis : [
            {
                type : 'value',
                axisLabel : {
                    formatter: '{value}'
                }
            }
        ],
        series : seriesObj
    });
};
var drawChannelReprintSummary = function (legendData, seriesObj) {
    var myChart = echarts.init(document.getElementById('news-analysis-channel-reprint-summary'));
    myChart.setOption({
        title: {
            text: '各渠道转载分布',
        },
        color:["#5793f3","#9957f3","#dd4d7a", "#de4545", "#fb5a35", "#9957f3 ", "#cd4ddc", "#be3d47", "#5793f3", "#5578c2"],
        tooltip : {
            show: true,
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        legend: {
            orient : 'vertical',
            x : document.getElementById('news-analysis-channel-reprint-summary').offsetWidth / 2,
            y : 45,
            itemGap:12,
            data: legendData
        },
        series : seriesObj
    });
};
