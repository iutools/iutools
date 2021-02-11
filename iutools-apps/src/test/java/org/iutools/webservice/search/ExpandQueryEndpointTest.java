package org.iutools.webservice.search;

import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.webservice.IUTServiceTestHelpers;
import org.junit.Before;
import org.junit.Test;

public class ExpandQueryEndpointTest {

	ExpandQueryEndpoint endPoint = null;

	@Before
	public void setUp() throws Exception {
		endPoint = new ExpandQueryEndpoint();
	}


	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__ExpandQueryEndpoint__RomanNotAlreadyExpanded() throws Exception {

		ExpandQueryInputs expandInputs = new ExpandQueryInputs("inuksuk");

		MockHttpServletResponse response =
		IUTServiceTestHelpers.postEndpointDirectly(
			IUTServiceTestHelpers.EndpointNames.EXPAND_QUERY,
			expandInputs);

		String expQuery = "(inussummik OR inuksunnguat OR inuksui OR inuksuup OR inuksummi)";
		new AssertExpandQueryResponse(response)
			.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__RomanAlreadyExpanded() throws Exception {

		String origQuery = "(inussummik OR inuksunnguat)";
		ExpandQueryInputs expandInputs = new ExpandQueryInputs(origQuery);

		MockHttpServletResponse response =
			IUTServiceTestHelpers.postEndpointDirectly(
				IUTServiceTestHelpers.EndpointNames.EXPAND_QUERY,
				expandInputs);

		// If the original query is already expanded, don't re-expand it.
		String expQuery = origQuery;
		new AssertExpandQueryResponse(response)
			.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__SyllabicNotAlreadyExpanded() throws Exception {

		ExpandQueryInputs expandInputs = new ExpandQueryInputs("ᐃᓄᒃᓱᒃ");

		MockHttpServletResponse response =
		IUTServiceTestHelpers.postEndpointDirectly(
		IUTServiceTestHelpers.EndpointNames.EXPAND_QUERY,
		expandInputs);

		String expQuery = "(ᐃᓄᒃᓱᒃ OR ᐃᓄᔅᓱᒻᒥᒃ OR ᐃᓄᒃᓱᙳᐊᑦ OR ᐃᓄᒃᓱᐃ OR ᐃᓄᒃᓲᑉ)";
		new AssertExpandQueryResponse(response)
		.expandedQueryIs(expQuery);
	}

	@Test
	public void test__ExpandQueryEndpoint__SyllabicAlreadyExpanded() throws Exception {

		String origQuery = "(ᐃᓄᒃᓱᒃ OR ᐃᓄᔅᓱᒻᒥᒃ OR ᐃᓄᒃᓱᙳᐊᑦ OR ᐃᓄᒃᓱᐃ OR ᐃᓄᒃᓲᑉ)";
		ExpandQueryInputs expandInputs = new ExpandQueryInputs(origQuery);

		MockHttpServletResponse response =
		IUTServiceTestHelpers.postEndpointDirectly(
		IUTServiceTestHelpers.EndpointNames.EXPAND_QUERY,
		expandInputs);

		// If the original query is already expanded, don't re-expand it.
		String expQuery = origQuery;
		new AssertExpandQueryResponse(response)
		.expandedQueryIs(expQuery);
	}
}
