package com.istar.mediabroken.service.compile

import com.istar.mediabroken.entity.LoginUser
import com.istar.mediabroken.entity.PushTypeEnum
import com.istar.mediabroken.entity.account.Account
import com.istar.mediabroken.entity.capture.NewsOperation
import com.istar.mediabroken.entity.compile.ArticlePush
import com.istar.mediabroken.entity.compile.Material
import com.istar.mediabroken.entity.compile.OrgTag
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.account.TeamRepo
import com.istar.mediabroken.repo.capture.NewsRepo
import com.istar.mediabroken.repo.compile.MaterialRepo
import com.istar.mediabroken.repo.compile.TagRepo
import com.istar.mediabroken.repo.favoriteGroup.FavoriteGroupRepo
import com.istar.mediabroken.service.ShareChannelService
import com.istar.mediabroken.utils.ExportExcel
import com.istar.mediabroken.utils.ImageInfo
import com.istar.mediabroken.utils.Md5Util
import com.istar.mediabroken.utils.StringUtils
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.http.HttpStatus
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import java.text.SimpleDateFormat

import static com.istar.mediabroken.api.ApiResult.apiResult

/**
 * Author: Luda
 * Time: 2017/8/1
 */
@Service
@Slf4j
class MaterialService {
    @Autowired
    MaterialRepo materialRepo
    @Autowired
    private ShareChannelService shareChannelSrv
    @Autowired
    AccountRepo accountRepo
    @Autowired
    FavoriteGroupRepo favoriteGroupRepo
    @Autowired
    NewsRepo newsRepo
    @Autowired
    TeamRepo teamRepo
    @Autowired
    TagRepo tagRepo

    private List getTags(List tagIds){
        def tags = [];
        List<OrgTag> orgTagList = tagRepo.getTagListByIds(tagIds)
        for (int i = 0; i < orgTagList.size(); i++) {
            tags.add(orgTagList.get(i).toMap())
        }
        return tags
    }
    Map addUserMaterial(HttpServletRequest request, long userId, String orgId, String title,
                        String author, String content, String contentAbstract,
                        String keywords, String classification, String source,
                        String picUrl, int type, List tagIds, List tagList) {

        def tags = this.getTags(tagIds)
        content = shareChannelSrv.dealBase64Image2Local(request, content)
        Material material = new Material([
                id             : Md5Util.md5(UUID.randomUUID().toString()),
                title          : title,
                author         : author,
                content        : content,
                contentAbstract: contentAbstract,
                keywords       : keywords,
                classification : classification,
                source         : source,
                picUrl         : picUrl,
                type           : type,
                userId         : userId,
                orgId          : orgId,
                tags           : tags,
                customTags     : tagList,
                createTime     : new Date(),
                updateTime     : new Date()
        ])
        String id = materialRepo.addMaterial(material)
        return apiResult(status: HttpStatus.SC_OK, msg: "success", "id": id)
    }

    Map modifyUserMaterial(HttpServletRequest request, long userId, String id, String orgId,
                           String title, String author, String content,
                           String contentAbstract, String keywords, String classification,
                           String source, String picUrl, int type ,List tagIds, List tagsList) {
        def tags = this.getTags(tagIds)
        content = shareChannelSrv.dealBase64Image2Local(request, content)
        Material material = new Material([
                id             : id,
                userId         : userId,
                title          : title,
                author         : author,
                content        : content,
                contentAbstract: contentAbstract,
                keywords       : keywords,
                classification : classification,
                source         : source,
                picUrl         : picUrl,
                type           : type,
                userId         : userId,
                orgId          : orgId,
                tags           : tags,
                customTags     : tagsList,
                updateTime     : new Date()
        ])
        boolean result = materialRepo.modifyMaterial(material)
        def material1 = materialRepo.getUserMaterial(userId, id)

        if (material1.articleId) {
            //发布h5
            def push = materialRepo.getUserArticlePush(userId, material1.articleId)
            if (push || material1.isReleased) {
                materialRepo.updateArticleOperationMaterial(userId, material1.toMap(), MaterialRepo.ARTICLE_TYPE_RELEASE, material1.articleId)
            }
            //预览
            if (push) {
                materialRepo.updateArticleOperationMaterial(userId, material1.toMap(), MaterialRepo.ARTICLE_TYPE_PREVIEW, material1.articleId)
            }
        }

        if (result) {
            return apiResult(status: HttpStatus.SC_OK, msg: "编辑成功")
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "编辑失败")
        }
    }

    Map getUserMaterial(String id) {
        Material material = materialRepo.getUserMaterial(id)
        if (!material) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "没有找到对应的素材信息"])
        }
        material.classification = []//classification字段意义改变，返回无意义
        return apiResult([status: HttpStatus.SC_OK, msg: material])
    }

    def getMaterialDetailByDate(long userId, String id, Date time) {
        def material = materialRepo.getUserMaterialByTime(id, time)
    }

    def MaterialPreview(LoginUser user, String id) {
        Material material = materialRepo.getUserMaterial(user.userId, id)
        if (!material) {
            return null
        }
        def type = materialRepo.getArticleOperationByIdAndType(user.userId, materialRepo.ARTICLE_TYPE_PREVIEW, material.id)
        if (!type) {
            def article = [
                    material     : material.toMap(),
                    userId       : user.userId,
                    orgId        : user.orgId,
                    updateTime   : new Date(),
                    createTime   : new Date(),
                    operationType: MaterialRepo.ARTICLE_TYPE_PREVIEW  // 1文章推送 2 文章同步 3 H5发布 4预览
            ]
            def operation1 = materialRepo.addArticleOperation(article)
            materialRepo.modifyMaterialPush(user.userId, material, operation1._id)
            return operation1
        } else if (type) {
            def material1 = materialRepo.removeArticleOperation(user.userId, type.id)//true 没删除
            if (material1) {
                return null
            }
            def article = [
                    material     : material.toMap(),
                    userId       : user.userId,
                    orgId        : user.orgId,
                    updateTime   : new Date(),
                    createTime   : type.getCreateTime(),
                    operationType: MaterialRepo.ARTICLE_TYPE_PREVIEW  // 1文章推送 2 文章同步 3 H5发布 4预览
            ]
            def operation = materialRepo.addArticleOperation(article)
            materialRepo.modifyMaterialPush(user.userId, material, operation._id)
            return operation
        }
    }

    void updateMaterialStatus2Published(long userId, String id, int publish) {
        boolean result = materialRepo.modifyMaterialStatus(userId, id, publish)
    }

    Map getUserMaterials(long userId, String queryKeyWords, int isPublished, int isReleased, int pageSize, int pageNo) {
        List<Material> result = materialRepo.getUserMaterials(userId, queryKeyWords, isPublished, isReleased, pageSize, pageNo)
        List list = []
        result.each { elem ->

            Map map = [:]
            map.put("id", elem.id)
            map.put("title", elem.title)
            map.put("author", elem.author)
            map.put("content", elem.content)
            map.put("contentAbstract", elem.contentAbstract)
            map.put("keywords", elem.keywords ?: "")
            map.put("classification", elem.classification)
            map.put("source", elem.source)
            map.put("picUrl", elem.picUrl)
            map.put("type", elem.type)
            map.put("isPublished", elem.isPublished)
            map.put("isReleased", elem.isReleased)
            map.put("userId", elem.userId)
            map.put("orgId", elem.orgId)
            map.put("createTime", elem.createTime)
            map.put("updateTime", elem.updateTime)
            map.put("channelResult", elem.channelResult)
            if (elem.articleId && elem.isReleased) {
                def id = materialRepo.getArticleOperationById(elem.articleId)
                if (id) {
                    map.put("releasedTime", id.updateTime)
                }
            }
            list.add(map)
        }
        return apiResult(status: HttpStatus.SC_OK, msg: list);
    }

    Map getUserMaterialCount(long userId, String queryKeyWords, int isPublished, int isReleased) {
        int result = materialRepo.getUserMaterialCount(userId, queryKeyWords, isPublished, isReleased)
        return apiResult(status: HttpStatus.SC_OK, msg: result);
    }

    void removeUserMaterialList(long userId, List materialIds) {
        def list = materialRepo.getUserMaterialList(userId, materialIds)
        materialRepo.removeMaterialList(userId, materialIds)
        if (list.size() > 0) {
            def articleIds = new ArrayList<>()
            for (int i = 0; i < list.size(); i++) {
                Material material = list.get(i);
                articleIds.add(material.getId())
            }
            materialRepo.removeArticleOperationList(userId, articleIds)
        }
    }
    void removeUserMaterial(long userId, String materialId) {
        def material = materialRepo.getUserMaterial(userId, materialId)
        materialRepo.removeMaterial(userId, materialId)
        if (material.articleId) {
            materialRepo.removeArticleOperation(userId, material.articleId)
        }
    }

    Map addArticlePush(long userId, String agentId, String orgId, String teamId, String materialId) {
        Material material = materialRepo.getUserMaterial(userId, materialId)
        ArticlePush articlePush = new ArticlePush([
                id        : UUID.randomUUID().toString(),
                material  : material.toMap(),
                status    : 1,
                userId    : userId,
                orgId     : orgId,
                updateTime: new Date(),
                createTime: new Date(),
        ])
        String id = materialRepo.addArticlePush(articlePush)
        def result = addArticlePush(material, userId, agentId, orgId, teamId, id, 1, "")
        if (result.status == 200) {
            return apiResult(status: HttpStatus.SC_OK, msg: "success", "id": id)
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "fail", "id": id)
        }
    }

    Map addArticlePush(Material material, Long userId, String agentId, String orgId, String teamId, String id, int operationType,
                       def shareResult) {
        def now = new Date()
        def user = accountRepo.getUserById(userId)
        def team = teamRepo.getTeam(teamId)
        def newsOperationList = []
        List newsList = []
        def newsOperation = new NewsOperation()
        newsOperation._id = id
        newsOperation.newsId = material.id
        newsOperation.agentId = agentId
        newsOperation.orgId = orgId
        newsOperation.userId = userId
        newsOperation.teamId = teamId
        newsOperation.teamName = team ? team.teamName : ""
        newsOperation.publisher = user?.realName ?: user.userName
        newsOperation.updateTime = now
        newsOperation.createTime = now
        newsOperation.news = material.toMap()
        newsOperation.pushType = PushTypeEnum.ARTICLE_PUSH.index
        newsOperation.status = 1
        newsOperation.operationType = operationType
        if (operationType == 5) {
            newsOperation.shareResult = shareResult
        }
        if (operationType == 1) {
            newsOperation.isAutoPush = false
        }
        newsOperationList << newsOperation
        newsRepo.addNewsOperationList(newsOperationList)

        return apiResult(status: HttpStatus.SC_OK, msg: "推送成功")
    }

    Map findAndModifyArticlePush(Material material, Long userId, String agentId, String orgId, String teamId, int operationType,
                                 def shareResult, String timeStamp) {
        def now = new Date()
        Account account = accountRepo.getUserById(userId)
        def team = teamRepo.getTeam(teamId)
        def newsOperation = new NewsOperation()
        newsOperation._id = timeStamp
        newsOperation.newsId = material.id
        newsOperation.agentId = agentId
        newsOperation.orgId = orgId
        newsOperation.userId = userId
        newsOperation.teamId = teamId
        newsOperation.publisher = account?.realName ?: account.userName
        newsOperation.teamName = team ? team.teamName : ""
        newsOperation.updateTime = now
        newsOperation.createTime = now
        newsOperation.news = material.toMap()
        newsOperation.pushType = PushTypeEnum.ARTICLE_PUSH.index
        newsOperation.status = 1
        newsOperation.operationType = operationType
        if (operationType == 5) {
            newsOperation.shareResult = shareResult
        }
        int channelType = shareResult[0].channelType
        newsOperation.timeStamp = timeStamp
        newsRepo.findAndModifyNewsOperation(userId, newsOperation, timeStamp, channelType)

        return apiResult(status: HttpStatus.SC_OK, msg: "同步成功")
    }


    Map addArticle(long userId, String orgId, String materialId, int operationType) {
        Material material = materialRepo.getUserMaterial(userId, materialId)
        Date now = new Date()
        def article = [
                material     : material.toMap(),
                userId       : userId,
                orgId        : orgId,
                updateTime   : now,
                createTime   : now,
                operationType: operationType
        ]
        if (operationType == MaterialRepo.ARTICLE_TYPE_PUSH) {
            article.status = 1
        }
        article.id = materialRepo.addArticle(article)
        return apiResult(status: HttpStatus.SC_OK, msg: article)
    }

    Map getUserArticlePush(long userId, String id) {
        ArticlePush articlePush = materialRepo.getUserArticlePush(userId, id)
        if (!articlePush) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "没有找到对应的信息"])
        }
        return apiResult([status: HttpStatus.SC_OK, msg: articlePush])
    }

    Map importArticle(long userId, String agentId, String orgId, String teamId, String title, String content) {
        def news = [title: title, content: content]
        def newsOperation = new NewsOperation()
        newsOperation._id = UUID.randomUUID().toString()
        newsOperation.agentId = agentId
        newsOperation.teamId = teamId
        newsOperation.orgId = orgId
        newsOperation.userId = userId
        newsOperation.updateTime = new Date()
        newsOperation.createTime = new Date()
        newsOperation.news = news
        newsOperation.operationType = 4
        //获得用户的导入分组id
        def favoriteGroup = favoriteGroupRepo.getImportGroupListByUserId(userId)
        def groupId = favoriteGroup.getId()
        String newsOperationId = null
        if (groupId != null) {
            newsOperation.groupId = groupId
            newsOperationId = materialRepo.importArticle(newsOperation)
        }
        if (newsOperationId) {
            return apiResult(status: HttpStatus.SC_OK, msg: "导入Word成功", newsOperationId: newsOperationId)
        } else {
            return apiResult(status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "导入Word失败")
        }
    }

    Map getUserNewsOperationById(String newsOperationId) {
        def result = materialRepo.getUserNewsOperationById(newsOperationId)
        if (!result) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "没有找到对应的文稿信息"])
        }
        return apiResult(status: HttpStatus.SC_OK, msg: result)
    }

    Map getUserArticleOperationById(String operationId) {
        def result = materialRepo.getArticleOperationById(operationId)
        if (!result) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "没有找到对应的文章信息"])
        }
        return apiResult(status: HttpStatus.SC_OK, msg: result)
    }

    List queryUserMaterials() {
        def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
        List<Account> validUsers = accountRepo.getValidUsers();
        List materialList = new ArrayList()
        for (int i = 0; i < validUsers.size(); i++) {
            List<Map> mapList = new ArrayList<>()
            Account account = validUsers.get(i)
            long userId = account.getId();
            String userName = account.getUserName();
            String realName = account.getRealName();
            String company = account.getCompany();
            List materials = materialRepo.queryUserMaterials(account.getId())
            List<Map> lists = new ArrayList<>()
            for (int j = 0; j < materials.size(); j++) {
                Material material = materials.get(j);
                Map map = new HashMap<>();
                map.put("id", material.getId());
                map.put("userId", userId);
                map.put("userName", userName);
                map.put("realName", realName);
                map.put("company", company);
                map.put("title", material.getTitle());
                map.put("contentAbstract", material.getContentAbstract())
                map.put("updateTime", sdf.format(material.getUpdateTime()))
                mapList.add(map);
            }
            materialList.addAll(mapList);
        }
        return materialList;
    }

    List<String> extractCoverList(HttpServletRequest request, String content) {
        def list = StringUtils.extractImgUrl(content)

        //过滤小图片 //todo 增加对图片格式的验证
        def subList = []
        ImageInfo img = null
        for (int i = 0; i < list.size(); i++) {
            String item = list.get(i)
            img = new ImageInfo(item)
            if (img && img.width >= 200 && img.height >= 200) {
                subList.add(item)
            }
        }
        return subList
    }

    void excelOutMaterials(List materialList) {
        String outfileName = UUID.randomUUID();
        ExportExcel ex = new ExportExcel();
        String sheetName = "我的文稿明细";//下载文件的默认名字,sheet页名字
        String fileName = outfileName
        String headers = "id,userId,userName,realName,company,title,contentAbstract,updateTime";
//表头
        String selname = "id,userId,userName,realName,company,title,contentAbstract,updateTime,o"
//标题对应key值

        HSSFWorkbook wb = ex.exportExcel(sheetName, headers, materialList, selname);
//返回一个workbook即excel实例。datetime是sheet页的名称
        String excelFolder = "D:\\" + sheetName;
        def result = [
                status: HttpStatus.SC_OK,
                msg   : '',
        ];
        //如果文件夹不存在则创建，如果已经存在则删除
        File outPath = new File(excelFolder)
        if (!outPath.exists()) {
            FileUtils.forceMkdir(outPath)
        } else {
            FileUtils.forceDelete(outPath)
            FileUtils.forceMkdir(outPath)
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            wb.write(os);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        byte[] content = os.toByteArray();
        OutputStream fos = null;
        File file = new File(excelFolder + "/" + "excel.xls")

        try {
            fos = new FileOutputStream(file);
            fos.write(content);
            os.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        println("默认生成目录在 D:\\" + sheetName);
    }

    Map MaterialRelease(LoginUser user, String materialId) {
        Material material = materialRepo.getUserMaterial(user.userId, materialId)
        if (!material) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "发布失败"])
        }

        def operation = materialRepo.getArticleOperationById(material.articleId)
        if (operation) {
            materialRepo.modifyArticleOperationMaterial(user.userId, material.toMap(), MaterialRepo.ARTICLE_TYPE_RELEASE, material.articleId)
        } else {
            def article = [
                    material     : material.toMap(),
                    userId       : user.userId,
                    orgId        : user.orgId,
                    updateTime   : new Date(),
                    createTime   : new Date(),
                    operationType: MaterialRepo.ARTICLE_TYPE_RELEASE  // 1文章推送 2 文章同步 3 H5发布
            ]
            operation = materialRepo.addArticleOperation(article)
        }

        def released = materialRepo.modifyMaterialReleased(user.userId, material, operation._id)
        if (!released) {
            return apiResult([status: HttpStatus.SC_INTERNAL_SERVER_ERROR, msg: "发布失败"])
        }

        return apiResult([status: HttpStatus.SC_OK, msg: operation])
    }

    def getArticleOperationByTime(String articleId, Date time) {
        materialRepo.getArticleOperationByTime(articleId, time)
    }
}
