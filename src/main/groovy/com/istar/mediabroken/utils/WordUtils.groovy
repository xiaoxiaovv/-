package com.istar.mediabroken.utils

import com.istar.mediabroken.Const
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateException
import org.apache.http.HttpStatus

/**
 * Author : YCSnail
 * Date   : 2017-04-25
 * Email  : liyancai1986@163.com
 */
class WordUtils {

    /**
     *
     * @param targetPath
     * @param data {
     *     fileName   : '文件名',
     *     fileType : 'news|abstract|summary',
     *     ...
     * }
     * @return
     */
    static Map createWord(String targetPath, Map<String, Object> data, String env) {

        Configuration configuration = new Configuration();
        configuration.setDefaultEncoding("UTF-8");

        String filePath
        if (Const.ENV_ONLINE == env || Const.ENV_TEST == env) {
            filePath = new File(WordUtils.class.getResource("/").path, 'template').path
        } else {
            filePath = new File(WordUtils.class.getResource("/").path.replace('/classes/', '/resources/'), 'template').path
        }

        configuration.setDirectoryForTemplateLoading(new File(filePath))
        Template t = null;

        try {
//            t = configuration.getTemplate(data.fileType.toString() + ".ftl"); //文件名
            t = configuration.getTemplate("${data.fileType.toString()}.ftl"); //文件名
        } catch (IOException e) {
            return [
                    status  : HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    msg     : ''
            ]
        }

//        String completeFilePath = "/download/${data.fileType as String}/${data.fileName as String}" +
//                "/${data.fileType as String}.doc"
        String completeFilePath = "/download/${data.fileType as String}/${data.fileName as String}/${data.fileName as String}.doc"

        File outFile = new File(targetPath, completeFilePath);
        Writer out = null;
        FileOutputStream fos = null;
        OutputStreamWriter oWriter = null
        try {
            fos = new FileOutputStream(outFile);
            oWriter = new OutputStreamWriter(fos,"UTF-8");
            out = new BufferedWriter(oWriter);

        } catch (FileNotFoundException e) {
            return [
                    status  : HttpStatus.SC_NOT_FOUND,
                    msg     : ''
            ]
        }

        try {
            t.process(data, out);
            out.close();
            oWriter.close()
            fos.close();
        } catch (TemplateException e) {
            return [
                    status  : HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    msg     : '生成word文档发生错误！'
            ]
        } catch (IOException e) {
            return [
                    status  : HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    msg     : ''
            ]
        }

        return [
                status       : HttpStatus.SC_OK,
                msg          : completeFilePath,
        ]
    }

}
