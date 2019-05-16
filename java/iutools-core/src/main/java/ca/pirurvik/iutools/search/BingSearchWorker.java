package ca.pirurvik.iutools.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ca.nrc.data.harvesting.BingSearchEngine;
import ca.nrc.data.harvesting.SearchEngine;
import ca.nrc.data.harvesting.SearchEngine.Hit;
import ca.nrc.data.harvesting.SearchEngine.SearchEngineException;
import ca.nrc.data.harvesting.SearchEngine.Type;

public class BingSearchWorker implements Runnable {
	public String query;
	private Integer hitsPerPage;
	public int hitsPageNum = 0;
	
	
	public String thrName;
	
	private Thread thr;
	public Exception error;
	
	public Long  totalHits = new Long(0);
	public List<SearchHit> hits = new ArrayList<SearchHit>();
	private Set<String> excludedURLs;
		   
	BingSearchWorker(String _query) {
		this.initialize(_query,  0, null, null);
	}

	BingSearchWorker(String _query, String _thrName) {
		this.initialize(_query,  0, null, _thrName);
	}

	BingSearchWorker(String _query, int _hitsPageNum, Integer _hitsPerPage, String _thrName) {
		this.initialize(_query, _hitsPageNum, _hitsPerPage, _thrName);
	}
	
	private void initialize(String _query, int _hitsPageNum, Integer _hitsPerPage, String _thrName) {
		if (_hitsPerPage == null) _hitsPerPage = 10;
		
		this.query = _query;
		this.hitsPerPage = _hitsPerPage;
		this.hitsPageNum = _hitsPageNum;
		this.thrName = _thrName;
		
	}
	
	public boolean stillWorking() {
		return thr.isAlive();
	}
	   
   public void run()  {
//	   System.out.println("-- BingSearcher.run: thrName="+this.thrName+" started, query="+this.query);
	   
		List<SearchHit> hitsList = new ArrayList<SearchHit>();
		Long total = new Long(0);
		BingSearchEngine engine;
		try {
			engine = new BingSearchEngine();
		} catch (IOException | SearchEngineException e) {
			this.error = e;
			return;
		}
		
		SearchEngine.Query webQuery = 
				new SearchEngine.Query(this.query).setType(Type.ANY)
						.setLang("iu").setMaxHits(this.hitsPerPage)
						.setHitsPageNum(this.hitsPageNum)
				;
		List<SearchEngine.Hit> results;
		try {
			results = engine.search(webQuery);
		} catch (SearchEngineException e) {
			this.error = e;
			return;
		}
		
		Iterator<Hit> iter = results.iterator();
		while (iter.hasNext()) {
			Hit bingHit = iter.next();
			total = bingHit.outOfTotal;
			SearchHit aHit = new SearchHit(bingHit.url.toString(), bingHit.title, bingHit.summary);
			if (!this.excludedURLs.contains(aHit.url)) {
				hitsList.add(aHit);	
			}
		}
		
		this.totalHits = total;
		this.hits = hitsList;
	   
//	   System.out.println("-- BingSearcher.run: thrName="+this.thrName+" ENDED");

   }
   
   public void start () {
//	   System.out.println("-- BingSearcher.start: thrName="+this.thrName);
	   if (thr == null) {
		   thr = new Thread (this, this.thrName);
		   thr.start ();
	   }
	}

public void excludeUrls(Set<String> urls) {
	this.excludedURLs = urls;
	
}	

}
