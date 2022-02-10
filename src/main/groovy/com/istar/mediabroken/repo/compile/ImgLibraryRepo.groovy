package com.istar.mediabroken.repo.compile

import com.istar.mediabroken.entity.compile.ImgLibrary
import com.istar.mediabroken.utils.MongoHolder
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * Author: zc
 * Time: 2018/7/4
 */
@Repository
@Slf4j
class ImgLibraryRepo {
    @Autowired
    MongoHolder mongo

    List<ImgLibrary> getUserImgs(long userId, boolean mina, String imgGroupId, int pageNo, int pageSize) {
        def collection = mongo.getCollection("materialLibrary")
        def query = [userId: userId, imgGroupId: imgGroupId, type: "img"]
        if (mina){
            query = [userId: userId, mina: mina, imgGroupId: imgGroupId, type: "img"]
        }
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1])).skip((pageNo - 1)* pageSize).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def img = cursor.next()
            result << new ImgLibrary(img)
        }
        cursor.close()
        return result
    }

    List<ImgLibrary> getUserVideos(long userId, int pageNo, int pageSize) {
        def collection = mongo.getCollection("materialLibrary")
        def query = [userId: userId, type: "video"]
        def cursor = collection.find(toObj(query)).sort(toObj([createTime: -1])).skip((pageNo - 1)* pageSize).limit(pageSize)
        def result = []
        while (cursor.hasNext()) {
            def video = cursor.next()
            result << new ImgLibrary(video)
        }
        cursor.close()
        return result
    }

    List<ImgLibrary> getUserVideos(long userId, String ids) {
        def collection = mongo.getCollection("materialLibrary")
        String[] _ids = ids.split(",")
        def result = []
        List idList = Arrays.asList(_ids)
        if(!_ids) {
            return result
        }
        def cursor = collection.find(toObj([userId: userId, "_id": [$in: idList]]))
        while (cursor.hasNext()) {
            def video = cursor.next()
            result << new ImgLibrary(video)
        }
        cursor.close()
        return result
    }

    String addUserImg(ImgLibrary imgLibrary) {
        def collection = mongo.getCollection("materialLibrary")
        collection.insert(toObj(imgLibrary.toMap()))
        return imgLibrary.id
    }

    ImgLibrary getUserImgById(long userId, String imgId) {
        def collection = mongo.getCollection("materialLibrary")
        def obj = collection.findOne(toObj([userId: userId, _id: imgId]))
        return obj
    }

    void removeImg(long userId, String imgId) {
        def collection = mongo.getCollection("materialLibrary")
        collection.remove(toObj([userId: userId, _id: imgId]))
    }
}
