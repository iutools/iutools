package ca.pirurvik.iutools.search;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.pirurvik.iutools.search.BingSearchWorker;

public class BingSearchWorkerTest {

	@Test
	public void test__BingSearcher__HappyPath() {
		BingSearchWorker[] workers = new BingSearchWorker[] {
				new BingSearchWorker("inuktitut", "thr-1-inuktitut"),
				new BingSearchWorker("inuk", "thr-2-inuk"),
				new BingSearchWorker("inukshuk", "thr-3-inukshuk"),
		};
		
		for (BingSearchWorker aWorker: workers) {
			aWorker.run();
		}
	}

}
