package org.iutools.corpus;

import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.dtrc.elasticsearch.SearchResults;
import ca.nrc.dtrc.elasticsearch.StreamlinedClientObserver;
import ca.nrc.dtrc.elasticsearch.request.Query;
import ca.nrc.json.PrettyPrinter;
import org.apache.log4j.Logger;

import java.net.URL;

/**
 * For some strange reason, the ES index sometimes ends up having a
 * a WordInfo record that conatains the json of an ES request instead of the
 * json of an actual WordInfo. This ends up raising an exception when we
 * try to deserialize that record to a WordInfo.
 *
 * This ES observer is designed to monitor the index after every transaction
 * and make sure that no record has been added that does not correspond to an
 * actual WordInfo
 */
public class ObsEnsureAllRecordsAreWordInfo extends StreamlinedClientObserver {
	private static final WordInfo winfoProto = new WordInfo();
	private static final String typeWinfo = "WordInfo_ES";
	private static Query queryNonWinfoRecords = null;

	static {
		queryNonWinfoRecords = new Query();
			queryNonWinfoRecords
			.openAttr("exists")
			.openAttr("field")
			.setOpenedAttr("scroll")
		;
	}

	;

	protected void checkForBadRecords(Logger tLogger, URL url)
		throws ElasticSearchException {
		checkForBadRecords(tLogger, url, (String)null);
	}

	protected void checkForBadRecords(Logger tLogger, URL url, String json)
		throws ElasticSearchException {
		String indexName = esClient().getIndexName();
		String[] loggerName = tLogger.getName().split("\\.");
		String when = loggerName[loggerName.length-1];
		String callInfo =
			"[index="+indexName+"; when="+when+"; url="+url+"]: ";

		if (esClient().indexExists()) {
			tLogger.trace(callInfo+"Checking for bad records.");
			SearchResults<WordInfo> badRecords =
				esClient().search(
				"", typeWinfo, winfoProto, queryNonWinfoRecords);
			long numBad = badRecords.getTotalHits();
			if (badRecords.getTotalHits() > 0) {
				if (tLogger.isTraceEnabled()) {
					tLogger.trace(callInfo+"Found " + numBad + " bad records in index ");
					tLogger.trace(callInfo+"First bad record is:"+ PrettyPrinter.print(badRecords.docIterator().next()));
				}
				throw new ElasticSearchException(
					when + ": There were some bad records in type '" + typeWinfo + "' of index '" +
					indexName + "'.\n   URL was  : " + url + "\n   Json was : " + json);
			} else {
				tLogger.trace(callInfo+"Index is fine");
			}
		} else {
			tLogger.trace(callInfo+"Index does not yet exist. Nothing to check.");
		}
	}

	@Override
	protected void beforePUT(URL url, String json) throws ElasticSearchException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.ObsEnsureAllRecordsAreWordInfo.beforePUT");
		checkForBadRecords(tLogger, url, json);
	}

	@Override
	protected void afterPUT(URL url, String json) throws ElasticSearchException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.ObsEnsureAllRecordsAreWordInfo.afterPUT");
		checkForBadRecords(tLogger, url, json);
	}

	@Override
	protected void onBulkIndex(int i, int i1, String json, String s1) {}

	@Override
	protected void beforePOST(URL url, String json) {}

	@Override
	protected void afterPOST(URL url, String json) {}

	@Override
	protected void beforeGET(URL url) throws ElasticSearchException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.ObsEnsureAllRecordsAreWordInfo.beforeGET");
		checkForBadRecords(tLogger, url);
	}

	@Override
	protected void afterGET(URL url) throws ElasticSearchException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.ObsEnsureAllRecordsAreWordInfo.afterGET");
		checkForBadRecords(tLogger, url);
	}

	@Override
	protected void beforeDELETE(URL url, String json) {}

	@Override
	protected void afterDELETE(URL url, String json) {}
}
