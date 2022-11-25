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
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.datastructure.CloseableIteratorChain;
import org.iutools.elasticsearch.ES;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ca.nrc.datastructure.CloseableIterator;
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

	@Override
	public void removeAligmentsFromDoc(String docID) throws CompiledCorpusException {
		// TODO: IMPLEMENT THIS
	}

	public CloseableIterator<Alignment> search(
		String sourceLang, String[] sourceExprVariants, String targetLang)
		throws TranslationMemoryException {
		return search(sourceLang, sourceExprVariants, targetLang, (String[])null);
	}

//	public CloseableIterator<Alignment> search__OLD(
//		String sourceLang, String[] sourceExprVariants, String targetLang,
//		String[] withTranslationAmong) throws TranslationMemoryException {
//		List<CloseableIterator<Alignment>> iterators =
//			new ArrayList<CloseableIterator<Alignment>>();
//		try {
//			for (boolean withAlignment: new boolean[] {true, false}) {
//				// First look for alignments that have word-level alignments
//				for (String expr: sourceExprVariants) {
//					// Then possibly search for syllabics and roman variants
//					Query query = esQuery(sourceLang, expr, withAlignment, targetLang, withTranslationAmong);
//					SearchResults<Alignment> searchResult = null;
//					try {
//						searchResult = esFactory().searchAPI()
//							.search(query, ES_ALIGNMENT_TYPE, new Alignment());
//
//						CloseableIterator<Alignment> blah = (CloseableIterator<Alignment>) searchResult.docIterator();
//						iterators.add(searchResult.docIterator());
//					} catch (ElasticSearchException e) {
//						throw new TranslationMemoryException(e);
//					}
//				}
//			}
//		} catch (Exception e) {
//			throw new TranslationMemoryException(e);
//		}
//
//		CloseableIterator<Alignment> iterator = null;
//		if (iterators.size() == 1) {
//			iterator = iterators.get(0);
//		} else {
//			iterator =
//				new CloseableIteratorChain<Alignment>(iterators.get(0), iterators.get(1));
//		}
//
//		return iterator;
//	}

	public CloseableIterator<Alignment> search(
		String sourceLang, String[] sourceExprVariants, String targetLang,
		String[] withTranslationAmong) throws TranslationMemoryException {
		List<CloseableIterator<Alignment>> iterators =
			new ArrayList<CloseableIterator<Alignment>>();
		try {
			for (boolean withAlignment: new boolean[] {
				// Fist, look for alignments that have word-level alignments
				true,
				// Then, look for alignments that DON'T have word-level alignments
				false}) {

					// Then possibly search for syllabics and roman variants
					Query query = esQuery(sourceLang, sourceExprVariants, withAlignment, targetLang, withTranslationAmong);
					SearchResults<Alignment> searchResult = null;
					try {
						searchResult = esFactory().searchAPI()
							.search(query, ES_ALIGNMENT_TYPE, new Alignment());

						CloseableIterator<Alignment> blah = (CloseableIterator<Alignment>) searchResult.docIterator();
						iterators.add(searchResult.docIterator());
					} catch (ElasticSearchException e) {
						throw new TranslationMemoryException(e);
					}
			}
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}

		CloseableIterator<Alignment> iterator = null;
		if (iterators.size() == 1) {
			iterator = iterators.get(0);
		} else {
			iterator =
				new CloseableIteratorChain<Alignment>(iterators.get(0), iterators.get(1));
		}

		return iterator;
	}

	private Query esQuery(
		String sourceLang, String sourceExpr, boolean withAlignment) {
		return esQuery(sourceLang, new String[] {sourceExpr}, withAlignment, (String)null, (String[])null);
	}


	private Query esQuery__OLD(
		String sourceLang, String sourceExpr, boolean withAlignment, String targetLang,
		String[] withTranslationsAmong) {

		String freeformQuery = "+sentences." + sourceLang + ":\"" + sourceExpr + "\"";
		if (targetLang != null && withTranslationsAmong != null) {
			freeformQuery += " +sentences." + targetLang + ":\"" + withTranslationsAmong + "\"";
		}
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

	private Query esQuery(
		String sourceLang, String[] sourceExprVariants, boolean withAlignment, String targetLang,
		String[] withTranslationsAmong) {

//		String freeformQuery = "+sentences." + sourceLang + ":\"" + sourceExprVariants + "\"";
//		if (targetLang != null && withTranslationsAmong != null) {
//			freeformQuery += " +sentences." + targetLang + ":\"" + withTranslationsAmong + "\"";
//		}
//
//		JSONArray must = new JSONArray();
//		must.put(
//			new JSONObject().put("query_string",
//				new JSONObject().put("query", freeformQuery))
//		);
//
//		if (withAlignment) {
//			must.put(new JSONObject()
//				.put("exists", new JSONObject()
//					.put("field", "walign4langpair"))
//			);
//		}
//
//		JSONObject jObj = new JSONObject()
//			.put("bool", new JSONObject()
//				.put("must", must)
//			);

		JSONArray mustClauses = new JSONArray();
		mustClauses.put(
			// Clause for ensuring we have at least one of the source
			// expression variants
			esQueryClauses_mustHaveOneOfVariants(sourceLang, sourceExprVariants)
		);
		if (withTranslationsAmong != null) {
			// Clause for ensuring we have at least one of the translation
			// variants
			mustClauses.put(
				esQueryClauses_mustHaveOneOfVariants(targetLang, withTranslationsAmong)
			);
		}

		JSONObject queryJObj = new JSONObject()
			.put("bool", new JSONObject()
				.put("must", mustClauses)
			)
		;
		Query query = new Query(queryJObj);


		return query;
	}

	private JSONObject esQueryClauses_mustHaveOneOfVariants(String expLang, String[] exprVariants) {
		JSONArray shouldClauses = new JSONArray();
		for (String aVariant: exprVariants) {
			shouldClauses.put(new JSONObject()
				.put("query_string", new JSONObject()
					.put("query", "+sentences." + expLang + ":\"" + aVariant + "\"")
				)
			);
		}

		JSONObject jobj = new JSONObject()
			.put("bool", new JSONObject()
				.put("should", shouldClauses)
			);

		return jobj;
	}

	public void delete() throws TranslationMemoryException {
		try {
			esFactory().indexAPI().delete();
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}
	}
}
