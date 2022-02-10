package com.istar.mediabroken.repo.account

import com.istar.mediabroken.entity.account.Menu
import com.istar.mediabroken.utils.MongoHolder
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

/**
 * Author: hh
 * Time: 2018/1/26
 */
@Repository
class MenuRepo {
    @Autowired
    MongoHolder mongo;

    List getMenuListByIds(List<String> ids) {
        def collection = mongo.getCollection("menu")
        QueryBuilder queryKey = QueryBuilder.start()
        queryKey.put("_id").in(ids)
        def cursor = collection.find(queryKey.get())
        def menuList = []
        while (cursor.hasNext()) {
            def menu = cursor.next()
            menuList << new Menu(menu)
        }
        return menuList
    }

    List addParentMenuByIds(HashSet<String> parentIds, List<Menu> menuList) {
        def collection = mongo.getCollection("menu")
        QueryBuilder queryKey = QueryBuilder.start()
        queryKey.put("_id").in(parentIds)
        def cursor = collection.find(queryKey.get())
        while (cursor.hasNext()) {
            def menu = cursor.next()
            menuList << new Menu(menu)
        }
        return menuList
    }

}
