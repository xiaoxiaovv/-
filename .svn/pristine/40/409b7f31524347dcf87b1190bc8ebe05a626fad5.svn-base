import com.istar.mediabroken.api3rd.CaptureApi3rd
import com.istar.mediabroken.repo.account.AccountRepo
import com.istar.mediabroken.utils.MongoHolder
import org.springframework.context.support.ClassPathXmlApplicationContext

import static com.istar.mediabroken.utils.MongoHelper.toObj

/**
 * 清理无用的专题
 */
def ctx = new ClassPathXmlApplicationContext("classpath*:spring*.xml");
def captureApi = ctx.getBean(CaptureApi3rd)
def accountRepo = ctx.getBean(AccountRepo)
def mongo = ctx.getBean(MongoHolder)
def session = accountRepo.getYqmsSession2()


println 'site'
def subjectMapIds = new HashSet()
def collection = mongo.getCollection("site")
def cursor = collection.find()
while (cursor.hasNext()) {
    def it = cursor.next()
    subjectMapIds << it.subjectId
    println it.subjectId
}
cursor.close()

println 'subjectMap'
def subjectIds = new HashSet()
def deleteSubjectMapIds = new HashSet()
collection = mongo.getCollection("subjectMap")
cursor = collection.find()
while (cursor.hasNext()) {
    def it = cursor.next()
    if (subjectMapIds.contains( it._id)) {
        subjectIds << it.yqmsSubjectId
        println it.yqmsSubjectId
    } else {
        println '需要删除' + it.yqmsSubjectId
        deleteSubjectMapIds << it._id
    }
}
cursor.close()

collection = mongo.getCollection("subjectMap")
deleteSubjectMapIds.each {
    cursor = collection.remove(toObj([_id: it]))
}

println 'specialFocus'
collection = mongo.getCollection("specialFocus")
cursor = collection.find()
while (cursor.hasNext()) {
    def it = cursor.next()
    subjectIds << it.yqmsSubjectId
    println it.yqmsSubjectId
}
cursor.close()

println 'focus'
collection = mongo.getCollection("focus")
cursor = collection.find()
while (cursor.hasNext()) {
    def it = cursor.next()
    subjectIds << it.yqmsSubjectId
    println it.yqmsSubjectId
}
cursor.close()

// todo subjectMap中有,但site中没有,要去除的

def subjects = []
int i = 0
for (def pageNo = 1; true; pageNo++) {
    def paging = captureApi.getPagingSubjectList(session, pageNo, 50)
    if (!paging.list) {
        break
    }
    paging.list.each {
        if (
        it.subjectName.startsWith('st')
                || it.subjectName.startsWith('wc')
                || it.subjectName.startsWith('sname')
                || it.subjectName.startsWith('fc')) {
            if (!subjectIds.contains(it.subjectId)) {
                subjects << it
//                println it
            } else {
                println it
                i++

            }

        }
    }
}

println i

println subjects.size()
subjects.each {
    captureApi.removeSubject(session.sid, session.userId, it.subjectId)
}