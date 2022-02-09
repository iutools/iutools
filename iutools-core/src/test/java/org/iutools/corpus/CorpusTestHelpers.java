package org.iutools.corpus;

import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import org.iutools.elasticsearch.ES;

public class CorpusTestHelpers {
    public static final String ES_TEST_INDEX = "iutools-test-index";

    public static void clearESTestIndex() throws Exception {
		 ES.makeFactory(ES_TEST_INDEX).indexAPI().clear(false);
    }

	public static void deleteCorpusIndex(String indexName) throws ElasticSearchException {
		ES.makeFactory(indexName).indexAPI().delete();
	}
}
