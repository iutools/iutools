admin_lib_path=`realpath $(dirname "$0")/lib`

echo "*** redeploy-webapps.bash ***"
echo "Running with"
source $admin_lib_path/validate_IUTOOLS_VERSION.bash
source $admin_lib_path/validate_CATALINA_HOME.bash
source $admin_lib_path/validate_TOMCAT_WEBAPPS.bash
source $admin_lib_path/validate_IUTOOLS_M2.bash
echo

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
echo
echo ===
echo === Restarting Tomcat
echo ===
sudo sh $CATALINA_HOME/bin/catalina.sh jpda start
