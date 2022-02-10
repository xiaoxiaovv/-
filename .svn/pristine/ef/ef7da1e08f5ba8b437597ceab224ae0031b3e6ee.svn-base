var instantNewsMarkCtrl = function ($rootScope, $scope, $http, $state, $stateParams, $sce, $interval, $document) {
    var clipboard   = null;
    $scope.newNews = false;
    $scope.newsDetail = false;
    $scope.nowTime = Date.parse(new Date());
    $scope.markNewsdataListView = false;
    $scope.listFirstNewsTime = $stateParams.firstNewsTime;
    $scope.pno = 1;
    if(markNewNewsNumber) {
        $interval.cancel(markNewNewsNumber);
        markNewNewsNumber = null;
    }
    var markNewNewsNumber = $interval(function () {
        $scope.nowTime = Date.parse(new Date());
        markGetInstantNewsCount($scope, $http)
    }, 30000);
    $rootScope.$on('$stateChangeStart', function (){
        $interval.cancel(markNewNewsNumber);
    });
    bjj.scroll.loadList.init($scope, $http, '.bjj-cont-page', '.instant-body', getLaterReadNews, 30);
    getLaterReadNews($scope, $http);
    $scope.reloadPage = function() {
        $interval.cancel(markNewNewsNumber);
        $state.go("instantNews")
    }
    let markCur = 0;
    $document.unbind("keyup");
    $document.bind("keyup", function(event) {
        if($state.current.name != 'instantNewsMark') {
            return;
        }
        if(event.keyCode == 38) {
            _.forEach($scope.dataList, function(v) {
                v.isHover = false;
            })
            $(".every-instant-news-later").removeClass('hover');
            if (markCur == -1) {
                markCur = $scope.dataList.length - 1;
            }else {
                markCur -= 1;
                if(markCur < -1) {
                    return;
                }
                $(".every-instant-news-later").eq(markCur).addClass('hover');
                $scope.dataList[markCur].isHover = true;
            }
            if (markCur < 0) {
                markCur = $scope.dataList.length - 1;
                $(".every-instant-news-later").eq(markCur).addClass('hover');
                $scope.dataList[markCur].isHover = true;
            }
        }
        if(event.keyCode == 40) {
            _.forEach($scope.dataList, function(v) {
                v.isHover = false;
            })
            $(".every-instant-news-later").removeClass('hover');
            if (markCur == -1){
                markCur = 0;
                $(".every-instant-news-later").eq(markCur).addClass('hover');
                $scope.dataList[markCur].isHover = true;
            }else {
                markCur++;
                if(markCur == $scope.dataList.length) {
                    markCur = 0;
                }
                $(".every-instant-news-later").eq(markCur).addClass('hover');
                $scope.dataList[markCur].isHover = true;
            }
            if (markCur >= $scope.dataList.length){
                markCur = 0;
                $(".every-instant-news-later").eq(markCur).addClass('hover');
                $scope.dataList[markCur].isHover = true;
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
        if(event.keyCode == 88) {
            var myInput = document.getElementById('searchInput');
            if (myInput == document.activeElement) {
                return;
            } else {
                $interval.cancel(markNewNewsNumber);
                $state.go("instantNews", {reload: true});
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
                $state.reload('instantNewsMark');
            }
        }
        if(event.keyCode == 37){
            _.forEach($scope.dataList, function(v,i) {
                if(v.id == $scope.id) {
                    markGetNewsDetail($scope, $http, $sce, $scope.dataList[i-1].id);
                    setTimeout(function(){
                        $scope.id = $scope.dataList[i-1].id;
                        _.forEach($scope.dataList, function(v, i) {
                            v.isHover = false;
                            if(v.id == $scope.id) {
                                $rootScope.writeNewsId($rootScope.user.userName, v.id, 'instantNews');
                                v.isRead  = true;
                                v.isHover = true;
                                markCur   = i;
                            }
                        })
                    },0)
                }
            })
        }
        if(event.keyCode == 39){
            _.forEach($scope.dataList, function(v,i) {
                if(v.id == $scope.id) {
                    markGetNewsDetail($scope, $http, $sce, $scope.dataList[i+1].id);
                    setTimeout(function(){
                        $scope.id = $scope.dataList[i+1].id;
                        _.forEach($scope.dataList, function(v, i) {
                            v.isHover = false;
                            if(v.id == $scope.id) {
                                $rootScope.writeNewsId($rootScope.user.userName, v.id, 'instantNews');
                                v.isRead  = true;
                                v.isHover = true;
                                markCur   = i;
                            }
                        })
                    },0)
                }
            })
        }
    });
    $scope.goNewsRead = function() {
        $interval.cancel(markNewNewsNumber);
        $state.go("instantNews");
    };
    $scope.reloadThisPage = function() {
        $scope.newNews = false;
        $state.reload('instantNewsMark');
    }
    $scope.toNewsCentered = function() {
        $interval.cancel(markNewNewsNumber);
        $state.go("instantNews",{type: '1'});
    };
    $scope.toTitleOnly = function() {
        $interval.cancel(markNewNewsNumber);
        $state.go("instantNews");
        $('.instant-body').addClass('title-only').removeClass("news-centered");
    };
    $scope.searchNews = function($event) {
        if($event) {
            if($event.keyCode == 13) {
                $scope.pno = 1;
                $scope.queryId = '';
                getLaterReadNews($scope, $http);
            }
        }else {
            $scope.pno = 1;
            $scope.queryId = '';
            getLaterReadNews($scope, $http);
        }
    }
    $scope.LaterReadMark = function($event, news) {
        if(news.isMarked) {
            markCancelNewsMark($scope, $http, $event, news.id, function() {
                news.isMarked = !news.isMarked;
            })
        }else {
            markAddNewsMark($scope, $http, $event, news.id, function() {
                news.isMarked = !news.isMarked;
            })
        }
        markGetNewsDetail($scope, $http, $sce, news.id);
    }
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
    $scope.selectNews = function($event, index) {
        markCur = index;
        $(".every-instant-news-later").removeClass('hover');
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
        markGetNewsDetail($scope, $http, $sce, id);
        $scope.id = id;
    };
    $scope.closeOverlay = function() {
        $scope.newsDetail = false;
    };
    $scope.leftNews = function() {
        _.forEach($scope.dataList, function(v,i) {
            if(v.id == $scope.id) {
                markGetNewsDetail($scope, $http, $sce, $scope.dataList[i-1].id);
                setTimeout(function(){
                    $scope.id = $scope.dataList[i-1].id;
                    _.forEach($scope.dataList, function(v) {
                        if(v.id == $scope.id) {
                            $rootScope.writeNewsId($rootScope.user.userName, v.id, 'instantNews');
                            v.isRead = true;
                        }
                    })
                },0)
            }
        })
    }
    $scope.rightNews = function() {
        _.forEach($scope.dataList, function(v,i) {
            if(v.id == $scope.id) {
                markGetNewsDetail($scope, $http, $sce, $scope.dataList[i+1].id);
                setTimeout(function(){
                    $scope.id = $scope.dataList[i+1].id;
                    _.forEach($scope.dataList, function(v, i) {
                        v.isHover = false;
                        if(v.id == $scope.id) {
                            $rootScope.writeNewsId($rootScope.user.userName, v.id, 'instantNews');
                            v.isRead  = true;
                            v.isHover = true;
                            markCur   = i;
                        }
                    })
                },0)
            }
        })
    }
    $scope.modifyMark = function($event) {
        var news = this.news;
        if(news.isMarked) {
            markCancelNewsMark($scope, $http, $event, news.id, function() {
                news.isMarked = !news.isMarked;
                _.forEach($scope.dataList, function(v, i) {
                    v.isHover = false;
                    if(v.id == news.id) {
                        v.isMarked = !v.isMarked;
                        v.isHover  = true;
                        markCur    = i;
                    }
                })
            })
        }else {
            markAddNewsMark($scope, $http, $event, news.id, function() {
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

let getLaterReadNews = function ($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/capture/instantNewsMark', {
        pageNo      : $scope.pno,
        pageSize    : $scope.psize,
        queryName   : $scope.markQueryName ? $scope.markQueryName : ""
    }, function(res) {
        $scope.readNewsId($scope.user.userName, 'instantNews', res.list);
        if(res.list.length > 0) {
            res.list[0].isHover = true;
        }
        bjj.scroll.loadList.callback($scope, res.list);
        if(res.msg) {
            $scope.markNewsdataListView = true;
            $scope.markListEmptyMsg = res.msg;
        }else {
            $scope.markNewsdataListView = false;
        }
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    }, 'instantNewsMarkLoading')
};
let markGetInstantNewsCount = function($scope, $http) {
    bjj.http.ng.get($scope, $http, '/api/capture/instantNews/count', {
        startTime   : $scope.listFirstNewsTime,//列表第一条时间
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
            localStorage.setItem('newNewsTime', $scope.listFirstNewsTime);
        }
    });
}
let markGetNewsDetail = function($scope, $http, $sce, id) {
    bjj.http.ng.get($scope, $http, '/api/capture/newsDetail/' + id, {}, function (res) {
        $scope.contentAbstract = res.msg.contentAbstract;
        $scope.news = res.msg;
        $scope.news.content = $sce.trustAsHtml($scope.news.content);
    });
}
let markAddNewsMark = function($scope, $http, $event, id, callback) {
    bjj.http.ng.post($scope, $http, '/api/capture/instantNewsMark/'+ id, {}, function(res){
        callback();
        $($event.target).addClass('later-read');
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    })
}
let markCancelNewsMark = function($scope, $http, $event, id, callback) {
    bjj.http.ng.del($scope, $http, '/api/capture/instantNewsMark/'+ id, {}, function(res){
        callback();
        $($event.target).removeClass('later-read');
    }, function(res) {
        bjj.dialog.alert('danger', res.msg);
    })
}