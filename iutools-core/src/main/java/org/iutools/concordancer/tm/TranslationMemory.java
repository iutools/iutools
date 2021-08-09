package org.iutools.concordancer.tm;

import ca.nrc.dtrc.elasticsearch.DocIterator;
import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.dtrc.elasticsearch.SearchResults;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.nrc.ui.commandline.UserIO;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.log4j.Logger;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.script.TransCoder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A basic Translation Memory that uses ElasticSearch
 */
public class TranslationMemory {
	public static final String ES_ALIGNMENT_TYPE = "Alignment";

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
		try {
			Iterator<Alignment_ES> iter = searchIter(sourceLang,sourceExpr, targetLangs);
			while (iter.hasNext()) {
				alignments.add(iter.next());
			}
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}

		return alignments;
	}

	private Iterator<Alignment_ES> searchIter(String sourceLang, String sourceExpr, String[] targetLangs) throws TranslationMemoryException {
		List<Iterator<Alignment_ES>> iterators =
			new ArrayList<Iterator<Alignment_ES>>();
		try {
			String[] sourceExprVariants = new String[]{sourceExpr};
			if (sourceLang.equals("iu")) {
				// For iu, try the search with both scripts.
				// Some of the TMs use roman while others use syllabic
				sourceExprVariants = new String[]{
				TransCoder.ensureScript(TransCoder.Script.SYLLABIC, sourceExpr),
				TransCoder.ensureScript(TransCoder.Script.ROMAN, sourceExpr),
				};
			}

			for (String expr: sourceExprVariants) {
				String freeformQuery = "sentences."+sourceLang+":\""+expr+"\"";
				SearchResults<Alignment_ES> searchResult = null;
				try {
					searchResult = esClient()
						.search(freeformQuery, ES_ALIGNMENT_TYPE, new Alignment_ES());
					iterators.add(searchResult.docIterator());
				} catch (ElasticSearchException e) {
					throw new TranslationMemoryException(e);
				}
			}
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}

		Iterator<Alignment_ES> iterator = null;
		if (iterators.size() == 1) {
			iterator = iterators.get(0);
		} else {
			iterator =
				new IteratorChain<Alignment_ES>(iterators.get(0), iterators.get(1));
		}

		return iterator;
	}

	public void deleteIndex() throws TranslationMemoryException {
		try {
			esClient().deleteIndex();
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}
	}
}
