if [ -z ${TOMCAT_WEBAPPS} ]; then
    echo "TOMCAT_WEBAPPS environment variable not set.It should point to the location where Tomcat reads the war file and deploys it (not necessarily equal to $$CATALINA_HOME/webapps" 1>&2
    exit 1
fi

if [ -z ${CATALINA_HOME} ]; then
    echo "CATALINA_HOME environment variable not set.It should point to the root of the Tomcat installation" 1>&2
    exit 1
fi

if [ -z ${IUTOOLS_VERSION} ]; then
    echo "IUTOOLS_VERSION environment variable not set.It should be set to the version of iutools we want to redeploy." 1>&2
    exit 1
fi

if [ -z ${IUTOOLS_M2} ]; then
    echo "IUTOOLS_M2 environment variable not set.It should be set to the version location of the Maven directory (.m2) where the iutools jars and wars were compiled." 1>&2
    exit 1
fi

# Makes the bash script to print out every command before it is executed except echo
trap '[[ $BASH_COMMAND != echo* ]] && echo $BASH_COMMAND' DEBUG

m2_war=$IUTOOLS_M2/repository/org/iutools/iutools-apps/${IUTOOLS_VERSION}/iutools-apps-${IUTOOLS_VERSION}.war

# Delete the old WAR file and iutools directories on Tomcat
tomcat_war=$TOMCAT_WEBAPPS/iutools.war
echo
echo "==="
echo "=== Deleting the old WAR file from Tomcat"
echo "===    old war: ${tomcat_war}"
echo "==="
sudo rm $tomcat_war
    
# Delete old iutools directory from the Tomcat webapps
old_dir=$TOMCAT_WEBAPPS/iutools
echo
echo "==="
echo "=== Delete the old iutools directory from Tomcat"
echo "===    old iutools dir: $old_dir"
echo "==="
sudo rm -r $old_dir

# Copy new version of the WAR to Tomcat
echo
echo "==="
echo "=== Copying new version of WAR file from:"
echo "===   $m2_war"
echo "=== to:"
echo "===   $tomcat_war"
echo "==="
sudo cp $m2_war $tomcat_war

# Restart Tomcat
echo
echo "==="
echo === Stopping Tomcat
echo "==="
sudo sh $CATALINA_HOME/bin/shutdown.sh
echo
echo ===
echo === Sleeping for a bit...
echo ===
sleep 2
ehcho
echo ===
echo === Restarting Tomcat
echo ===
sudo sh $CATALINA_HOME/bin/catalina.sh jpda start
