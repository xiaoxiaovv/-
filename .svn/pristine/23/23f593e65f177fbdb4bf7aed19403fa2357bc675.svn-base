package com.istar.mediabroken.service.alioss

import com.istar.mediabroken.repo.alioss.OssRepo
import com.istar.mediabroken.utils.HttpHelper
import com.istar.mediabroken.utils.OssSignUtil
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * @description: 阿里云oss调用
 * @author: hexushuai
 * @date: 2019/1/22 15:27
 */
@Service
@Slf4j
class OssApiUrlService {

    @Autowired
    private OssRepo ossRepo

    /**
     * 获取提交转码任务的url
     */
    public def getSubmitTransCodeUrl(String orgId, String object) {
        List urlList = new ArrayList()
        Map<String, String> parameterMap = buildPublicParameter()
        Map map = ossRepo.getAliyunOssParameter(orgId)
        String secret = map.get("accessKey")
        String publishTemplateId = map.get("publishTemplateId")
        String previewTemplateId = map.get("previewTemplateId")
        String pipelineId = map.get("pipelineId")
        String publishOutputDir = map.get("publishDir")
        parameterMap = buildSubmitTransCodeMap(publishTemplateId, pipelineId, object, publishOutputDir, map, parameterMap)
        String publishUrl = OssSignUtil.sign(secret, parameterMap)
        urlList.add(publishUrl)
        parameterMap.put("SignatureNonce", UUID.randomUUID().toString())
        String previewOutputDir = map.get("previewDir")
        parameterMap = buildSubmitTransCodeMap(previewTemplateId, pipelineId, object, previewOutputDir, map, parameterMap)
        String previewUrl = OssSignUtil.sign(secret, parameterMap)
        urlList.add(previewUrl)
        return urlList
    }

    /**
     * 构造调用阿里云oss服务参数
     */
    private  Map<String, String> buildSubmitTransCodeMap(String templateId, String pipelineId, String object, String outputDir, Map map, Map<String, String> parameterMap) {
        parameterMap.put("AccessKeyId", map.get("accessId"))
        String input = "{\"Bucket\":\"" + map.get("bucketVideo") + "\",\"Location\":\"oss-cn-beijing\",\"Object\":\"" + object + "\"}"
        String transferName = object.substring(0, object.lastIndexOf(".")) + ".mp4"
        String outputs = "[{\"OutputObject\":\"" + outputDir + "/" + transferName + "\",\"TemplateId\":\"" + templateId + "\",\"UserData\":\""+ map.get("userData") +"\"}]"
        String outputLocation = "oss-cn-beijing"
        String outputBucket = map.get("bucketTranscode")
        parameterMap.put("Action", "SubmitJobs")
        parameterMap.put("Input", input)
        parameterMap.put("userData", map.get("userData"))
        parameterMap.put("Outputs", outputs)
        parameterMap.put("OutputLocation", outputLocation)
        parameterMap.put("OutputBucket", outputBucket)
        parameterMap.put("PipelineId", pipelineId)
        return parameterMap
    }

    /**
     * 获取查询转码状态的url
     */
    public String getStateTransCodeUrl(String orgId, List jobIdList) {
        Map<String, String> parameterMap = buildPublicParameter()
        Map map = ossRepo.getAliyunOssParameter(orgId)
        String secret = map.get("accessKey")
        parameterMap.put("Action", "QueryJobList")
        parameterMap.put("AccessKeyId", map.get("accessId"))
        parameterMap.put("userData", map.get("userData"))
        String jobIds = ""
        for(String jobId: jobIdList) {
            jobIds += jobId + ","
        }
        parameterMap.put("JobIds", jobIds.substring(0, jobIds.length() - 1))
        String url = OssSignUtil.sign(secret, parameterMap)
        log.info("getStateTransCodeUrl is :{}", url)
        return url

    }

    /**
     * 构造公有参数
     */
    private Map<String, String> buildPublicParameter() {
        Map<String, String> parameterMap = new HashMap<String, String>()
        parameterMap.put("Version", "2014-06-18")
        parameterMap.put("Timestamp", OssSignUtil.formatIso8601Date(new Date()))
        parameterMap.put("SignatureMethod", "HMAC-SHA1")
        parameterMap.put("SignatureVersion", "1.0")
        parameterMap.put("SignatureNonce", UUID.randomUUID().toString())
        parameterMap.put("Format", "JSON")
        return parameterMap
    }

    /**
     * 获取视频截帧请求url
     */
    public void doCutFrameToImg(String orgId, String object) {
        Map<String, String> parameterMap = buildPublicParameter()
        Map map = ossRepo.getAliyunOssParameter(orgId)
        String secret = map.get("accessKey")
        String pipelineId = map.get("pipelineId")
        String cutFrameName = object.substring(0, object.lastIndexOf(".")) + ".jpg"
        String cutFrameObject = map.get("cutFrameDir") + "/" + cutFrameName
        String input = "{\"Bucket\":\"" + map.get("bucketVideo") + "\",\"Location\":\"oss-cn-beijing\",\"Object\":\"" + object + "\"}"
        String output = "{\"OutputFile\":{\"Bucket\":\"" + map.get("bucketTranscode") + "\", \"Location\":\"oss-cn-beijing\",\"Object\":\"" + cutFrameObject + "\"},\"Time\": \"5\"}"
        parameterMap.put("Action", "SubmitSnapshotJob")
        parameterMap.put("AccessKeyId", map.get("accessId"))
        parameterMap.put("userData", map.get("userData"))
        parameterMap.put("Input", input)
        parameterMap.put("SnapshotConfig", output)
        parameterMap.put("PipelineId", pipelineId)
        String url = OssSignUtil.sign(secret, parameterMap)
        HttpHelper.doGet(url, null)
    }
}
