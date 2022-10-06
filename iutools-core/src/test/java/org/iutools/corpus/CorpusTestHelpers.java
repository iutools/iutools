package org.iutools.corpus;

import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import org.iutools.elasticsearch.ES;

public class CorpusTestHelpers {
    public static final String ES_TEST_INDEX = "iutools-test-index";

    public static void clearESTestIndex() throws Exception {
//		 ES.makeFactory(ES_TEST_INDEX).indexAPI().clear(false);
		 clearCorpus(ES_TEST_INDEX);
    }

	public static void deleteCorpusIndex(String indexName) throws Exception {
//		ES.makeFactory(indexName).indexAPI().delete();
		clearCorpus(indexName);
	}

	public static void clearCorpus(String corpusName) throws Exception {
    	CompiledCorpus corpus = CompiledCorpusRegistry.makeCorpus(corpusName);
		corpus.deleteAll(true);
		corpus.changeLastUpdatedHistory(new Long(0));
	}
}
