package com.istar.mediabroken.repo.account

import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * Author: hh
 * Time: 2018/1/22
 */
@Repository
class MenuVsPrivilegeRepo {
    @Autowired
    MongoHolder mongo;

    List getMenuByPrivilege(List<String> privilegeIds) {
        def collection = mongo.getCollection("menuVsPrivilege")
        QueryBuilder queryKey = QueryBuilder.start()
        queryKey.put('privId').in(privilegeIds)
        def cursor = collection.find(queryKey.get())
        def result = []
        while (cursor.hasNext()) {
            def res = cursor.next()
            result << res.menuId
        }
        return result
    }
}
