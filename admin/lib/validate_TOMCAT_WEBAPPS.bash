if [ -z ${TOMCAT_WEBAPPS} ]; then
    echo "TOMCAT_WEBAPPS environment variable not set.It should point to the location where Tomcat reads the war file and deploys it (not necessarily equal to $$CATALINA_HOME/webapps" 1>&2
    exit 1
fi
echo "   TOMCAT_WEBAPPS=$TOMCAT_WEBAPPS"