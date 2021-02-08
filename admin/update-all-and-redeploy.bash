admin_path=`realpath $(dirname "$0")`

# Makes the bash script to print out every command before it is executed except echo
trap '[[ $BASH_COMMAND != echo* ]] && echo $BASH_COMMAND' DEBUG

bash $admin_path/update-all.bash
echo
echo
bash $admin_path/redeploy-webapps.bash