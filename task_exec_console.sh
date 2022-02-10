#!/bin/sh

task_home=/Users/steven/project/trunk2/bjj/build/console

for i in $task_home/lib/*.jar;
    do CLASSPATH="$CLASSPATH":$i;
done

export CLASSPATH=$task_home/mediabroken-1.0.jar:$task_home/:$CLASSPATH

pid=`ps -ef | grep $1 | grep java | head -1 | awk '{print  $2}'`
if [ -z "$pid" ]  
then  
    java -Xms512m -Xmx512m -XX:MaxPermSize=64m -XX:PermSize=64m -XX:MaxNewSize=128m -XX:NewSize=128m -classpath $CLASSPATH com.istar.mediabroken.console.ConsoleManager addUserConsole $task_home
else
    echo "$1 is running"
fi