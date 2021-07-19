package org.iutools.concordancer.tm;

import ca.nrc.dtrc.elasticsearch.DocIterator;
import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.dtrc.elasticsearch.SearchResults;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.nrc.ui.commandline.UserIO;
import org.apache.log4j.Logger;
import org.iutools.concordancer.Alignment_ES;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/**
 * A basic Translation Memory that uses ElasticSearch
 */
public class TranslationMemory {
	public static final String ES_ALIGNMENT_TYPE = "SentencePair";

	public static final String DEFAULT_TM_NAME = "iutools_tm";

	private String indexName = null;
	private StreamlinedClient _esClient = null;
	private UserIO userIO = new UserIO().setVerbosity(UserIO.Verbosity.Level0);

	public TranslationMemory() {
		init_TranslationMemory(DEFAULT_TM_NAME);
	}

	public TranslationMemory(String _indexName) {
		init_TranslationMemory(_indexName);
	}

	private void init_TranslationMemory(String _indexName) {
		this.indexName = _indexName;
	}

	public void loadFile(Path tmFile) throws TranslationMemoryException {
		try {
			esClient().bulkIndex(tmFile.toString(), ES_ALIGNMENT_TYPE);
		} catch (ElasticSearchException e) {
			throw new TranslationMemoryException(
				"Problem loading file into translation memory '"+indexName+
				" ("+tmFile+")", e);
		}
	}

	protected StreamlinedClient esClient() throws TranslationMemoryException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.esClient");
		if (_esClient == null) {
			try {
				_esClient =
					new StreamlinedClient(indexName)
						.setSleepSecs(0.0);
				_esClient.setUserIO(this.userIO);
				// 2021-01-10: Setting this to false should speed things up, but it may corrupt
				// the ES index.
//				_esClient.synchedHttpCalls = false;
				_esClient.synchedHttpCalls = true;
			} catch (ElasticSearchException e) {
				throw new TranslationMemoryException(e);
			}
		}

		return _esClient;
	}

	public TranslationMemory setUserIO(UserIO _userIO) {
		this.userIO = _userIO;
		this._esClient = null;
		return this;
	}

	public void addAlignment(Alignment_ES alignment)
		throws TranslationMemoryException {
		try {
			esClient().putDocument(ES_ALIGNMENT_TYPE, alignment);
		} catch (ElasticSearchException e) {
			throw new TranslationMemoryException(e);
		}
	}

	public List<Alignment_ES> search(String sourceLang, String sourceExpr,
		String... targetLangs) throws TranslationMemoryException {

		List<Alignment_ES> alignments = new ArrayList<Alignment_ES>();

		String freeformQuery = "sentences."+sourceLang+":\""+sourceExpr+"\"";
		SearchResults<Alignment_ES> searchResult = null;
		try {
			searchResult = esClient().search(freeformQuery, ES_ALIGNMENT_TYPE, new Alignment_ES());
			for (
				DocIterator<Alignment_ES> it = searchResult.docIterator(); it.hasNext(); ) {
				Alignment_ES aHit = it.next();
				alignments.add(aHit);
			}
		} catch (ElasticSearchException e) {
			throw new TranslationMemoryException(e);
		}

		return alignments;
	}
}
