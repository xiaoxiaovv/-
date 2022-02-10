var evaluateManagementCtrl = function ($rootScope, $scope, $http) {
    $rootScope.minBar = true;
    getReportStatus($scope, $http)
    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.evaluate-body-managemente', getReportList, 30);
    getReportList($scope, $http)
    $scope.deleteEvaluate = function () {
        if (this.item.status == 1) {
            return;
        }
        var that = this;
        bjj.dialog.confirm('是否确认删除该测评？', function () {
            delReport($scope, $http, that.item.id)
        })
    }
    $scope.operateEvaluate = function () {
        getTrendData($scope, $http, this.item.id);
    }
    $scope.changeStatus = function() {
        $scope.pno = 1;
        getReportList($scope, $http)
    }
    //重试
    $scope.retry = function () {
        retryEvaluateReport($scope, $http, this.item.id)
    }
    $scope.downLoadEvaluate = function () {
        window.open("http://" + window.location.host + "/api/evaluate/report/" + this.item.id);
    }
    $scope.searchClick = function (e) {
        $scope.pno = 1;
        getReportList($scope, $http)
    };
    $scope.myKeyup = function (e) {
        if (e.keyCode == 13) {
            $scope.pno = 1;
            getReportList($scope, $http)
        }
    };
}
//报告状态
let getReportStatus = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, "/api/evaluate/status", {}, function (res) {
        $scope.statusList = res.list;
    })
}
//获取测评列表
let getReportList = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, "/api/evaluate/report", {
        pageNo: $scope.pno,
        pageSize: $scope.psize,
        status: $scope.status? $scope.status: '',
        evaluateName: $scope.evaluateName? $scope.evaluateName: '', //报告名称
    }, function (res) {
        if(res.list.length == 0) {
            $scope.evaluateListEmptyView = true;
        }else {
            $scope.evaluateListEmptyView = false;
        }
        var list = res.list;
        bjj.scroll.loadList.callback($scope, list);
        $scope.evaluateListEmptyMsg = res.msg;
    }, 'evaluateListLoading')
}
//删除测评
let delReport = function ($scope, $http, reportId) {
    bjj.http.ng.del($scope, $http, "/api/evaluate/report/" + reportId, {}, function (res) {
        $scope.msg = res.msg;
        $scope.pno = 1;
        getReportList($scope, $http)
    })
}
//重试
let retryEvaluateReport = function ($scope, $http, reportId) {
    bjj.http.ng.put($scope, $http, "/api/evaluate/report/" + reportId, {}, function (res) {
        $scope.msg = res.msg;
        $scope.pno = 1;
        getReportList($scope, $http)
    })
}
//生成报告
let createEvaluateReport = function ($scope, $http, reportId) {
    $scope.arr = [];
    var trendType = ["publishCount", "multiple", "psi", "mii", "tsi", "bsi"]
    _.forEach(trendType, function(i) {
        dealsData($scope, i)
    })
    getContentReadAndLike($scope, $http, reportId)
    setTimeout(function() {
        bjj.http.ng.postBody($scope, $http, "/api/evaluate/report/" + reportId, JSON.stringify({
            imgList: $scope.arr
        }), function (res) {
            $scope.msg = res.msg;
            $scope.pno = 1;
            bjj.dialog.alert('success', res.msg);
            getReportList($scope, $http)
            $scope.operateName = '下载报告';
        }, function (res) {
            bjj.dialog.alert('danger', res.msg);
        })
    },2000)

}
let getTrendData = function ($scope, $http, evaluateId) {
    bjj.http.ng.get($scope, $http, '/api/evaluate/tendency', {
        evaluateId:evaluateId
    }, function (res) {
        $scope.evaluateTendencyData = res.info;
        createEvaluateReport($scope, $http, evaluateId)
    });
}
//平均点赞分布  最外面的avgRead   avgLike 代表坐标中心 data 里面是数据
let getContentReadAndLike = function ($scope, $http, evaluateId) {
    bjj.http.ng.get($scope, $http, "/api/evaluate/content/readAndLike", {
        evaluateId:evaluateId
    }, function (res) {
        $scope.list = res.list;

        var axisInfo = { x : parseInt(res.list.avgRead), y : parseInt(res.list.avgLike) };
        var seriesData = _.map(res.list.data, function (v) {
            return [v.avgRead - axisInfo.x, v.avgLike - axisInfo.y];
        });
        drawContentReadAndLike($scope, seriesData, axisInfo);
        getContentKeywords($scope, $http, evaluateId)
    })
};
//词云分析
let getContentKeywords = function ($scope, $http, evaluateId) {
    bjj.http.ng.get($scope, $http, "/api/evaluate/content/keywords", {
        evaluateId:evaluateId
    }, function (res) {
        var seriesData = _.map(res.list, function (v) {
            return {
                name    : v.word,
                value   : v.count
            };
        });
        drawContentKeywords($scope, seriesData);
    });
};
var graphic = function ($scope) {
    return [{
        type: 'group',
        bounding: 'raw',
        left: 380,
        bottom: 5,
        z: 100,
        children: [{
            type: 'text',
            z: 100,
            style: {
                fill: '#999',
                text: '* 该数据来源于' + $scope.appInfo.appName + '，仅供参考',
                fontSize: 50
            }
        }]
    }];
};
//处理echarts渲染前的数据，并最终渲染图形
var dealsData = function ($scope, type) {
    var result = $scope.evaluateTendencyData[type];
    var xAxisData = [];
    var seriesObj = _.map(result, function (v, key) {
        v = _.sortBy(v, 'time');
        xAxisData = _.map(v, function (item) {
            return new Date(item.time).Format("M月d日");
        });
        return {
            name    : key,
            type    : 'line',
            stack   : key,
            smooth  : true,//平滑曲线
            data    : _.map(v, 'count')
        };
    });
    drawTendency($scope, seriesObj, xAxisData);
};

// publishCount:发文量   multiple:综合指数  psi:传播力  mii:影响力   bsi:引导力  tsi:公信力
var drawTendency = function ($scope, seriesObj, xAxisData) {
    var multiple = echarts.init(document.getElementById("trend"));
    multiple.setOption({
        animation: false,
        title: {
            show: false
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            show: false
        },
        grid: {
            left: '0%',
            right: '20',
            top: '10',
            bottom: '20',
            containLabel: true
        },
        toolbox: {
            show: false
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: xAxisData
        },
        yAxis: {
            type: 'value'
        },
        series: seriesObj,
        color: ['#FFA16E', '#6EABFF', '#9B6DFD', '#FFC61F', '#00BD5B', '#1FCEFF', '#EB6DFD', '#F86574','#6174FA', '#00CCAF', '#FB4558', '#0550E4', '#D301BD']
    });
   $scope.arr.push(multiple.getDataURL({
       type: 'png'
   }))
};
var drawContentReadAndLike = function ($scope, seriesData, axisInfo) {
    var myChart = echarts.init(document.getElementById('read-and-like'));
    myChart.setOption({
        animation: false,
        tooltip: {
            formatter: function (data, index) {
                return '平均阅读数：' + (data.value[0] + axisInfo.x) + '<br/>' + '平均点赞数：' +(data.value[1] + axisInfo.y);
            }
        },
        xAxis: {
            name: '平均阅读数',
            nameLocation: 'middle',
            nameGap: 25,
            axisLabel: {
                formatter: function (value, index) {
                    return value + axisInfo.x;
                }
            }
        },
        yAxis: {
            name: '平均点赞数',
            nameLocation: 'middle',
            nameGap: 40,
            axisLabel: {
                formatter: function (value, index) {
                    return value + axisInfo.y;
                }
            }
        },
        series: [{
            type: 'scatter',
            symbolSize: 20,
            data: seriesData
        }],
        color: ['#FFA16E', '#6EABFF', '#9B6DFD', '#FFC61F', '#00BD5B', '#1FCEFF', '#EB6DFD', '#F86574','#6174FA', '#00CCAF', '#FB4558', '#0550E4', '#D301BD']

    });
    window.onresize = myChart.resize;
    $scope.arr.push(myChart.getDataURL({
        type: 'png'
    }))
};
var drawContentKeywords = function ($scope, seriesData) {
    var myCharts = echarts.init(document.getElementById('keyWords'));
    myCharts.setOption({
        animation: false,
        tooltip: {},
        series: [ {
            type: 'wordCloud',
            gridSize: 20,
            sizeRange: [18, 50],
            rotationRange: [-80, 90],   //旋转范围
            shape: 'pentagon',
            drawOutOfBound: true,
            textStyle: {
                normal: {
                    color: function () {
                        return 'rgb(' + [
                            Math.round(Math.random() * 160),
                            Math.round(Math.random() * 160),
                            Math.round(Math.random() * 160)
                        ].join(',') + ')';
                    }
                },
                emphasis: {
                    shadowBlur: 5,
                    shadowColor: '#ccc'
                }
            },
            data: seriesData
        }]
    });
    window.onresize = myCharts.resize;
    setTimeout(function() {
        $scope.arr.push(myCharts.getDataURL({
            type: 'png'
        }))
    },1000)
};
