package org.iutools.concordancer.tm;

import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.dtrc.elasticsearch.SearchResults;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.nrc.dtrc.elasticsearch.request.Query;
import ca.nrc.ui.commandline.UserIO;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.log4j.Logger;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.script.TransCoder;
import org.json.JSONArray;
import org.json.JSONObject;

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
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.tm.TranslationMemory.search");
		List<Alignment_ES> alignments = new ArrayList<Alignment_ES>();
		try {
			Iterator<Alignment_ES> iter = searchIter(sourceLang,sourceExpr, targetLangs);
			while (iter.hasNext()) {
				Alignment_ES alignment = iter.next();
				if (tLogger.isTraceEnabled()) {
					String hasOrNot = "HAS";
					if (alignment.walign4langpair == null ||
						alignment.walign4langpair.isEmpty()) {
						hasOrNot = "HAS NO";
					}
					tLogger.trace("Alignment "+hasOrNot+" word level alignments");
				}
				alignments.add(alignment);
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

			for (boolean withAlignment: new boolean[] {true, false}) {
				// First look for alignments that have word-level alignments
				for (String expr: sourceExprVariants) {
					// Then possibly search for syllabics and roman variants
					Query query = esQuery(sourceLang, expr, withAlignment);
					SearchResults<Alignment_ES> searchResult = null;
					try {
						searchResult = esClient()
							.search(query, ES_ALIGNMENT_TYPE, new Alignment_ES());
						iterators.add(searchResult.docIterator());
					} catch (ElasticSearchException e) {
						throw new TranslationMemoryException(e);
					}
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

	private Query esQuery(
		String sourceLang, String expr, boolean withAlignment) {

		String freeformQuery = "sentences." + sourceLang + ":\"" + expr + "\"";
		JSONArray must = new JSONArray();
		must.put(
			new JSONObject().put("query_string",
				new JSONObject().put("query", freeformQuery))
		);

		if (withAlignment) {
			must.put(new JSONObject()
				.put("exists", new JSONObject()
					.put("field", "walign4langpair"))
			);
		}

		JSONObject jObj = new JSONObject()
			.put("bool", new JSONObject()
				.put("must", must)
			);

		Query query = new Query(jObj);

		return query;
	}

	public void deleteIndex() throws TranslationMemoryException {
		try {
			esClient().deleteIndex();
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}
	}
}
