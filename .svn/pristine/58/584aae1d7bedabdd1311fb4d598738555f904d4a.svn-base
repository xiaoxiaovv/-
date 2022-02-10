package com.istar.mediabroken.repo.account

import com.istar.mediabroken.entity.account.AccountVsRole
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author: hh
 * Time: 2018/1/22
 */
@Repository
class AccountVsRoleRepo {
    @Autowired
    MongoHolder mongo;

    AccountVsRole getRoleIdByUserId(Long userId) {
        def collection = mongo.getCollection("accountVsRole")
        QueryBuilder queryKey = QueryBuilder.start()
        queryKey.put('userId').is(userId)
        def result = collection.findOne(queryKey.get())
        if (result) {
            return new AccountVsRole(result)
        } else {
            return null
        }
    }
    AccountVsRole saveAccountVsRole(AccountVsRole accountVsRole){
        def collection = mongo.getCollection("accountVsRole")
        def dataMap = accountVsRole.toMap()
        collection.insert(toObj(dataMap))
        def findOne = collection.findOne(toObj([_id: accountVsRole.getId()]))
        if(findOne){
            return new AccountVsRole(findOne)
        }else {
            return null
        }
    }
}
