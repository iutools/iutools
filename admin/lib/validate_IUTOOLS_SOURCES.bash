if [ -z ${IUTOOLS_SOURCES} ]; then
    echo "The IUTOOLS_SOURCES environment variable must be set to the root of your iutools project"
    exit 1
fi
echo "   IUTOOLS_SOURCES=IUTOOLS_SOURCES"