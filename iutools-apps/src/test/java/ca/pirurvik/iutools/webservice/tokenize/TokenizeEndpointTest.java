package ca.pirurvik.iutools.webservice.tokenize;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import ca.pirurvik.iutools.webservice.IUTServiceTestHelpers;
import ca.pirurvik.iutools.webservice.IUTServiceTestHelpers.EndpointNames;
import ca.pirurvik.iutools.webservice.tokenize.GistPrepareContentInputs;
import ca.pirurvik.iutools.webservice.tokenize.TokenizeEndpoint;
import ca.pirurvik.iutools.webservice.SearchEndpoint;
import ca.pirurvik.iutools.webservice.SearchInputs;

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
