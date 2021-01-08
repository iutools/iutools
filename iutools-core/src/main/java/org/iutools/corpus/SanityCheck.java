package org.iutools.corpus;

import ca.nrc.dtrc.elasticsearch.*;
import ca.nrc.dtrc.elasticsearch.request.Query;
import ca.nrc.dtrc.elasticsearch.request._Source;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SanityCheck {

	private static final WordInfo winfoProto = new WordInfo();
	private static final String typeWinfo = "WordInfo_ES";
	private _Source idFieldOnly = new _Source("id");
	private static Query queryNonWinfoRecords = null;

	static {
		queryNonWinfoRecords = new Query();
			queryNonWinfoRecords
			.openAttr("exists")
			.openAttr("field")
			.setOpenedAttr("scroll")
			;
	}


	private Map<String,CompiledCorpus> corpora =
		new HashMap<String,CompiledCorpus>();

	private Map<String, Set<String>> alreadyLogged =
		new HashMap<String,Set<String>>();

	public SanityCheck(String[] corporaNames) throws CompiledCorpusException {
		for (String name: corporaNames) {
			try {
				CompiledCorpus corpus =
					new CompiledCorpusRegistry().getCorpus(name);
				corpora.put(name, corpus);
				alreadyLogged.put(corpus.getIndexName(), new HashSet<String>());
			} catch (CompiledCorpusRegistryException   e) {
				throw new CompiledCorpusException(e);
			}
		}

		return;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			usage();
		}

		SanityCheck checker = new SanityCheck(args);
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
			Logger tLogger = Logger.getLogger("org.iutools.corpus.SanityCheck.check");
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
				} else {
					tLogger.trace(callInfo + "Index does not yet exist. Nothing to check.");
				}
			} catch (ElasticSearchException e) {
				throw new CompiledCorpusException(e);
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
		System.out.println("Usage: SanityCheck corpus");
		System.exit(1);
	}

}
