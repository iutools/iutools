package ca.inuktitutcomputing.iutools.webservice;

import java.util.List;

public class SearchResponse extends ServiceResponse {
	public String expandedQuery;
	public Long totaHits;
	public List<SearchHit> hits;
}
