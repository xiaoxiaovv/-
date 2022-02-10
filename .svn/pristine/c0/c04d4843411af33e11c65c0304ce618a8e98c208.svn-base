#!/usr/bin/env bash

svn_path=/var/svn/bjjtask
task_home=/usr/local/bjjtask

cd ${svn_path}

#/bin/cp src/main/resources/config.properties.test src/main/resources/config.properties -f
#/bin/cp src/main/resources/config.weibo.properties.test src/main/resources/config.weibo.properties -f

gradle jar copyJar -Dprofile=test
