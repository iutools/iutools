package ca.pirurvik.iutools.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.nrc.datastructure.Cloner;
import ca.nrc.datastructure.Cloner.ClonerException;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.datastructure.Pair;

public class BingSearchMultiQuery {
	
	public static class BingSearchMultithrdException extends Exception {
		public BingSearchMultithrdException(Exception e) {super(e);}
	}
	
	private BingSearchWorker[] workers = null;
	
	public BingSearchMultiQuery() {
	}

	public PageOfHits search(String query) throws BingSearchMultithrdException  {
		return search(query, null);
	}
	
	public PageOfHits search(String query, Integer _hitsPerPage) throws BingSearchMultithrdException  {
		PageOfHits nullPage = new PageOfHits(query);
		if (_hitsPerPage != null) {
			nullPage.setHitsPerPage(_hitsPerPage);
		}
		PageOfHits page = retrieveNextPage(nullPage);
		return page;
	}
	
	public PageOfHits retrieveNextPage(PageOfHits prevPage) throws BingSearchMultithrdException  {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.search.BingSearchMultithrd.retrieveNextPage");
		
		tLogger.trace("prevPage="+PrettyPrinter.print(prevPage));
		
		PageOfHits page;
		try {
			page = Cloner.clone(prevPage);
		} catch (ClonerException e) {
			throw new BingSearchMultithrdException(e);
		}
		page.hasNext = false;
		long totalEstHits = 0;		
		List<SearchHit> hits = new ArrayList<SearchHit>();
		
		page.incrPageNum();
		
		// Create one worker per term
		int numWorkers = page.getQueryTerms().length;
		workers = new BingSearchWorker[numWorkers];
		for (int ii=0; ii < numWorkers; ii++) {
			String aTerm = page.getQueryTerms()[ii];
			BingSearchWorker aWorker = 
					new BingSearchWorker(aTerm, page.getBingPageNum(), page.hitsPerPage, "thr-"+ii+"-"+aTerm);
			aWorker.excludeUrls(page.urlsAllPreviousHits);
			workers[ii] = aWorker;
			aWorker.start();
		}
		
		// Monitor the workers until they are all done
		while (true) {
			int stillRunning = 0;
			for (int ii=0; ii < numWorkers; ii++) {
				BingSearchWorker currWorker = workers[ii];
				if (currWorker != null) {
					if (currWorker.stillWorking()) {
						stillRunning++;
					} else {
						// This worker just finished running. Integrate its
						// results in the total
						if (currWorker.hits.size() >= page.hitsPerPage) {
							page.hasNext = true;
						}
						totalEstHits += currWorker.totalHits;
					}
				}				
			}
			// All workers have finished
			if (stillRunning == 0) break;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// Nothing to do if the sleep is interrupted
			}
		}
		
		aggregateWorkerHits(page);
		
		page.estTotalHits = totalEstHits;
		if (!page.hasNext) {
			page.estTotalHits = new Long(page.urlsAllPreviousHits.size() + page.hitsCurrPage.size());
		}
		
		return page;
	}

	private void aggregateWorkerHits(PageOfHits page) {

		//
		// Mix hits of the different workers. That way, the top
		// 10 hits will contain a mix of hits for different terms.
		//
		
		List<SearchHit> aggregated = new ArrayList<SearchHit>();
		boolean someHitsLeft = true;
		while (someHitsLeft) {
			int emptyWorkers = 0;
			for (int ii=0; ii < workers.length; ii++) {
				List<SearchHit> remainingHits = workers[ii].hits;
				if (remainingHits.size() == 0) {
					emptyWorkers++;
				} else {
					SearchHit hit = remainingHits.remove(0);
					String url = hit.url;
					if (!page.urlsAllPreviousHits.contains(url)) {
						aggregated.add(hit);
						page.addPreviousHitURLs(new String[] {url});
					}
				}
			}
			if (emptyWorkers == workers.length) someHitsLeft = false;
		}
		
		page.hitsCurrPage = aggregated;
	}



}
