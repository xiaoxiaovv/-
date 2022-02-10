import com.istar.mediabroken.service.capture.SiteService
import net.sourceforge.pinyin4j.PinyinHelper
import org.springframework.context.ApplicationContext
import org.springframework.context.support.FileSystemXmlApplicationContext

ApplicationContext appctx = new FileSystemXmlApplicationContext("classpath:spring*.xml");

def siteSer = appctx.getBean("PinyinHelper")

def pinYin = PinyinHelper.toHanyuPinyinStringArray("卢达")

//def result = siteSer.getUserSites(1496822110851L)

println(pinYin)