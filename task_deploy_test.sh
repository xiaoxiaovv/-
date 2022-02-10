#!/usr/bin/env bash

svn_path=/var/svn/bjjtask
task_home=/usr/local/bjjtask

cd ${svn_path}
mkdir -p ${task_home}/lib
/bin/cp -rf ${svn_path}/build/lib/*.jar ${task_home}/lib
/bin/cp -rf ${svn_path}/build/libs/*.jar ${task_home}/
cp ${svn_path}/task_exec.sh ${task_home}/

chmod +x ${task_home}/*.sh


