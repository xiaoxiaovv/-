package com.istar.mediabroken.utils.wordseg;


import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class WordSegUtil {

    public static String seg(String text) {
        List<Term> parse = ToAnalysis.parse(text);

        parse= MyFilterModifWord.modifResult(parse) ;
        StringBuilder buffer = new StringBuilder(text.length());
        for (Term term: parse) {
            buffer.append(term.getName());
            buffer.append(" ");
        }
        return buffer.toString();
    }

    public static String extractKeywords(String title, String content, int keywordCount) {
        KeyWordComputer kwc = new KeyWordComputer(keywordCount);
        Collection<Keyword> result = kwc.computeArticleTfidf(title, content);
        StringBuffer sb = new StringBuffer();
        Iterator<Keyword> it = result.iterator();
        while (it.hasNext()) {
            sb.append(it.next().getName()).append(" ");
        }
        return sb.toString().trim();
    }

    public static List<Term>  segToTermList(String text) {
        List<Term> parse = ToAnalysis.parse(text);

        parse = MyFilterModifWord.modifResult(parse) ;

        return parse;
    }

    public static String[] segToArray(String text) {
        List<Term> parse = ToAnalysis.parse(text);
        parse= MyFilterModifWord.modifResult(parse) ;
        String[] result = new String[parse.size()];
        int i = 0;
        for (Term term: parse) {
            result[i++] = term.getName();
        }
        return result;
    }

    public static void main(String[] args) {
        String title = "维基解密否认斯诺登接受委内瑞拉庇护";
        String context = "有俄罗斯国会议员，9号在社交网站推特表示，美国中情局前雇员斯诺登，已经接受委内瑞拉的庇护，不过推文在发布几分钟后随即删除。俄罗斯当局拒绝发表评论，而一直协助斯诺登的维基解密否认他将投靠委内瑞拉。　　俄罗斯国会国际事务委员会主席普什科夫，在个人推特率先披露斯诺登已接受委内瑞拉的庇护建议，令外界以为斯诺登的动向终于有新进展。　　不过推文在几分钟内旋即被删除，普什科夫澄清他是看到俄罗斯国营电视台的新闻才这样说，而电视台已经作出否认，称普什科夫是误解了新闻内容。　　委内瑞拉驻莫斯科大使馆、俄罗斯总统府发言人、以及外交部都拒绝发表评论。而维基解密就否认斯诺登已正式接受委内瑞拉的庇护，说会在适当时间公布有关决定。　　斯诺登相信目前还在莫斯科谢列梅捷沃机场，已滞留两个多星期。他早前向约20个国家提交庇护申请，委内瑞拉、尼加拉瓜和玻利维亚，先后表示答应，不过斯诺登还没作出决定。　　而另一场外交风波，玻利维亚总统莫拉莱斯的专机上星期被欧洲多国以怀疑斯诺登在机上为由拒绝过境事件，涉事国家之一的西班牙突然转口风，外长马加略]号表示愿意就任何误解致歉，但强调当时当局没有关闭领空或不许专机降落。";

        System.out.println(extractKeywords(title, context, 5));
//        System.out.println(WordSegUtil.seg("你让战士们过一个欢乐祥和的新春佳节。 1.5吨重的坦克， http://www.sina.com"));
//
////        UserDefineLibrary.insertWord("张艺谋谋", "userDefine", 1000);
//
//        System.out.println(WordSegUtil.seg("我喜欢张艺谋谋"));
//
//        System.out.println(WordSegUtil.seg("你们可以随便参观"));
//        System.out.println(ToAnalysis.parse("你们可以随便参观"));
//
//        System.out.println(ToAnalysis.parse("数据显示，当前我国眼科用药市场尚处于发展阶段。在全国约5000家制药企业中，仅有不足200家生产眼科用药。2006～2012年，我国眼科药物市场规模从43.20亿元增长到105.71亿元，年均复合增长率为16.08%。预计至2018年，全球眼科外用制剂有望达到216亿美元，而我国眼科药物市场规模也将超过300亿元人民币，呈现继续增长之势。</p><p>另一方面，眼科保健类产品的市场份额还比较小。庶正康讯市场调研负责人陈白雪告诉本报记者：“截至2016年5月，我国共批准‘缓解视疲劳’保健食品129个，‘改善视力’的20个，多集中在叶黄素、各种有一定抗氧化功能的植物提取物上，如蓝莓提取物、越橘提取物等。”</p><p>面对眼科用药的“超级蓝海”，记者还发现一个有意思的现象：眼科领域似乎是一座“围城”，里面的企业正期待走出城门，而外面的企业则试图走进城门。或许是受限于单一市场的限制，莎普爱思、天目药业等专业眼药企业目前正尝试转型大健康，挑战业务多元化。科瑞、江中等药企则试图拓展业务范围，频频朝眼科领域抛出了橄榄枝。</p><p><strong>新的目标猎物</strong></p><p>相关眼科用药市场研究报告显示，2014年，莎普爱思滴眼液在全国白内障药物市场占有率为28.23%。作为该企业的拳头品种，其2015年报显示，莎普爱思滴眼液生产量为2689.56万支，同比增长 42.13%；销售量为2487.94万支，同比增长30.82%；库存量为267.99万支，同比增长302.54%。</p><p>虽然主营业务呈现良好的增长势头，但莎普爱思的主营业务仍略显单一，并表示了对未来大健康产业的看好。近日，该企业公布了一季报，以3.46亿元收购吉林省东丰药业股份有限公司持有的吉林强身药业有限责任公司100%股权。实际上，莎普爱思从去年开始就发布了重组方案，期望通过收购拥有77个OTC品种的165个<strong>药品</strong>批准文号、主营中老年保健品的吉林强身药业，进军中药领域。</p><p>天目药业则积极转战大健康。其公布的2015年报显示，实现营业收入9476万元，同比下降36%；实现利润总额为2120万元，同比下降641%；归属于母公司所有者的净利润亏损2154万元，同比下降894%。天目药业负责人对外表示，未来企业将关注包括医疗设备、医疗卫生、制药产业等行业，以延缓衰老、防范疾病、维护生命健康为目标。</p><p>“天目药业如果希望在主营业务上有所改善，借助当前热门的大健康产业是一条不错的出路。”某分析人士如是说。</p><p><strong>眼保健食品</strong><strong>成明日之星</strong></p><p>眼保健食品在国外非常受欢迎。据悉，目前已有上千品种在欧美日等国外市场畅销。资料显示，欧美市场上销售的眼保健食品大体可分为两大类：一是维生素类眼保健食品，其代表为VC+VE胶丸、VC+β-胡萝卜素胶丸和VA胶丸等；二是在上世纪90年代异军突起的以蓝莓为主的浆果类植物提取物的新型眼保健食品。</p><p>“比如日本知名眼科药企参天制药，各种滴眼液是海外代购的宠儿，最近也在我国申报下来一款叶黄素保健食品。” 陈白雪介绍说。</p><p>而从国内情况看，有生物科技公司引进了单剂量滴眼液产品线，称生产的产品没有防腐剂，符合当前药品使用趋势，并有望在2016年底上市。“此外，鉴于眼科产品的消费群体很大，浙江康恩贝去年向可得眼镜网投资3.2亿元，估值达16亿元。”前述市场人士介绍道。</p><p>尽管如此，国内的眼<strong>保健品</strong>市场依旧未成大规模。除一些传统中药眼药制剂（如珍珠明目滴眼液、麝珠明目滴眼液、熊胆明目滴眼液、明目地黄丸、石斛夜光丸等）之外，市场上很少有知名度更高的眼保健类产品。保健食品方面，主要有深圳海王的“眼之宝”等以蓝莓为主要原料的护目食品。</p><p>采访中其行业专家对此现象分析道，我国现行保健食品注册管理制度中，声称“缓解视疲劳”的保健食品按要求需要进行人体试验，费用高，时间长；如只按照“营养素补充剂”的要求进行注册，则无法进行声称。所以很多对眼睛有好处的保健品，在市场上无法进行相关宣传，消费者对于该类产品的认知并不高。</p><p>实际上，利用中<strong>药材</strong>开发眼保健食品应是我国厂商可走的一条捷径。</p><p>据了解，至少有一二十种中药材具有“清肝明目”的作用。如白菊花、草决明子、桔梗等。想要打开国内眼保健食品市场，分析人士预计，“国内生产商还需要做大量的工作。”</p><p>近段时间，为满足无处不在的电子时代护眼需要，江中集团旗下一款主打保护眼睛的饮料“蓝枸饮料”面世。目前该产品多在网上销售，剑指中高端。</p><p>“蓝莓提取物、越橘提取物这类都比较火，主要是跟着跨境电商一起进来的海外产品，都是近两年才热起来的。”采访中一位市场人士表示，“一些"));


//        List<Term> parse = ToAnalysis.parse("让战士们过一个欢乐祥和的新春佳节。 1.5吨重的坦克， http://www.sina.com");
////        FilterModifWord.insertStopWord("http", "com", "www", "com", "cn") ;
//        MyFilterModifWord.insertStopNatures("w", null, "en", "m");
//        MyFilterModifWord.insertStopWord("们", "的", "过", "让", "重", ".") ;
//        parse= MyFilterModifWord.modifResult(parse) ;
//        System.out.println(parse);
////        for (Term term: parse) {o
////            System.out.print(term.getName());
////            System.out.print(">");
////            System.out.print(term.natrue());
////            System.out.print(",");
////
////        }


    }

    public static void loadUserDefineLibrary(String fileName) throws IOException {
        File file = new File(fileName);
        String lineTxt;

        if (!file.exists()) {
          return;
        }

        InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");//考虑到编码格式
        BufferedReader bufferedReader = new BufferedReader(read);
        System.out.println("load dict: " + file);
        int i = 0;
        while ((lineTxt = bufferedReader.readLine()) != null) {
            if (i++ % 10000 == 0) {
                System.out.println(i);
            }
            UserDefineLibrary.insertWord(lineTxt.trim(), "userDefine", 1000);
        }
        read.close();
    }

    public static void loadStopWord(String fileName) throws IOException {
        MyFilterModifWord.insertStopNatures("w", null, "m");

        File file = new File(fileName);
        String lineTxt;

        if (!file.exists()) {
            return;
        }

        InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");//考虑到编码格式
        BufferedReader bufferedReader = new BufferedReader(read);
        System.out.println("load stop word: " + file);
        int i = 0;
        while ((lineTxt = bufferedReader.readLine()) != null) {
            if (i++ % 10000 == 0) {
                System.out.println(i);
            }
            MyFilterModifWord.insertStopWord(lineTxt.trim());
        }
        read.close();
    }
}
