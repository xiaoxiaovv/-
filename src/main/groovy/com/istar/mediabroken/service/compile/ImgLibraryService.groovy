package com.istar.mediabroken.service.compile

import com.istar.mediabroken.entity.compile.ImgLibrary
import com.istar.mediabroken.repo.compile.ImgLibraryRepo
import com.istar.mediabroken.service.ShareChannelService
import com.istar.mediabroken.utils.StringUtils
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * 一键排版相关
 * Author: zc
 * Time: 20180704
 */
@Service
@Slf4j
class ImgLibraryService {
    @Value('${image.upload.path}')
    String UPLOAD_PATH
    @Autowired
    ImgLibraryRepo imgLibraryRepo
    @Autowired
    private ShareChannelService shareChannelSrv

    List getUserImgs(long userId, boolean mina, String imgGroupId, int pageNo, int pageSize, String type) {
        def result = []
        if(type.equals("img")) {
            result = imgLibraryRepo.getUserImgs(userId, mina, imgGroupId, pageNo, pageSize)
        } else {
            result = imgLibraryRepo.getUserVideos(userId, pageNo, pageSize)
        }

        return result
    }

    List getMaterialState(long userId, String ids) {
        def result = []
        result = imgLibraryRepo.getUserVideos(userId, ids)
        return result
    }

    boolean addUserImg(Long userId, String imgUrl, boolean mina) {
        try {
            def img = new ImgLibrary(
                    _id: UUID.randomUUID().toString(),
                    userId: userId,
                    imgGroupId: "0",
                    mina: mina,
                    imgUrl: imgUrl,
                    type: "img",
                    updateTime: new Date(),
                    createTime: new Date()
            )
            imgLibraryRepo.addUserImg(img)
            return true
        } catch (Exception e) {
            return false
        }
    }

    boolean removeUserImg(Long userId, String imgId) {
        if (userId == 0) return false
        try {
            ImgLibrary imgLibrary = imgLibraryRepo.getUserImgById(userId, imgId)
            String imgPath = shareChannelSrv.picLocalUrl(imgLibrary.imgUrl as String)
            File file = new File(imgPath);
            if (file){
                file.delete()
            }
            //删除素材
            imgLibraryRepo.removeImg(userId, imgId)
            return true
        } catch (Exception e) {
            return false
        }
    }

    boolean removeUserMaterial(Long userId, String imgId) {
        if (userId == 0) return false
        imgLibraryRepo.removeImg(userId, imgId)
        return true
    }


}
