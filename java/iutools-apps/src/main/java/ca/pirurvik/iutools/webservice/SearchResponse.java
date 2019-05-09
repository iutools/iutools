package ca.pirurvik.iutools.webservice;

import java.util.List;

public class SearchResponse extends ServiceResponse {
	public String expandedQuery;
	public Long totalHits;
	public List<SearchHit> hits;
}
