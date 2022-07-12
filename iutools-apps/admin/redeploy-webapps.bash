admin_path=`realpath $(dirname "$0")`
admin_lib_path=`realpath $(dirname "$0")/lib`

echo "*** redeploy-webapps.bash ***"
echo
echo "Running with"
source $admin_lib_path/validate_IUTOOLS_VERSION.bash
source $admin_lib_path/validate_CATALINA_HOME.bash
source $admin_lib_path/validate_TOMCAT_WEBAPPS.bash
#source $admin_lib_path/validate_IUTOOLS_M2.bash
echo
echo

# Makes the bash script to print out every command before it is executed except echo
#trap '[[ $BASH_COMMAND != echo* ]] && echo $BASH_COMMAND' DEBUG

#
# Determine the path of the new war file
# First check to see if it's in the target directory.
# This corresponds to a situation where we are running the script from a Git repo.
#
distro_war=`realpath $admin_path/../target/iutools-apps.war`

if [ ! -f $distro_war ]; then
  # Next, check if the war is one level up.
  # This corresponds to a situation where we run the admin script from an unziped
  # distro.
  distro_war=`realpath $admin_path/../iutools-apps.war`
fi

if [ ! -f $distro_war ]; then
  echo "Could not locate the iutools-apps.war file"
  exit 100
fi

echo === distro_war=${distro_war}

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

iutools_war=$admin_lib_path/../iutools-${IUTOOLS_VERSION}-
if [ -f "$FILE" ]; then
    echo "$FILE exists."
fi
echo
echo "==="
echo "=== Copying new version of WAR file from:"
echo "===   $distro_war"
echo "=== to:"
echo "===   $tomcat_war"
echo "==="
sudo cp $distro_war $tomcat_war

# Restart Tomcat
echo
echo "==="
echo === Stopping Tomcat
echo "==="
sudo sh $CATALINA_HOME/bin/shutdown.sh
echo
echo ===
echo === Sleeping for 10 secss...
echo ===
sleep 10

echo -ne '\007'; sleep 1; echo -ne '\007'
echo
echo ===
read -p $'==\n== Check that Tomcat has been properly SHUTDOWN, then press any key to continue\n==\n\n> '
echo
echo
echo ===
echo === Restarting Tomcat
echo ===
sudo sh $CATALINA_HOME/bin/catalina.sh jpda start

echo ===
echo === Sleeping for 10 secs ...
echo ===
sleep 10

echo -ne '\007'; sleep 1; echo -ne '\007'
echo
echo ===
read -p $'==\n== Check to make sure Tomcat has been properly RESTARTED, then press any key to continue\n==\n\n> '
echo
