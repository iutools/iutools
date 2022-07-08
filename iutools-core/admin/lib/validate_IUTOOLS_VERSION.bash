if [ -z ${IUTOOLS_VERSION} ]; then
    echo "IUTOOLS_VERSION environment variable not set.It should be set to the version of iutools we want to redeploy." 1>&2
    exit 1
fi
echo "   IUTOOLS_VERSION=${IUTOOLS_VERSION}"