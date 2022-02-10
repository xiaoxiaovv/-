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
class RoleVsPrivilegeRepo {
    @Autowired
    MongoHolder mongo;

    List getPrivilegeByRoleId(String roleId) {
        def collection = mongo.getCollection("roleVsPrivilege")
        QueryBuilder queryKey = QueryBuilder.start()
        queryKey.put('roleId').is(roleId)
        def cursor = collection.find(queryKey.get())
        def result = []
        while (cursor.hasNext()) {
            def res = cursor.next()
            result << res.privId
        }
        return result
    }
}
