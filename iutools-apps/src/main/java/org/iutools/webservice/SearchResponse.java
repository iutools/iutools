package org.iutools.webservice;

import java.util.List;

import org.iutools.search.SearchHit;

public class SearchResponse extends ServiceResponse {
	public String expandedQuery;
	public List<String> expandedQueryWords;
	public Long totalHits;
	public List<SearchHit> hits;
}
