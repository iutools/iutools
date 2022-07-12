if [ -z ${IUTOOLS_DATA} ]; then
    echo "The IUTOOLS_DATA environment variable must be set to the root of your iutools project"
    exit 1
fi
echo "   IUTOOLS_DATA=$IUTOOLS_DATA"
