package org.iutools.concordancer;

/**
 * A basic Translation Memory that uses ElasticSearch
 */
public class TranslationMemory {

	private String indexName = null;

	public TranslationMemory() {
		init_TranslationMemory((String)null);
	}

	public TranslationMemory(String _indexName) {
		init_TranslationMemory(_indexName);
	}

	private void init_TranslationMemory(String _indexName) {
		this.indexName = _indexName;
	}
}
