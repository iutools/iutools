package ca.pirurvik.iutools.corpus;

import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.dtrc.elasticsearch.SearchResults;
import ca.nrc.dtrc.elasticsearch.StreamlinedClientObserver;
import ca.nrc.dtrc.elasticsearch.request.Query;

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
					.setOpenedAttr("highlight")
			;
	};

	protected void checkForBadRecords(String when, URL url, String json)
		throws ElasticSearchException {
		if (esClient().indexExists()) {
			String indexName = esClient().getIndexName();
			SearchResults<WordInfo> badRecords =
				esClient().search(
				"", typeWinfo, winfoProto, queryNonWinfoRecords);
			if (badRecords.getTotalHits() > 0) {
				throw new ElasticSearchException(
				when + ": There were some bad records in type '" + typeWinfo + "' of index '" +
				indexName + "'.\n   URL was  : " + url + "\n   Json was : " + json);
			}
		}
	}

	@Override
	protected void beforePUT(URL url, String json) throws ElasticSearchException {
		checkForBadRecords("before PUT", url, json);
	}

	@Override
	protected void afterPUT(URL url, String json) throws ElasticSearchException {
		checkForBadRecords("after PUT", url, json);
	}

	@Override
	protected void onBulkIndex(int i, int i1, String json, String s1) {}

	@Override
	protected void beforePOST(URL url, String json) {}

	@Override
	protected void afterPOST(URL url, String json) {}

	@Override
	protected void beforeGET(URL url) {}

	@Override
	protected void afterGET(URL url) {}

	@Override
	protected void beforeDELETE(URL url, String json) {}

	@Override
	protected void afterDELETE(URL url, String json) {}
}
