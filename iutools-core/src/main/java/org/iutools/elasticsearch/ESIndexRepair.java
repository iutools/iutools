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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

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

	static {
		queryNonWinfoRecords = new Query();
			queryNonWinfoRecords
			.openAttr("exists")
			.openAttr("field")
			.setOpenedAttr("scroll")
			;
	}


	private Map<String, CompiledCorpus> corpora =
		new HashMap<String,CompiledCorpus>();

	private Map<String, Set<String>> alreadyLogged =
		new HashMap<String,Set<String>>();

	private Set<String> indicesWithCorruptedLastLoadeDate =
		new HashSet<String>();
	
	public ESIndexRepair(String _indexName) {
		this.indexName = _indexName;
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
				Thread.sleep(1*1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

		private void check(CompiledCorpus corpus) throws CompiledCorpusException {
			Logger tLogger = Logger.getLogger("org.iutools.corpus.ESIndexRepair.check");
			tLogger.setLevel(Level.ALL);
			check(corpus, tLogger, (String)null, (URL)null, (String)null);
		}

		private void check(CompiledCorpus corpus, Logger tLogger, String callInfo,
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
						logNewBadRecord(tLogger, badRecords, callInfo, corpus.esClient(),
							typeWinfo, url, json);
					} else {
//						tLogger.trace(callInfo + "Index is fine");
					}
					try {
						corpus.lastLoadedDate();
					} catch (Exception e) {
						logCorruptedLastLoadeDate(tLogger, e, esClient);
					}
				} else {
					tLogger.trace(callInfo + "Index does not yet exist. Nothing to check.");
				}
			} catch (ElasticSearchException e) {
				throw new CompiledCorpusException(e);
			}
	}

	private void logCorruptedLastLoadeDate(Logger tLogger, Exception exc,
		StreamlinedClient esClient) {
		String indexName = esClient.getIndexName();
		if (!indicesWithCorruptedLastLoadeDate.contains(indexName)) {
			indicesWithCorruptedLastLoadeDate.add(indexName);
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("The last loaded date type was corrupted for index: " +
				indexName + "\n" +
				"Exception: " + exc.getCause().toString());
			}
		}
	}

	private void logNewBadRecord(
		Logger tLogger, SearchResults<WordInfo> badRecords,
		String callInfo, StreamlinedClient esClient, String typeWinfo, URL url, String json) {
		if (tLogger.isTraceEnabled()) {
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
					tLogger.trace(mess);
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

		Query query = new Query();
		query
			.openAttr("exists")
			.openAttr("field")
			.setOpenedAttr("scroll_id")
		;
		_Source source = new _Source("id");
		SearchResults<Document> results =
			esClient().search(query, esType, goodDocPrototype, source);

		return results.docIDIterator();
	}

	public void repairCorruptedDocs(
		Iterator<String> corruptedIDsIter, String esTypeName, Document goodDocProto, Path jsonFile) throws ElasticSearchException {
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
}
