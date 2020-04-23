package ca.pirurvik.iutools.webservice.tokenize;

import java.io.IOException;

import org.junit.Assert;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertObject;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import ca.pirurvik.iutools.webservice.IUTServiceTestHelpers;

public class TokenizeResponseAssertion {
	
	protected String baseMessage = "";
	protected TokenizeResponse gotTokenizeResponse = null;

	public static TokenizeResponseAssertion assertThat(
			MockHttpServletResponse response, String mess) 
					throws Exception {
		
		TokenizeResponseAssertion assertion = 
				new TokenizeResponseAssertion();
		
		assertion.baseMessage = mess;
		assertion.gotTokenizeResponse = 
				IUTServiceTestHelpers.toTokenizeResponse(response);
		
		return assertion;
	}
	
	public TokenizeResponseAssertion raisesNoError() {
		Assert.assertEquals(baseMessage+"\nResponse raised error",  
				null, gotTokenizeResponse.errorMessage);
		return this;
	}

	public TokenizeResponseAssertion producesTokens(
			Pair<String, Boolean>[] expTokens) throws Exception {
		
		AssertObject.assertDeepEquals(
				baseMessage+"\nTokens were not as expected", 
				expTokens, gotTokenizeResponse.tokens);
		return this;
	}
	

}
