var instantNewsCtrl = function ($rootScope, $scope, $http, $state, $stateParams, $sce, $interval, $document) {
    var clipboard   = null;
    $rootScope.minBar           = true;
    $scope.newNews              = false;
    $scope.newsDetail           = false;
    $scope.laterReadNumberShow  = false;
    $scope.allNewsdataListView  = false;
    $scope.nowTime = Date.parse(new Date());
    $scope.beforeNewsTime = localStorage.getItem('newNewsTime');
    if(newNewsNumber) {
        $interval.cancel(newNewsNumber);
        newNewsNumber = null;
    }
    var newNewsNumber = $interval(function () {
        $scope.nowTime = Date.parse(new Date());
        getInstantNewsCount($scope, $http)
    }, 30000);
    $rootScope.$on('$stateChangeStart', function (){
        $interval.cancel(newNewsNumber);
    });
    if(!$scope.beforeNewsTime) {
        $scope.beforeNewsTime = $scope.nowTime;
    }
    $scope.laterReadNumber = 0;
    var audio = $("#audio")[0];
    if($stateParams.type) {
        $scope.type = $stateParams.type;
    }
    if($scope.type == 1) {
        $('.instant-body').addClass('news-centered').removeClass("title-only");
    }else {
        $('.instant-body').removeClass('news-centered').addClass("title-only");
    }
    $scope.pno = 1;
    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.instant-body', getInstantNewsData, 20);
    getInstantNewsData($scope, $http);
    $scope.reloadPage = function() {
        $scope.newNews = false;
        $state.reload('instantNews');
        $interval.cancel(newNewsNumber);
    }
    let cur = 0;
    $document.unbind("keyup");
    $document.bind("keyup", function(event) {
        if($state.current.name != 'instantNews') {
            return;
        }
        if(event.keyCode == 38) {
            _.forEach($scope.dataList, function(v) {
                v.isHover = false;
            })
            $(".every-instant-news").removeClass('hover');
            if (cur == -1) {
                cur = $scope.dataList.length - 1;
            }else {
                cur -= 1;
                if(cur < -1) {
                    return;
                }
                $(".every-instant-news").eq(cur).addClass('hover');
                $scope.dataList[cur].isHover = true;
            }
            if (cur < 0) {
                cur = $scope.dataList.length - 1;
                $(".every-instant-news").eq(cur).addClass('hover');
                $scope.dataList[cur].isHover = true;
            }
        }
        if(event.keyCode == 40) {
            _.forEach($scope.dataList, function(v) {
                v.isHover = false;
            })
            $(".every-instant-news").removeClass('hover');
            if (cur == -1){
                cur = 0;
                $(".every-instant-news").eq(cur).addClass('hover');
                $scope.dataList[cur].isHover = true;
            }else {
                cur++;
                if(cur == $scope.dataList.length) {
                    cur = 0;
                }
                $(".every-instant-news").eq(cur).addClass('hover');
                $scope.dataList[cur].isHover = true;
            }
            if (cur >= $scope.dataList.length){
                cur = 0;
                $(".every-instant-news").eq(cur).addClass('hover');
                $scope.dataList[cur].isHover = true;
            }
        }
        if(event.keyCode == 13) {
            var myInput = document.getElementById('searchInput');
            if (myInput == document.activeElement) {
                return;
            } else {
                _.forEach($scope.dataList, function(v,i) {
                    if(v.isHover) {
                        $(".title-"+ v.id).click();
                    }
                })
            }
        }
        if(event.keyCode == 70) {//复制
            var myInput = document.getElementById('searchInput');
            if (myInput == document.activeElement) {
                return;
            } else {
                _.forEach($scope.dataList, function(v,i) {
                    if(v.isHover) {
                        $(".copy-"+ v.id).click();
                    }
                })
            }
        }
        if(event.keyCode == 89) {//原文链接
            var myInput = document.getElementById('searchInput');
            if (myInput == document.activeElement) {
                return;
            } else {
                _.forEach($scope.dataList, function(v,i) {
                    if(v.isHover) {
                        window.open($scope.dataList[i].url);
                    }
                })
            }
        }
        if(event.keyCode == 67) {//标记
            var myInput = document.getElementById('searchInput');
            if (myInput == document.activeElement) {
                return;
            } else {
                _.forEach($scope.dataList, function(v,i) {
                    if(v.isHover) {
                        $(".unread-"+ v.id).click();
                    }
                })
            }
        }
        if(event.keyCode == 88) {//x
            var myInput = document.getElementById('searchInput');
            if (myInput == document.activeElement) {
                return;
            } else {
                $interval.cancel(newNewsNumber);
                $state.go("instantNewsMark", {reload: true, firstNewsTime: $scope.firstNewsTime});
            }
        }
        if(event.keyCode == 81) {
            var myInput = document.getElementById('searchInput');
            if (myInput == document.activeElement) {
                return;
            } else {
                $(".close-tools").click();
            }
        }
        if(event.keyCode == 83) {
            var myInput = document.getElementById('searchInput');
            if (myInput == document.activeElement) {
                return;
            } else {
                $state.reload('instantNews');
            }
        }
        if(event.keyCode == 37){
            _.forEach($scope.dataList, function(v,i) {
                if(v.id == $scope.id) {
                    getNewsDetail($scope, $http, $sce, $scope.dataList[i-1].id);
                    setTimeout(function(){
                        $scope.id = $scope.dataList[i-1].id;
                        _.forEach($scope.dataList, function(v, i) {
                            v.isHover = false;
                            if(v.id == $scope.id) {
                                $rootScope.writeNewsId($rootScope.user.userName, v.id, 'instantNews');
                                v.isRead  = true;
                                v.isHover = true;
                                cur       = i;
                            }
                        })
                    },0)
                }
            })
        }
        if(event.keyCode == 39){
            _.forEach($scope.dataList, function(v,i) {
                if(v.id == $scope.id) {
                    getNewsDetail($scope, $http, $sce, $scope.dataList[i+1].id);
                    setTimeout(function(){
                        $scope.id = $scope.dataList[i+1].id;
                        _.forEach($scope.dataList, function(v, i) {
                            v.isHover = false;
                            if(v.id == $scope.id) {
                                $rootScope.writeNewsId($rootScope.user.userName, v.id, 'instantNews');
                                v.isRead  = true;
                                v.isHover = true;
                                cur       = i;
                            }
                        })
                    },0)
                }
            })
        }
    });

    $scope.toNewsCentered = function() {
        $interval.cancel(newNewsNumber);
        $state.go("instantNews",{'type': 1});
        $('.instant-body').addClass('news-centered').removeClass("title-only");
    };
    $scope.toTitleOnly = function() {
        $interval.cancel(newNewsNumber);
        $state.go("instantNews",{'type': 0});
        $('.instant-body').addClass('title-only').removeClass("news-centered");
    };
    $scope.selectNews = function($event, id) {
        _.forEach($scope.dataList, function(v, i) {
            if(v.id == id) {
                cur = i;
            }
        })
        $(".every-instant-news").removeClass('hover');
        _.forEach($scope.dataList, function(v) {
            v.isHover = false;
        })
        $($event.target).parents("li").addClass("hover").siblings().removeClass("hover");
    }
    $scope.goDetail = function($event, id) {
        _.forEach($scope.dataList, function(v, i) {
            v.isHover = false;
            if(v.id == id) {
                v.isHover = true;
                cur = i;
            }
        })
        $scope.newsDetail = true;
        $rootScope.writeNewsId($rootScope.user.userName, id, 'instantNews');
        $($event.target).addClass('read');
        getNewsDetail($scope, $http, $sce, id);
        $scope.id = id;
    };
    $scope.copyInstantNews = function () {
        if(clipboard != undefined && clipboard != null) clipboard.destroy();
        var that = this;
        clipboard = new ClipboardJS('.copy', {
            text: function () {
                //todo 需要复制到剪贴板的内容 return即可
                return that.news.title + " " + that.news.siteName + " " + that.news.contentAbstract + " " + that.news.url;
            }
        }).on('success', function(e) {
            bjj.dialog.alert('success', '复制成功！');
            e.clearSelection();
        });
    };
    /*搜索*/
    $scope.searchNews = function($event) {
        if($event) {
            if($event.keyCode == 13) {
                $scope.pno = 1;
                $scope.queryId = '';
                getInstantNewsData($scope, $http);
            }
        }else {
            $scope.pno = 1;
            $scope.queryId = '';
            getInstantNewsData($scope, $http);
        }
    }
    /*标记已读*/
    $scope.addLaterRead = function($event, news) {
        if(news.isMarked) {
            cancelNewsMark($scope, $http, $event, news.id, function() {
                if($scope.laterReadNumber >= 1) {
                    $scope.laterReadNumber--;
                }
                if($scope.laterReadNumber == 0) {
                    $scope.laterReadNumberShow = false
                }
                news.isMarked = !news.isMarked;
                getNewsDetail($scope, $http, $sce, news.id);
            })
        }else {
            addNewsMark($scope, $http, $event, news.id, function() {
                $scope.laterReadNumberShow = true;
                $scope.laterReadNumber++;
                news.isMarked = !news.isMarked;
                getNewsDetail($scope, $http, $sce, news.id);
            })
        }
    }
    /*去标记阅读页面*/
    $scope.goLaterRead = function() {
        $interval.cancel(newNewsNumber);
        $scope.laterReadNumber = false;
        $state.go("instantNewsMark", {firstNewsTime: $scope.firstNewsTime});
    }
    $scope.closeOverlay = function() {
        $scope.newsDetail = false;
    };
    /*新闻翻页*/
    $scope.leftNews = function() {
        _.forEach($scope.dataList, function(v,i) {
            if(v.id == $scope.id) {
                getNewsDetail($scope, $http, $sce, $scope.dataList[i-1].id);
                setTimeout(function(){
                    $scope.id = $scope.dataList[i-1].id;
                    _.forEach($scope.dataList, function(v, i) {
                        v.isHover = false;
                        if(v.id == $scope.id) {
                            $rootScope.writeNewsId($rootScope.user.userName, v.id, 'instantNews');
                            v.isRead  = true;
                            v.isHover = true;
                            cur       = i;
                        }
                    })
                },0)
            }
        })
    }
    $scope.rightNews = function() {
        _.forEach($scope.dataList, function(v,i) {
            if(v.id == $scope.id) {
                getNewsDetail($scope, $http, $sce, $scope.dataList[i+1].id);
                setTimeout(function(){
                    $scope.id = $scope.dataList[i+1].id;
                    _.forEach($scope.dataList, function(v, i) {
                        v.isHover = false;
                        if(v.id == $scope.id) {
                            $rootScope.writeNewsId($rootScope.user.userName, v.id, 'instantNews');
                            v.isRead  = true;
                            v.isHover = true;
                            cur       = i;
                        }
                    })
                },0)
            }
        })
    }
    $scope.modifyMark = function($event) {
        var news = this.news;
        if(news.isMarked) {
            cancelNewsMark($scope, $http, $event, news.id, function() {
                if($scope.laterReadNumber >= 1) {
                    $scope.laterReadNumber--;
                }
                if($scope.laterReadNumber == 0) {
                    $scope.laterReadNumberShow = false
                }
                news.isMarked = !news.isMarked;
                _.forEach($scope.dataList, function(v) {
                    if(v.id == news.id) {
                        v.isMarked = !v.isMarked;
                    }
                })
            })
        }else {
            addNewsMark($scope, $http, $event, news.id, function() {
                $scope.laterReadNumberShow = true;
                $scope.laterReadNumber++;
                news.isMarked = !news.isMarked;
                _.forEach($scope.dataList, function(v) {
                    if(v.id == news.id) {
                        v.isMarked = !v.isMarked;
                    }
                })
            })
        }
    }
};

let getInstantNewsData = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/capture/instantNews', {
        pageNo      : $scope.pno,
        pageSize    : $scope.psize,
        startTime   : $scope.nowTime - 8*60*60*1000,
        endTime     : $scope.nowTime,
        queryId     : $scope.queryId ? $scope.queryId : "",
        queryName   : $scope.queryName ? $scope.queryName : ""
    }, function (res) {
        $scope.queryId = res.queryId;
        if(res.list.length != 0 && $scope.pno == 1 && ($scope.queryName == '' || $scope.queryName == undefined)) {
            $scope.firstNewsTime = res.list[0].captureTime;
        }
        $scope.readNewsId($scope.user.userName, 'instantNews', res.list);
        bjj.scroll.loadList.callback($scope, res.list);
        transformInstantNewsList2JsonArray($scope);
        if(res.msg) {
            $scope.allNewsdataListView  = true;
            $scope.listEmptyMsg = res.msg;
        }else {
            $scope.allNewsdataListView  = false;
        }
    }, function (res) {
        //todo 请求失败时的异常处理
    }, 'instantNewsLoading');
};
/**
 * 将数据list 转化成时间段间隔的jsonArray结构
 */
let transformInstantNewsList2JsonArray = function ($scope) {

    var timeSpace = 1800 * 1000;//半小时

    let getAlias = function (time) {
        var alias = new Date(time).Format('hh:mm') + ' - ' + new Date(time + timeSpace).Format('hh:mm')
        if((time + timeSpace) > new Date().getTime()){
            alias = "最新";
        }
        return alias;
    };

    var list = [];
    _.each(_.groupBy($scope.dataList, function(i) {
        return parseInt(i.captureTime / timeSpace);
    }), function (value, key) {
        list.push({
            "alias" : getAlias(parseInt(key * timeSpace)),
            "time"  : key,
            "list"  : value
        })
    });
    $scope.list = _.reverse(list);
    _.forEach($scope.list, function(v,i) {
        if(i == 0) {
            v.list[0].isHover = true;
        }
    })
};
let getInstantNewsCount = function($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/capture/instantNews/count', {
        startTime   : $scope.firstNewsTime,//列表第一条时间
        endTime     : $scope.nowTime//现在
    }, function (res) {
        if($scope.count == undefined && res.count > 0) {
            audio.play();
        }
        if(res.count > 0) {
            if(res.count > $scope.count) {
                audio.play();
            }
            $scope.count = res.count;
            $scope.newNews = true;
            localStorage.setItem('newNewsTime', $scope.firstNewsTime);
        }
    });
}
let getNewsDetail = function($scope, $http, $sce, id) {
     bjj.http.ng.get($scope, $http, '/api/capture/newsDetail/' + id, {}, function (res) {
         $scope.contentAbstract = res.msg.contentAbstract;
         $scope.news = res.msg;
         $scope.news.content = $sce.trustAsHtml($scope.news.content);
     });
 }
let addNewsMark = function($scope, $http, $event, id, callback) {
    bjj.http.ng.post($scope, $http, '/api/capture/instantNewsMark/'+ id, {}, function(res){
        callback();
        $($event.target).addClass('later-read');
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    })
}
let cancelNewsMark = function($scope, $http, $event, id, callback) {
    bjj.http.ng.del($scope, $http, '/api/capture/instantNewsMark/'+ id, {}, function(res){
        callback();
        $($event.target).removeClass('later-read');
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    })
}