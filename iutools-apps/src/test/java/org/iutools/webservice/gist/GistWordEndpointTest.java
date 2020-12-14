package org.iutools.webservice.gist;

import org.junit.Before;
import org.junit.Test;

import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.webservice.IUTServiceTestHelpers;

public class GistWordEndpointTest {

	GistWordEndpoint endPoint = null;
	
	@Before
	public void setUp() throws Exception {
		endPoint = new GistWordEndpoint();
	}

	/***********************
	 * VERIFICATION TESTS
	 ***********************/	
	
	@Test
	public void test__GistWordEndpoint__RomanWord() throws Exception {
		
		GistWordInputs spellInputs = new GistWordInputs("inuktitut");
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.GIST_WORD,
					spellInputs
				);
		
		GistWordAsserter.assertThat(response, "")
			.gistMorphemesEqual(new String[] {"inuk", "titut"})
			.nthAlignmentIs(1, "en", "Inuktitut Documentation", "iu", "inuktitut titiraqtauniq")
			;
		return;	
	}

	@Test
	public void test__GistWordEndpoint__SyllabicWord() throws Exception {
		
		GistWordInputs spellInputs = new GistWordInputs("ᐃᓄᒃᑎᑐᑦ");
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.GIST_WORD,
					spellInputs
				);
		
		GistWordAsserter.assertThat(response, "")
			.gistMorphemesEqual(new String[] {"inuk", "titut"})
			.nthAlignmentIs(1, "en", "Inuktitut Documentation", "iu", "inuktitut titiraqtauniq")
			;
		return;	
	}
}
