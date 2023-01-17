package org.iutools.linguisticdata.index;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.elasticsearch.*;
import org.iutools.linguisticdata.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ElasticSearch index for all known morphemes
 */
public class MorphemeIndex {

	public ElasticSearchDB db = null;
	public ElasticsearchClient esClient = null;


	public MorphemeIndex() {
		init_MorphemeIndex((String)null);
	}

	public MorphemeIndex(String _dbName) {
		init_MorphemeIndex(_dbName);
	}

	private void init_MorphemeIndex(String _dbName) {
		try {
			this.db = new ElasticSearchDB(_dbName, new IUToolsESSchema());
		} catch (GenericESException e) {
			throw new MorphemeIndexException(e);
		}
		try {
			esClient = ClientPool.getClient();
		} catch (GenericESException e) {
			throw new MorphemeIndexException(e);
		}

		ensureIndexIsUpToDate();
		return;
	}

	private void ensureIndexIsUpToDate() throws MorphemeIndexException {
		Long lastLoadedOn = getIndexLastLoadDate();

		if (lastLoadedOn == null) {
			loadMorphemes();
			return;
		}
	}

	private void loadMorphemes() {
		Logger logger = LogManager.getLogger("org.iutools.linguisticdata.index.loadIndex");

		BulkRequest.Builder br = new BulkRequest.Builder();

		String indexName = db.index4docClass(MorphemeEntry.class);

		for (String morphID : LinguisticData.getInstance().allMorphemeIDs()) {
			try {
				MorphemeHumanReadableDescr descr =
						new MorphemeHumanReadableDescr(morphID);
				MorphemeEntry morphEntry = new MorphemeEntry(descr);
				br.operations(op -> op
					.index(idx -> idx
						.index(indexName)
						.id(morphEntry.getId())
						.document(morphEntry)
					)
				);
				int x = 1;
			} catch (MorphemeException e) {
				throw new MorphemeIndexException(e);
			}
		}

		BulkResponse result = null;
		try {
			BulkRequest request = br.build();
			String json = ESUtils.toJson(request);
			result = esClient.bulk(request);
		} catch (IOException e) {
			throw new MorphemeIndexException(e);
		}

		// Log errors, if any
		if (result.errors() && logger.isErrorEnabled()) {
			logger.error("Bulk indexing of morpheme description raised some errors");
			for (BulkResponseItem item : result.items()) {
				if (item.error() != null) {
					logger.error(item.error().reason());
				}
			}
		}

		setIndexLastLoadDate(System.currentTimeMillis());
	}

	private List<MorphemeHumanReadableDescr> morphemeDescriptions() {
		List<MorphemeHumanReadableDescr> descriptions = new ArrayList<MorphemeHumanReadableDescr>();
		for (String morphID : LinguisticData.getInstance().allMorphemeIDs()) {
			try {
				MorphemeHumanReadableDescr descr = new MorphemeHumanReadableDescr(morphID);
				descriptions.add(descr);
			} catch (MorphemeException e) {
				throw new MorphemeIndexException(e);
			}
		}
		return descriptions;
	}

	private void setIndexLastLoadDate(long date) {
		DateLastLoaded loadDateEntry = new DateLastLoaded("morphemes", date);
		try {
			IndexResponse response = esClient.index(i -> i
				.index(db.index4doc(loadDateEntry))
				.id(loadDateEntry.getId())
				.document(loadDateEntry));
			int x = 1;
		} catch (IOException e) {
			throw new MorphemeIndexException(e);
		}
		return;
	}

	private Long getIndexLastLoadDate() {

		String indexName = db.index4docClass(DateLastLoaded.class);
		Long lastLoaded = null;
		try {
			GetResponse<DateLastLoaded> response = esClient.get(g -> g
				.index(indexName)
				.id("morphemes"),
				DateLastLoaded.class
			);

			if (response.found()) {
				lastLoaded = response.source().timestamp;
			}
		} catch (ElasticsearchException e) {
			// Ignore index_not_found_exception exceptions. They just mean
			// that no resources have been loaded ever.
			// So leave lastLoaded at null.
			// Otherwise rethrow the exception.
			//
			if (!e.error().type().equals("index_not_found_exception")) {
				throw e;
			}
		} catch (IOException e) {
			throw new MorphemeIndexException(e);
		}
		return lastLoaded;
	}



	public static void main(String[] args) throws Throwable {
		try {
			MorphemeIndex index = new MorphemeIndex();
			index.loadMorphemes();
		} finally {
			ClientPool.closeAll();
		}

		return;
	}

	public List<Morpheme> searchMorphemes(String _canonicalForm,
		String _grammar, String _meaning) throws ElasticsearchException, GenericESException {
		Logger logger = LogManager.getLogger("org.iutools.linguisticdata.index.MorphemeIndex.searchMorphemes");
		List<Morpheme> morphemes = new ArrayList<Morpheme>();
		InputStream jsonQueryStream = makeMorphemeQuery(_canonicalForm, _grammar, _meaning);
		String indexName = db.index4docClass(MorphemeEntry.class);
		SearchRequest sr = SearchRequest.of(s -> s
			.index(indexName)
			.query(q -> q
					.withJson(jsonQueryStream)
			)
			.size(100)
		);

		String srJson = ESUtils.toJson(sr);
		logger.trace("srJson="+srJson);

		try {
			SearchResponse<MorphemeEntry> response = esClient.search(sr, MorphemeEntry.class);
			List<MorphemeEntry> entries = new SearchResponseWrapper<MorphemeEntry>(response).documents();
			for (MorphemeEntry entry: entries) {
				Morpheme morpheme = Morpheme.getMorpheme(entry.descr.id);
				morphemes.add(morpheme);
			}
		} catch (IOException | LinguisticDataException e) {
			throw new GenericESException(e);
		}
		return morphemes;
	}

	private InputStream makeMorphemeQuery(String canonicalForm, String grammar, String meaning) {

		String canonicalChars = MorphemeEntry.canonicalForm2SpaceDelimitedChars(canonicalForm);

		JSONArray mustArr = ESUtils.mustMatchFields(
//			Pair.of("descr.canonicalForm", canonicalForm),
			Pair.of("descr.grammar", grammar),
			Pair.of("descr.meaning", meaning)
		);
		if (canonicalForm != null) {
			mustArr.put(new JSONObject()
				.put("wildcard", new JSONObject()
					.put("descr.canonicalForm", new JSONObject()
						.put("value", canonicalForm + "*")
					)
				)
			);
		}

		JSONObject queryJObj = new JSONObject()
			.put("bool", new JSONObject()
				.put("must", mustArr)
			)
		;

		String queryJsonStr = queryJObj.toString();

		InputStream queryIS = new ByteArrayInputStream(queryJsonStr.getBytes(StandardCharsets.UTF_8));

		return queryIS;
	}
}
