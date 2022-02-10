APP_HOME=/var/svn/bjj
TOMCAT_HOME=/usr/local/apache-tomcat-8.5.13/

cd $APP_HOME
cp $APP_HOME/build/libs/mediabroken-1.0.war $TOMCAT_HOME/webapps
