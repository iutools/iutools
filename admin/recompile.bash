admin_lib_path=`realpath $(dirname "$0")/lib`

echo
echo "*** recompile.bash ***"
echo "Running with"
source $admin_lib_path/validate_IUTOOLS_SOURCES.bash
source $admin_lib_path/validate_JAVAUTILS_SOURCES.bash
echo

# Makes the bash script to print out every command before it is executed except echo
trap '[[ $BASH_COMMAND != echo* ]] && echo $BASH_COMMAND' DEBUG

echo
echo "==="
echo "=== Recompiling iutools sources at: "
echo "===    $IUTOOLS_SOURCES"
echo "==="
echo
cd $IUTOOLS_SOURCES
sudo mvn install -skipTests=true

echo
echo "==="
echo "=== Recompiling java-utils sources at: "
echo "===    $JAVAUTILS_SOURCES"
echo "==="
echo
cd $JAVAUTILS_SOURCES
sudo mvn install -skipTests=true


