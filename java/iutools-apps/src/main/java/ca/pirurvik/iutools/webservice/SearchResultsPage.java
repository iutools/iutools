package ca.pirurvik.iutools.webservice;


import java.util.List;

import ca.pirurvik.iutools.search.SearchHit;

public class SearchResultsPage {
	public Integer bingPageNum = null;
	public Integer estTotalHits = 0;
	public Boolean hasNext = null;
	public List<SearchHit> hitsCurrPage = null;
	public int hitsPerPage = 10;
	public int pageNum = 0;
	public String query = null;
	public List<String> queryTerms = null;
	public String[] urlsAllPreviousHits = new String[0];
}
