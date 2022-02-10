import com.istar.mediabroken.api3rd.CaptureApi3rd
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.repo.CaptureRepo
import org.springframework.context.support.ClassPathXmlApplicationContext

def ctx = new
        ClassPathXmlApplicationContext("classpath*:spring*.xml");


AccountRepo accountReop = ctx.getBean(AccountRepo)
CaptureApi3rd captureApi = ctx.getBean(CaptureApi3rd)
def  captureRepo = ctx.getBean(CaptureRepo)
def session = accountReop.getYqmsSession()


def siteSubjects = []
def sites = captureRepo.getValidSites(1491536104387, 0)
println sites.each {
     def subjectMap = captureRepo.getSubjectMap(it)
    captureApi.getNewsCount()

    siteSubjects << [siteName: it.siteName, subjectName: subjectMap.yqmsSubjectName]
}

println siteSubjects



//while (true) {
//    def subjects2 =  captureApi.getSubjectList(session.session, session.yqmsUserId)
//    if (!subjects2.list) break;
//    subjects2.list.each {
//        if (subjectIds.contains(it.subjectId) || it.subjectName == '热门微信' || it.subjectName == '热门站点') {
//            println 'baoliu' + it
//        } else {
//            println 'remove' + it
//            captureApi.removeSubject(session.session, session.yqmsUserId, it.subjectId)
//        }
//    }
//}



