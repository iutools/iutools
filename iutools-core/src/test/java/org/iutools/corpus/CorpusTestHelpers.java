package org.iutools.corpus;

import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;

import java.io.IOException;

public class CorpusTestHelpers {
    public static final String ES_TEST_INDEX = "iutools-test-index";

    public static void clearESTestIndex() throws Exception {
        StreamlinedClient esClient = new StreamlinedClient(ES_TEST_INDEX);
        esClient.clearIndex(false);
    }
}
