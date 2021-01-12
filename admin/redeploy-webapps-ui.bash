#
# Use this to update UI files (html, css, js) to the latest version located in your
# iutools Git project. This is faster than redeploy-webapps.sh because it does
# not require re-starting Tomcat.
#
admin_lib_path=$(dirname "$0")/lib

echo Running with
source $admin_lib_path/validate_CATALINA_HOME.bash
source $admin_lib_path/validate_TOMCAT_WEBAPPS.bash
source $admin_lib_path/validate_IUTOOLS_M2.bash
echo


# Makes the bash script to print out every command before it is executed except echo
trap '[[ $BASH_COMMAND != echo* ]] && echo $BASH_COMMAND' DEBUG

sudo cp -pr $IUTOOLS_SOURCES/iutools-apps/src/main/webapp/* $TOMCAT_WEBAPPS/iutools/
