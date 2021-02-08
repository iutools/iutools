if [ -z ${JAVAUTILS_SOURCES} ]; then
  export JAVAUTILS_SOURCES=`realpath ${IUTOOLS_SOURCES}/../java-utils`
  if [! -d $JAVAUTILS_SOURCES]; then
    echo "JAVAUTILS_SRC environment variable not set. It should point to the root of the java-utils sources" 1>&2
    exit 1
  fi
fi
echo "   JAVAUTILS_SOURCES=$JAVAUTILS_SOURCES"
