cd /var/svn/bjj

time=`date "+%Y%m%d%H%M%S"`
backup_file=/var/mediabroken/backup/mediabroken-1.0-bakcup-${time}.war
cp /usr/local/apache-tomcat-8.5.13/webapps/mediabroken-1.0.war ${backup_file}
cp /var/svn/bjj/build/libs/mediabroken-1.0.war /usr/local/apache-tomcat-8.5.13/webapps
