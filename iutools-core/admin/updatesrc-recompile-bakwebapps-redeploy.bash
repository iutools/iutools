admin_path=`realpath $(dirname "$0")`

# Makes the bash script to print out every command before it is executed except echo
trap '[[ $BASH_COMMAND != echo* ]] && echo $BASH_COMMAND' DEBUG

source $admin_path/updatesrc.bash
echo
echo
source $admin_path/recompile.bash
echo
echo
source $admin_path/backup-webapps.bash
echo
echo
source $admin_path/redeploy-webapps.bash