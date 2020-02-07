package ca.pirurvik.iutools.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.nrc.datastructure.Pair;

/***********************************************************************************
 * This class captures the current status of a multi-page Bing Search for Inuktitut.
 ***********************************************************************************/

public class PageOfHits {
	
		// Query that this page of hits is supposed to meet
	public String query = "";
	
		// List of query terms that this page of hits is supposed to meet
	private String[] queryTerms = null;
	
		// Index of this page of hits
	private Integer pageNum = -1;
		public Integer getPageNum() { return pageNum; }
		public void incrPageNum() { 
			pageNum++;
			incrBingPageNum();
		}
//		public void setPageNum(Integer num) { bingPageNum = num; }
	
	
		// Index of the Bing page of hits.
		// If null, then same as pageNum
	private Integer bingPageNum = null;
		public Integer getBingPageNum() { 
			Integer num = pageNum;
			if (bingPageNum != null) { 
				num = bingPageNum;
			}
			return num;
		}
		public void incrBingPageNum() { setBingPageNum(getBingPageNum() + 1); }
		public void setBingPageNum(Integer num) { bingPageNum = num; }
	
		// True iif there is a next page of hits
	public boolean hasNext = true;
	
		// Number of hits to retrieve for each page
		// Actually, this is the number of hits to retrive per
		// query term
		//
	public Integer hitsPerPage = 10;
	
		// The hits retrieved with this page
	public List<SearchHit> hitsCurrPage = new ArrayList<SearchHit>();
	
		// Current best estimate of the total number of hits out there,
		// including those that have been returned in previous pages of hits
	public Long estTotalHits = new Long(0);

		// URL of hits that were returned in previous pages
	public Set<String> urlsAllPreviousHits = new HashSet<String>();
	
	public PageOfHits() {
		this.initialize(null);
	}
	
	public PageOfHits(String _query) {
		this.initialize(_query);
	}

	private void initialize(String _query) {
		this.query = _query;
	}

	public PageOfHits setHitsPageNum(int _hitsPageNum) {
		this.pageNum = _hitsPageNum;
		return this;
	}

	public PageOfHits setHitsPerPage(int _hitsPerPage) {
		this.hitsPerPage = _hitsPerPage;
		return this;
	}
	
	public PageOfHits addPreviousHitURLs(String[] urls) {
		for (String aURL: urls) {
			this.urlsAllPreviousHits.add(aURL);
		}
		return this;
	}
	
	public void addPreviousHitURLs(List<SearchHit> _hitsCurrPageList) {
		String[] hitsArr = new String[_hitsCurrPageList.size()];
		for (int ii=0; ii < hitsArr.length; ii++) {
			hitsArr[ii] = _hitsCurrPageList.get(ii).url;
		}
		addPreviousHitURLs(hitsArr);		
	}
	
	
	public PageOfHits setEstTotalHits(Long _estTotalHits) {
		this.estTotalHits = _estTotalHits;
		return this;
	}
	
	public PageOfHits setHitsCurrPage(List<SearchHit> _hitsCurrPage) {
		this.hitsCurrPage = _hitsCurrPage;
		return this;
	}
	
	@JsonIgnore
	public String[] getQueryTerms() {
		if (queryTerms == null) {
			List<Pair<String, Boolean>> tokens = ca.nrc.string.StringUtils.tokenizeNaively(query);
			
			List<String> termsList = new ArrayList<String>();
			for (Pair<String,Boolean> aToken: tokens) {
				Boolean isDelimiter = aToken.getSecond();
				String tokenStr = aToken.getFirst();
				if (!isDelimiter && !tokenStr.equals("OR")) {
					termsList.add(tokenStr);
					
				}
			}
			
			queryTerms = termsList.toArray(new String[termsList.size()]);			
		}
		
		
		return queryTerms;
	}

}
