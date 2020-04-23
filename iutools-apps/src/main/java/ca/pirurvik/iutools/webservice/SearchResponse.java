package ca.pirurvik.iutools.webservice;

import java.util.List;

import ca.pirurvik.iutools.search.SearchHit;

public class SearchResponse extends ServiceResponse {
	public String expandedQuery;
	public List<String> expandedQueryWords;
	public Long totalHits;
	public List<SearchHit> hits;
}
