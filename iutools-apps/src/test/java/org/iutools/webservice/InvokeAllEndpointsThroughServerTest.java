package org.iutools.webservice;

import ca.nrc.web.Http;
import org.iutools.webservice.gist.GistWordInputs;
import org.iutools.webservice.tokenize.GistPrepareContentInputs;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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

	@Test
	public void invokeAllEndpoints() throws Exception {
		Map<String, ServiceInputs> callsToMake =
		new HashMap<String, ServiceInputs>();
		{
			callsToMake.put("spell", new SpellInputs("inukttttut"));
			callsToMake.put("tokenize", new GistPrepareContentInputs("nunavut"));
			callsToMake.put("gist/preparecontent", new GistPrepareContentInputs("nunavut"));
			callsToMake.put("gist/gistword", new GistWordInputs("nunavut"));
		}

		for (int ii = 0; ii < 10; ii++) {
			for (String srvPath : callsToMake.keySet()) {
				ServiceInputs inputs = callsToMake.get(srvPath);
				IUTServiceTestHelpers.invokeEndpointThroughServer(
					Http.Method.POST, srvPath, callsToMake.get(srvPath));
			}
			Thread.sleep(1*1000);
		}

		Thread.sleep(30*1000);
	}
}
