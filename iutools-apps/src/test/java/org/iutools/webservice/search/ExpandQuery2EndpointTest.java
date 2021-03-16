package org.iutools.webservice.search;

import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class ExpandQuery2EndpointTest extends EndpointTest {
	@Override
	public Endpoint makeEndpoint() {
		return new ExpandQuery2Endpoint();
	}

	@Override @Test
	public void test__logEntry() throws Exception {
		ExpandQuery2Inputs inputs = new ExpandQuery2Inputs("inuksuk");
		assertLogEntryEquals(
			inputs,
			new JSONObject().put("origQuery", "inuksuk"));
	}

	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__ExpandQueryEndpoint__RomanNotAlreadyExpanded() throws Exception {

		EndpointResult epResult = execute("inuksuk");
		String expQuery = "(inuksuk OR inussummik OR inuksunnguat OR inuksui OR inuksuup OR inuksummi)";
		new AssertExpandQuery2Result(epResult)
			.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__RomanAlreadyExpanded() throws Exception {

		String origQuery = "(inussummik OR inuksunnguat)";
		EndpointResult epResult = execute(origQuery);

		// If the original query is already expanded, don't re-expand it.
		String expQuery = origQuery;
		new AssertExpandQuery2Result(epResult)
			.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__SyllabicNotAlreadyExpanded() throws Exception {

		EndpointResult epResult = execute("ᐃᓄᒃᓱᒃ");
		String expQuery = "(ᐃᓄᒃᓱᒃ OR ᐃᓄᔅᓱᒻᒥᒃ OR ᐃᓄᒃᓱᙳᐊᑦ OR ᐃᓄᒃᓱᐃ OR ᐃᓄᒃᓲᑉ)";
		new AssertExpandQuery2Result(epResult)
			.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__SyllabicAlreadyExpanded() throws Exception {

		String origQuery = "(ᐃᓄᒃᓱᒃ OR ᐃᓄᔅᓱᒻᒥᒃ OR ᐃᓄᒃᓱᙳᐊᑦ OR ᐃᓄᒃᓱᐃ OR ᐃᓄᒃᓲᑉ)";
		EndpointResult epResult = execute(origQuery);


		// If the original query is already expanded, don't re-expand it.
		String expQuery = origQuery;
		new AssertExpandQuery2Result(epResult)
			.expandedQueryIs(expQuery);
	}

	///////////////////////////////////
	// TEST HELPERS
	///////////////////////////////////

	protected EndpointResult execute(String query) throws Exception {
		ExpandQuery2Inputs expandInputs = new ExpandQuery2Inputs(query);
		EndpointResult result = endPoint.execute(expandInputs);
		return result;
	}
}
