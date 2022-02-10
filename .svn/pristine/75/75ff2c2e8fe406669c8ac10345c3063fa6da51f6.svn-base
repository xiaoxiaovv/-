package com.istar.mediabroken.utils

/**
 * Created by MCYarn on 2017/6/16.
 */
class SolrPaging<T> extends Paging<T> {

    SolrPaging( int pageNo, int limit, long total ) {
        super( pageNo, limit, total )
    }

    SolrPaging( int pageNo, int limit ) {
        super( pageNo, limit )
    }
    String CurrentCursor
    int limit

    SolrPaging(int pageNo, int limit, long total, String currentCursor ) {
        super(pageNo, limit, total)
        CurrentCursor = currentCursor
    }
    SolrPaging(int pageNo, int limit, String currentCursor) {
        super(pageNo, limit)
        CurrentCursor = currentCursor
    }
}
