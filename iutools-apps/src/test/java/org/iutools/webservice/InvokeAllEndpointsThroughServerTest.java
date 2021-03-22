package org.iutools.webservice;

import ca.nrc.web.Http;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.iutools.webservice.gist.GistPrepareContentInputs;
import org.iutools.webservice.gist.GistWordInputs;
import org.iutools.webservice.morphexamples.MorphemeExamplesInputs;
import org.iutools.webservice.search.ExpandQueryInputs;
import org.iutools.webservice.spell.SpellInputs;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This test case invokes every end point multiple times through the
 * server.
 *
 * The purpose of this is to ensure that threads for each end point have
 * been created and initialise, and that future calls to the endpoint will
 * not encur that any thread initialisation overhead.
 *
 * This in turn ensures that when we run the jmeter tests, the first few
 * endpoint calls won't be slower than normal and cause performance assertion
 * failures.
 */
public class InvokeAllEndpointsThroughServerTest {

	public static class HttpWorker implements Runnable {

		String uri = null;
		ServiceInputs inputs;
		private static List<Triple<String,ServiceInputs,Exception>> _raisedException
			= new ArrayList<Triple<String,ServiceInputs,Exception>>();

		public HttpWorker(String _uri, ServiceInputs _inputs) {
			init_HttpWorker(_uri, _inputs);
		}

		private void init_HttpWorker(String _uri, ServiceInputs _inputs) {
			this.uri = _uri;
			this.inputs = _inputs;
		}

		@Override
		public void run() {
			try {
				IUTServiceTestHelpers.invokeEndpointThroughServer(
					Http.Method.POST, uri, inputs);
			} catch (Exception e) {
				addException(e);
			}
		}

		private synchronized void addException(Exception e) {
			_raisedException.add(Triple.of(uri, inputs, e));
		}

		public synchronized static List<Triple<String,ServiceInputs,Exception>> getRaisedException() {
			return _raisedException;
		}


		public synchronized
			List<Triple<String,ServiceInputs,Exception>> raisedException() {
			return _raisedException;
		}
	}

	@Test
	public void invokeAllEndpoints() throws Exception {
		Map<String, Pair<ServiceInputs,Integer>> callsToMake =
		new HashMap<String, Pair<ServiceInputs,Integer>>();
		{
			callsToMake.put("gist/gistword",
				// Note: We input a word that does not exist in the compiled corpus
				// to force the gister to decompose it. This in turns forces the
				// loading of the linguistic database which encurs a significant
				// overhead
				Pair.of(new GistWordInputs("nunavutttt"), 10));
			callsToMake.put("gist/preparecontent",
				Pair.of(new GistPrepareContentInputs("inuksuk"), 2));
			callsToMake.put("morpheme_examples",
				Pair.of(new MorphemeExamplesInputs("siuq"), 2));
			callsToMake.put("search/expandquery",
				Pair.of(new ExpandQueryInputs("inuksuk"), 5));
			callsToMake.put("spell",
				Pair.of(new SpellInputs("inuktttitut"), 30));
		}

		ExecutorService executor = Executors.newFixedThreadPool(50);

		List<HttpWorker> workers = new ArrayList<HttpWorker>();
		for (String srvPath : callsToMake.keySet()) {
			Pair<ServiceInputs,Integer> callDetails = callsToMake.get(srvPath);
			ServiceInputs inputs = callDetails.getLeft();
			Integer sleepSecs = callDetails.getRight();
			for (int ii = 0; ii < 100; ii++) {
				HttpWorker worker = new HttpWorker(srvPath, inputs);
				executor.execute(worker);
				workers.add(worker);
			}
			Thread.sleep(sleepSecs*1000);
		}
		Thread.sleep(30*1000);
		System.out.println("--invokeAllEndpoints: "+HttpWorker.getRaisedException().size()+" exceptions raised");
	}
}
