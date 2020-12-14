package org.iutools.webservice.tokenize;

import org.junit.Before;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.webservice.IUTServiceTestHelpers;

public class TokenizeEndpointTest {

	TokenizeEndpoint endPoint = null;
	
	@Before
	public void setUp() throws Exception {
		endPoint = new TokenizeEndpoint();
	}

	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/
	
	@Test
	public void test__TokenizeEndpoint__HappyPath() throws Exception {
		
		GistPrepareContentInputs tokenizeInputs = new GistPrepareContentInputs("nunavut, inuktut");
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.TOKENIZE,
					tokenizeInputs
				);
		
		Pair<String,Boolean>[] expTokens = new Pair[] {
			Pair.of("nunavut", true), 
			Pair.of(",", false), 
			Pair.of(" ", false), 
			Pair.of("inuktut", true) 			
		};
		
		TokenizeResponseAssertion.assertThat(response, "")
			.raisesNoError()
			.producesTokens(expTokens)
		;
	}
}
