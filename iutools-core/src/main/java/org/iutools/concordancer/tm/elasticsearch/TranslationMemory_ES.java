package org.iutools.concordancer.tm.elasticsearch;


import ca.nrc.dtrc.elasticsearch.ESFactory;
import static ca.nrc.dtrc.elasticsearch.ESFactory.ESOptions;
import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.dtrc.elasticsearch.SearchResults;
import ca.nrc.dtrc.elasticsearch.request.Query;
import ca.nrc.ui.commandline.UserIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.datastructure.CloseableIteratorChain;
import org.iutools.datastructure.CloseableIteratorWrapper;
import org.iutools.elasticsearch.ES;
import org.iutools.script.TransCoder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.iutools.sql.CloseableIterator;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * ElasticSearch implementation of the TranslationMemory
 */
public class TranslationMemory_ES extends TranslationMemory {

	public static final String ES_ALIGNMENT_TYPE = "Alignment";
	private ESFactory _esFactory = null;

	public TranslationMemory_ES() {
		super();
	}

	public TranslationMemory_ES(String tmName) {
		super(tmName);
	}

	protected ESFactory esFactory() throws TranslationMemoryException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.esFactory");
		if (_esFactory == null) {
			try {
				_esFactory =
					ES.makeFactory(tmName)
					.setSleepSecs(0.0);
			} catch (ElasticSearchException e) {
				throw new TranslationMemoryException(e);
			}
			_esFactory.setUserIO(this.userIO);
			// 2021-01-10: Setting this to false should speed things up, but it may corrupt
			// the ES index.
//			_esFactory.synchedHttpCalls = false;
			_esFactory.synchedHttpCalls = true;
		}

		return _esFactory;
	}

	public void loadFile(Path tmFile, ESOptions... options) throws TranslationMemoryException {
		try {
			esFactory().indexAPI()
				.bulkIndex(tmFile.toString(), ES_ALIGNMENT_TYPE, options);
		} catch (ElasticSearchException e) {
			throw new TranslationMemoryException(
				"Problem loading file into translation memory '"+tmName+
				" ("+tmFile+")", e);
		}
	}

	@Override
	public TranslationMemory setUserIO(UserIO _userIO) {
		super.setUserIO(_userIO);
		this._esFactory = null;
		return this;
	}

	public void addAlignment(Alignment alignment)
		throws TranslationMemoryException {
		try {
			esFactory().crudAPI().putDocument(ES_ALIGNMENT_TYPE, alignment);
		} catch (ElasticSearchException e) {
			throw new TranslationMemoryException(e);
		}
	}

	public CloseableIterator<Alignment_ES> searchIter(
		String sourceLang, String sourceExpr, String... targetLangs) throws TranslationMemoryException {
		List<CloseableIterator<Alignment_ES>> iterators =
			new ArrayList<CloseableIterator<Alignment_ES>>();
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
						searchResult = esFactory().searchAPI()
							.search(query, ES_ALIGNMENT_TYPE, new Alignment_ES());
						iterators.add(new CloseableIteratorWrapper<Alignment_ES>(searchResult.docIterator()));
					} catch (ElasticSearchException e) {
						throw new TranslationMemoryException(e);
					}
				}
			}
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}

		CloseableIterator<Alignment_ES> iterator = null;
		if (iterators.size() == 1) {
			iterator = iterators.get(0);
		} else {
			iterator =
				new CloseableIteratorChain<Alignment_ES>(iterators.get(0), iterators.get(1));
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

	public void delete() throws TranslationMemoryException {
		try {
			esFactory().indexAPI().delete();
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}
	}

	public List<Alignment_ES> search(String sourceLang, String sourceExpr,
		String... targetLangs) throws TranslationMemoryException {
		Logger tLogger = LogManager.getLogger("org.iutools.concordancer.tm.TranslationMemory.search");
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

}
