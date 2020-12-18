#
# Use this to update UI files (html, css, js) to the latest version located in your
# iutools Git project. This is faster than redeploy-webapps.sh because it does
# not require re-starting Tomcat.
#

if [ -z ${IUTOOLS_SOURCES} ]; then
    echo "The IUTOOLS_SOURCES environment variable must be set to the root of your iutools project"
    exit 1
fi

if [ -z ${TOMCAT_WEBAPPS} ]; then
    echo "TOMCAT_WEBAPPS environment variable not set.It should point to the location where Tomcat reads the war file and deploys it (not necessarily equal to $$CATALINA_HOME/webapps" 1>&2
    exit 1
fi

if [ -z ${CATALINA_HOME} ]; then
    echo "CATALINA_HOME environment variable not set.It should point to the root of the Tomcat installation" 1>&2
    exit 1
fi

# Makes the bash script to print out every command before it is executed except echo
trap '[[ $BASH_COMMAND != echo* ]] && echo $BASH_COMMAND' DEBUG

sudo cp -pr $IUTOOLS_SOURCES/iutools-apps/src/main/webapp/* $TOMCAT_WEBAPPS/iutools/
