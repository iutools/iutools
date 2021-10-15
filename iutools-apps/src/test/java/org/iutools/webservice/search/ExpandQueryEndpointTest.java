package org.iutools.webservice.search;

import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class ExpandQueryEndpointTest extends EndpointTest {
	@Override
	public Endpoint makeEndpoint() {
		return new ExpandQueryEndpoint();
	}

	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__ExpandQueryEndpoint__RomanNotAlreadyExpanded() throws Exception {

		EndpointResult epResult = execute("inuksuk");
		String expQuery = "(inuksuk OR inussummik OR inuksunnguat OR inuksui OR inuksuup OR inuksummi)";
		new AssertExpandQueryResult(epResult)
			.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__RomanAlreadyExpanded() throws Exception {

		String origQuery = "(inussummik OR inuksunnguat)";
		EndpointResult epResult = execute(origQuery);

		// If the original query is already expanded, don't re-expand it.
		String expQuery = origQuery;
		new AssertExpandQueryResult(epResult)
			.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__SyllabicNotAlreadyExpanded() throws Exception {

		EndpointResult epResult = execute("ᐃᓄᒃᓱᒃ");
		String expQuery = "(ᐃᓄᒃᓱᒃ OR ᐃᓄᔅᓱᒻᒥᒃ OR ᐃᓄᒃᓱᙳᐊᑦ OR ᐃᓄᒃᓱᐃ OR ᐃᓄᒃᓲᑉ)";
		new AssertExpandQueryResult(epResult)
			.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__SyllabicAlreadyExpanded() throws Exception {

		String origQuery = "(ᐃᓄᒃᓱᒃ OR ᐃᓄᔅᓱᒻᒥᒃ OR ᐃᓄᒃᓱᙳᐊᑦ OR ᐃᓄᒃᓱᐃ OR ᐃᓄᒃᓲᑉ)";
		EndpointResult epResult = execute(origQuery);


		// If the original query is already expanded, don't re-expand it.
		String expQuery = origQuery;
		new AssertExpandQueryResult(epResult)
			.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__RomanWithSpacesBeforeAndAfterQueryWord() throws Exception {
		EndpointResult epResult = execute("  inuksuk  ");
		String expQuery = "(inuksuk OR inussummik OR inuksunnguat OR inuksui OR inuksuup OR inuksummi)";
		new AssertExpandQueryResult(epResult)
			.expandedQueryIs(expQuery);

		epResult = execute("   (inuksuk OR inussummik OR inuksunnguat OR inuksui OR inuksuup OR inuksummi)   ");
		new AssertExpandQueryResult(epResult)
			.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__SyllabicWithLeadingTrailingSpaces() throws Exception {
		EndpointResult epResult = execute("   ᐃᓄᒃᓱᒃ   ");
		String expQuery = "(ᐃᓄᒃᓱᒃ OR ᐃᓄᔅᓱᒻᒥᒃ OR ᐃᓄᒃᓱᙳᐊᑦ OR ᐃᓄᒃᓱᐃ OR ᐃᓄᒃᓲᑉ)";
		new AssertExpandQueryResult(epResult)
			.expandedQueryIs(expQuery);

		epResult = execute("   (ᐃᓄᒃᓱᒃ OR ᐃᓄᔅᓱᒻᒥᒃ OR ᐃᓄᒃᓱᙳᐊᑦ OR ᐃᓄᒃᓱᐃ OR ᐃᓄᒃᓲᑉ)  ");
		new AssertExpandQueryResult(epResult)
			.expandedQueryIs(expQuery);
	}


	///////////////////////////////////
	// TEST HELPERS
	///////////////////////////////////

	protected EndpointResult execute(String query) throws Exception {
		ExpandQueryInputs expandInputs = new ExpandQueryInputs(query);
		EndpointResult result = endPoint.execute(expandInputs);
		return result;
	}
}
