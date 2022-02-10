package com.istar.mediabroken.utils

class Paging<T> {
    Paging(int pageNo, int limit, long total) {
        this.pageNo = pageNo
        this.limit = limit
        this.total = total
    }

    Paging(int pageNo, int limit) {
        this.pageNo = pageNo
        this.limit = limit
        this.total = 0
    }

    int pageNo
    int limit
    long total

    int getOffset() {
        return (pageNo - 1) * limit
    }

    List<T> list = new ArrayList<T>(limit)

    long getTotalPage() {
        def totalPage = (total / limit) as long
        if (total % limit > 0) {
            totalPage = totalPage + 1
        }
        return totalPage
    }
}
