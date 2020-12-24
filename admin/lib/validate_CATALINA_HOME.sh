if [ -z ${CATALINA_HOME} ]; then
    echo "CATALINA_HOME environment variable not set.It should point to the root of the Tomcat installation" 1>&2
    exit 1
fi
echo "   CATALINA_HOME=$CATALINA_HOME"
