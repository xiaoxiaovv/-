package com.istar.mediabroken.repo.account

import com.istar.mediabroken.entity.account.Role
import com.istar.mediabroken.entity.account.UserRole
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author: Luda
 * Time: 2017/7/25
 */
@Repository
class RoleRepo {
    @Autowired
    MongoHolder mongo;

    UserRole getUserRole(Long userId) {
        def collection = mongo.getCollection("userRole")
        QueryBuilder queryKey = QueryBuilder.start()
        queryKey.put('userId').is(userId)
        def result = collection.findOne(queryKey.get())
        if (result) {
            return new UserRole(result)
        } else {
            return null
        }
    }

    boolean addUserRole(UserRole userRole) {
        def collection = mongo.getCollection("userRole")
        collection.insert(toObj(userRole.toMap()))
        return true
    }

    Role getRoleByAgentIdAndRoleName(String agentId, String roleName) {
        def collection = mongo.getCollection("role")
        def result = collection.findOne(toObj([agentId: agentId, roleName: roleName]))
        if(result){
            return new Role(result)
        }else {
            return null
        }
    }
    Role getRoleByRoleId(String roleId){
        def collection = mongo.getCollection("role")
        def result = collection.findOne(toObj([_id:roleId]))
        if(result){
            return  new Role(result)
        }else {
            return  null
        }

    }
}
