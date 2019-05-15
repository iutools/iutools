package ca.pirurvik.iutools.search;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.pirurvik.iutools.search.BingSearcWorker;

public class BingSearchWorkerTest {

	@Test
	public void test__BingSearcher__HappyPath() {
		BingSearcWorker[] workers = new BingSearcWorker[] {
				new BingSearcWorker("inuktitut", "thr-1-inuktitut"),
				new BingSearcWorker("inuk", "thr-2-inuk"),
				new BingSearcWorker("inukshuk", "thr-3-inukshuk"),
		};
		
		for (BingSearcWorker aWorker: workers) {
			aWorker.run();
		}
	}

}
