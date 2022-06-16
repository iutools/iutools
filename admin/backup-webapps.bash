admin_lib_path=`realpath $(dirname "$0")/lib`

echo "*** backup-webapps.bash ***"
echo "Running with"
source $admin_lib_path/validate_TOMCAT_WEBAPPS.bash
echo

# Makes the bash script to print out every command before it is executed except echo
trap '[[ $BASH_COMMAND != echo* ]] && echo $BASH_COMMAND' DEBUG

OLD_WEBAPPS=$TOMCAT_WEBAPPS/iutools
date+"%y-%m-%d"
NOW=`date +%Y-%m-%d-%Hh%M`
BAK_WEBAPPS=$TOMCAT_WEBAPPS/iutools.bak$NOW

echo
echo "==="
echo "=== Backing up old iutools webapp"
echo "===    orig   : $OLD_WEBAPPS"
echo "===    backup : $BAK_WEBAPPS"
echo
cp -pr $OLD_WEBAPPS $BAK_WEBAPPS
