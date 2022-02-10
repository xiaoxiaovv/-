package com.istar.mediabroken.console

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Controller

/**
 * 必须配置taskName和当前目录名
 */
@Controller
class ConsoleManager {
    static void main(String[] args) {
        ApplicationContext context = new
                ClassPathXmlApplicationContext("classpath*:spring*.xml");
        def console = getConsole(context, args[0])
        Properties properties = null
        if (console.getPropertyFileName()) {
            BufferedReader fin = new BufferedReader(new InputStreamReader(
                    new BufferedInputStream(
                            new FileInputStream(new File(args[1], console.getPropertyFileName()))), "UTF-8"))
//            println Thread.currentThread().getContextClassLoader().getResource("add_user.properties")
//            InputStream fin = getClass().getClassLoader().getResource("add_user.properties");
            properties = new Properties();
            properties.load(fin);
        }
        try {
            console.execute(properties)
        }catch (Exception e){
            e.printStackTrace()
        }
    }

    static Console getConsole(ApplicationContext context, String taskName) {
        def console = context.getBean(taskName) as Console;
        return console
    }
}
