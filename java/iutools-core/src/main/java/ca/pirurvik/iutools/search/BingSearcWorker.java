package ca.pirurvik.iutools.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ca.nrc.data.harvesting.BingSearchEngine;
import ca.nrc.data.harvesting.SearchEngine;
import ca.nrc.data.harvesting.SearchEngine.Hit;
import ca.nrc.data.harvesting.SearchEngine.SearchEngineException;
import ca.nrc.data.harvesting.SearchEngine.Type;

public class BingSearcWorker implements Runnable {
	private String query;
	private Integer hitsPerPage;
	private Integer maxHits;
	public String thrName;
	
	private Thread thr;
	public Exception error;
	
	public Long totalHits;
	public List<SearchHit> hits;
		   
	BingSearcWorker(String _query) {
		this.initialize(_query,  null, null, null);
	}

	BingSearcWorker(String _query, String _thrName) {
		this.initialize(_query,  null, null, _thrName);
	}

	BingSearcWorker(String _query, Integer _hitsPerPage, Integer _maxHits, String _thrName) {
		this.initialize(_query, _hitsPerPage, _maxHits, _thrName);
	}
	
	private void initialize(String _query, Integer _hitsPerPage, Integer _maxHits, String _thrName) {
		if (_hitsPerPage == null) _hitsPerPage = 10;
		if (_maxHits == null) _maxHits = 10;
		
		this.query = _query;
		this.hitsPerPage = _hitsPerPage;
		this.maxHits = _maxHits;
		this.thrName = _thrName;
		
	}
	
	public boolean stillWorking() {
		return thr.isAlive();
	}
	   
   public void run()  {
	   System.out.println("-- BingSearcher.run: thrName="+this.thrName+" started, query="+this.query);
	   
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
				;
		List<SearchEngine.Hit> results;
		try {
			results = engine.search(webQuery);
		} catch (SearchEngineException e) {
			this.error = e;
			return;
		}
		
		Iterator<Hit> iter = results.iterator();
		int hitsCount = 0;
		while (iter.hasNext()) {
			hitsCount++;
			if (hitsCount > this.maxHits) {
				break;
			}
			Hit bingHit = iter.next();
			total = bingHit.outOfTotal;
			SearchHit aHit = new SearchHit(bingHit.url.toString(), bingHit.title, bingHit.summary);
			hitsList.add(aHit);
			
		}
		
		this.totalHits = total;
		this.hits = hitsList;
	   
	   System.out.println("-- BingSearcher.run: thrName="+this.thrName+" ENDED");

   }
   
   public void start () {
	   System.out.println("-- BingSearcher.start: thrName="+this.thrName);
	   if (thr == null) {
		   thr = new Thread (this, this.thrName);
		   thr.start ();
	   }
	}	

}
