package com.istar.mediabroken.service.compile

import com.istar.mediabroken.entity.compile.Style
import com.istar.mediabroken.repo.compile.StyleRepo
import com.istar.mediabroken.service.ShareChannelService
import com.istar.mediabroken.utils.StringUtils
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author: zc
 * Time: 2017/8/3
 */
@Service
@Slf4j
class StyleService {

    @Value('${image.upload.path}')
    String UPLOAD_PATH

    @Autowired
    StyleRepo styleRepo
    @Autowired
    private ShareChannelService shareChannelSrv

    Map addUserStyle(Long userId, String content) {
        def style = new Style(
            userId      : userId,
            content     : content,
            updateTime  : new Date(),
            createTime  : new Date(),
        )
        styleRepo.addUserStyle(style)
        return apiResult([msg : 'success'])
    }

    Map getUserStyles(Long userId) {

        def stylesCommon = styleRepo.getUserStyles(0L)
        def stylesCustom = styleRepo.getUserStyles(userId)

        return apiResult([status: HttpStatus.SC_OK, msg: stylesCommon + stylesCustom])
    }

    void removeStyle(long userId, HttpServletRequest request, String styleId) {

        if(userId == 0) return

        Style style = styleRepo.getUserStyleById(userId, styleId)
        if (style && style.content) {
            def imgUrls = StringUtils.extractImgUrl(style.content)

            //移除图片
            imgUrls?.each { imgUrl ->
                String imgPath = shareChannelSrv.picLocalUrl(imgUrl as String)
                File file = new File(UPLOAD_PATH + imgPath);
                // 路径为文件且不为空则进行删除
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            }

            //删除样式
            styleRepo.removeStyle(userId, styleId)
        }
    }
}
