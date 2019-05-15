package ca.pirurvik.iutools.search;

import java.util.ArrayList;
import java.util.List;

import ca.nrc.datastructure.Pair;

public class BingSearchMultithrd {
	
//	String[] workerTerm;
//	Long[] workerTotalHits;
//	List<List<SearchHit>> workerHits;

	public BingSearchMultithrd() {
	}
	

	public Pair<Long,List<SearchHit>> search(String[] terms) {
		Long totalHits = new Long(0);
		List<SearchHit> hits = new ArrayList<SearchHit>();
		
		// Create one worker per term
		int numWorkers = terms.length;
		BingSearcWorker[] workers = new BingSearcWorker[numWorkers];
//		workerTerm = new String[numWorkers];
//		workerHits = new ArrayList<List<SearchHit>>();
//		workerTotalHits = new Long[numWorkers];
		for (int ii=0; ii < numWorkers; ii++) {
			String aTerm = terms[ii];
			BingSearcWorker aWorker = new BingSearcWorker(aTerm, "thr-"+ii+"-"+aTerm);
			workers[ii] = aWorker;
			aWorker.start();
		}
		
		// Monitor the workers until they are all done
		long totalEstHits = 0;
		List<SearchHit> allHits = new ArrayList<SearchHit>();
		
		while (true) {
			int stillRunning = 0;
			for (int ii=0; ii < numWorkers; ii++) {
				BingSearcWorker currWorker = workers[ii];
				if (currWorker != null) {
					if (currWorker.stillWorking()) {
						stillRunning++;
					} else {
						// This worker just finished running. Integrate its
						// results in the total
						totalEstHits += currWorker.totalHits;
						allHits.addAll(currWorker.hits);
					}
				}				
			}
			// All workers have finished
			if (stillRunning == 0) break;
		}
		
		
		
		return Pair.of(totalEstHits, allHits);
	}

}
