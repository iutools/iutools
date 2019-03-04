package ca.pirurvik.iutools.webservice;

import java.util.List;

public class IUTSSearchEndpointResults extends IUTServiceResults {
	public String expandedQuery;
	public Long totaHits;
	public List<IUTSSearchHit> hits;
}
