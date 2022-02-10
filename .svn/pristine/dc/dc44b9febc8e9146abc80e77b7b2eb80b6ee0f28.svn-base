#!/usr/bin/env bash

project_path=/var/svn/bjj
task_home=/var/svn/task
log_path=$task_home/logs

echo '执行task初始化-begin------'

svn up

/bin/cp src/main/resources/config.properties.online src/main/resources/config.properties -f
/bin/cp src/main/resources/config.weibo.properties.online src/main/resources/config.weibo.properties -f

gradle jar

if [ ! -d "$task_home" ]; then
 mkdir "$task_home"
fi

mv $project_path/build/libs/mediabroken-1.0.jar $task_home/
cp -rf $project_path/build/libs/WEB-INF/lib $task_home/
cp $project_path/task_exec.sh $task_home/

cd $task_home

chmod 777 *.sh

if [ ! -d "$log_path" ]; then
 mkdir "$log_path"
fi

echo '执行task初始化-end------'

