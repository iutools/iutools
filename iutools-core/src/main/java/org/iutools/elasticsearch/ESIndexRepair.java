package org.iutools.elasticsearch;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import ca.nrc.dtrc.elasticsearch.*;
import ca.nrc.dtrc.elasticsearch.request.Query;
import ca.nrc.dtrc.elasticsearch.request._Source;
import ca.nrc.introspection.Introspection;
import ca.nrc.introspection.IntrospectionException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.iutools.corpus.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * As of 2020-10 or so, we noticed that concurrent invocations of the iutools
 * web services sometimes end up corrupting some of the documents in the ES
 * indices (most notably, the indices related to the CompiledCorpus).
 *
 * The ESIndexRepair class is designed to inspect an index for such faulty
 * documents and possibly repair them.
 */
public class ESIndexRepair {

	private static final WordInfo winfoProto = new WordInfo();
	private static final String typeWinfo = "WordInfo_ES";
	private String indexName = null;
	private _Source idFieldOnly = new _Source("id");
	private static Query queryNonWinfoRecords = null;

	private StreamlinedClient _esClient = null;

	private Logger logger = null;

	static {
		queryNonWinfoRecords = new Query(
			new JSONObject()
			.put("exists", new JSONObject()
				.put("field", "scroll")
			)
		);
	}


	private Map<String, CompiledCorpus> corpora =
		new HashMap<String,CompiledCorpus>();

	private Map<String, Set<String>> alreadyLogged =
		new HashMap<String,Set<String>>();

	private Set<String> indicesWithCorruptedLastLoadeDate =
		new HashSet<String>();

	public ESIndexRepair(String _indexName, Logger _logger) {
		init_ESIndexRepair(_indexName, _logger);
	}

	public ESIndexRepair(String _indexName) {
		init_ESIndexRepair(_indexName, null);
	}

	private void init_ESIndexRepair(String _indexName, Logger _logger) {
		this.indexName = _indexName;
		this.setLogger(_logger);
	}

	private void setLogger(Logger _logger) {
		if (_logger != null) {
			logger = _logger;
		} else {
			logger = Logger.getLogger(this.getClass());
		}
		Level level = logger.getLevel();
		if (level == null) {
			logger.setLevel(Level.INFO);
		}
		return;
	}

	public ESIndexRepair(String[] corporaNames) throws CompiledCorpusException {
		for (String name: corporaNames) {
			try {
				CompiledCorpus corpus =
					new CompiledCorpusRegistry().getCorpus(name, false, true);
				corpora.put(name, corpus);
				alreadyLogged.put(corpus.getIndexName(), new HashSet<String>());
			} catch (CompiledCorpusRegistryException e) {
				throw new CompiledCorpusException(e);
			}
		}

		return;
	}

	private StreamlinedClient  esClient() throws ElasticSearchException {
		if (_esClient == null) {
			_esClient = new StreamlinedClient(indexName);
		}
		return _esClient;
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			usage();
		}

		ESIndexRepair checker = new ESIndexRepair(args);
		checker.keepChecking();
	}

	private void keepChecking() throws CompiledCorpusException {
		while (true) {
			for (String corpName: corpora.keySet()) {
				CompiledCorpus corpus = corpora.get(corpName);
				check(corpus);
			}
			try {
				Thread.sleep(60*1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

		private void check(CompiledCorpus corpus) throws CompiledCorpusException {
			check(corpus, (String)null, (URL)null, (String)null);
		}

		private void check(CompiledCorpus corpus, String callInfo,
 			URL url, String json) throws CompiledCorpusException {
			if (callInfo == null) {
				callInfo = "";
			}
			try {
				StreamlinedClient esClient = corpus.esClient();
				String indexName = corpus.getIndexName();
				if (corpus.esClient().indexExists()) {
					SearchResults<WordInfo> badRecords =
						esClient.search(
							"", typeWinfo, winfoProto, queryNonWinfoRecords, idFieldOnly);
					long numBad = badRecords.getTotalHits();
					if (badRecords.getTotalHits() > 0) {
						logNewBadRecord(badRecords, callInfo, corpus.esClient(),
							typeWinfo, url, json);
					} else {
//						logger.trace(callInfo + "Index is fine");
					}
					try {
						corpus.lastLoadedDate();
					} catch (Exception e) {
						logCorruptedLastLoadeDate(e, esClient);
					}
				} else {
					logger.trace(callInfo + "Index does not yet exist. Nothing to check.");
				}
			} catch (ElasticSearchException e) {
				throw new CompiledCorpusException(e);
			}
	}

	private void logCorruptedLastLoadeDate(Exception exc,
		StreamlinedClient esClient) {
		String indexName = esClient.getIndexName();
		if (!indicesWithCorruptedLastLoadeDate.contains(indexName)) {
			indicesWithCorruptedLastLoadeDate.add(indexName);
			if (logger.isTraceEnabled()) {
				logger.trace("The last loaded date type was corrupted for index: " +
				indexName + "\n" +
				"Exception: " + exc.getCause().toString());
			}
		}
	}

	private void logNewBadRecord(
		SearchResults<WordInfo> badRecords,
		String callInfo, StreamlinedClient esClient, String typeWinfo, URL url, String json) {
		if (logger.isTraceEnabled()) {
			String indexName = esClient.getIndexName();
			DocIterator iter = badRecords.docIterator();
			Set<String> alreadyLoggedIDs = alreadyLogged.get(indexName);
			while (iter.hasNext()) {
				WordInfo badDoc = (WordInfo) iter.next();
				String badDocID = badDoc.id;
				if (!alreadyLoggedIDs.contains(badDocID)) {
					alreadyLoggedIDs.add(badDocID);
					String mess =
						callInfo + "New bad record appeard. Word='"+badDocID+"', type='"+typeWinfo+"', index='"+indexName+"'";
					if (url != null || json != null) {
						mess += "\nCaused by ElasticSearch request:\n";
						if (url != null) {
							mess += "   url: "+url+"\n";
						}
						if (json != null) {
							mess += "   json: "+json+"\n";
						}
					}
					logger.trace(mess);
				}
			}
		}
	}

	public static void usage() {
		System.out.println("Usage: ESIndexRepair corpus...");
		System.exit(1);
	}

	public Iterator<String> corruptedDocIDs(
		String esType, Document goodDocPrototype) throws ElasticSearchException {
		Iterator<String> iter = null;

		Query query = new Query(
			new JSONObject()
				.put("exists", new JSONObject()
					.put("field", "scroll_id")
				)
		);
		_Source source = new _Source("id");
		SearchResults<Document> results =
			esClient().search(query, esType, goodDocPrototype, source);

		return results.docIDIterator();
	}

	JSONObject queryCorruptedDocs(Set<String> badFields) {
		"hello".compareTo("world");
		List<String> badFieldsSorted =
			badFields.stream().collect(Collectors.toList());
		Collections.sort(badFieldsSorted, (f1, f2) -> {
			return f1.compareTo(f2);
		});

		JSONArray existFieldsArray = new JSONArray();
		for (String badField: badFieldsSorted) {
			existFieldsArray.put(new JSONObject().put("exist", badField));
		}
		JSONObject query = new JSONObject()
			.put("query", new JSONObject()
				.put("bool", new JSONObject()
					.put("should", existFieldsArray)
				)
			);

		return query;
	}

	public void repairCorruptedDocs(
		Iterator<String> corruptedIDsIter, String esTypeName, Document goodDocProto,
		Path jsonFile) throws ElasticSearchException {
		Set<String> corruptedIDs = new HashSet<String>();
		while (corruptedIDsIter.hasNext()) {
			corruptedIDs.add(corruptedIDsIter.next());
		}
		String errMess = "Could not repair corrupted ES docs from json file: "+jsonFile;
		ObjectStreamReader reader = null;
		try {
			reader = new ObjectStreamReader(jsonFile.toFile());
			Object obj = reader.readObject();
			while (obj != null) {
				if (obj instanceof Document) {
					Document doc =
					Introspection.downcastTo(goodDocProto.getClass(), obj);
					if (corruptedIDs.contains(doc.getId())) {
						esClient().putDocument(esTypeName, doc);
					}
				}
				obj = reader.readObject();
			}
			// Sleep a bit to allow the changes to propagate to all ES nodes
			Thread.sleep(1000);
		} catch (IOException | ClassNotFoundException | ObjectStreamReaderException
				| InterruptedException e) {
			throw new ElasticSearchException(errMess, e);
		}
	}

	public String[] badFieldNames(String esTypeName, Document goodDocPrototype) throws ElasticSearchException {
		Set<String> existingFields =
			new Index(indexName).getFieldTypes(esTypeName).keySet();

		Set<String> validFields = null;
		try {
			validFields = Introspection.publicFields(goodDocPrototype).keySet();
		} catch (IntrospectionException e) {
			throw new ElasticSearchException(e);
		}

		Set<String> badFields = new HashSet<String>();
		for (String field: existingFields) {
			if (!validFields.contains(field)) {
				badFields.add(field);
			}
		}


		return badFields.toArray(new String[0]);
	}

	public void deleteCorruptedDocs(Iterator<String> corruptedIDs, String esType)
		throws ElasticSearchException {

		while (corruptedIDs.hasNext()) {
			String id = corruptedIDs.next();
			esClient().deleteDocumentWithID(id, esType);
		}
	}
}
