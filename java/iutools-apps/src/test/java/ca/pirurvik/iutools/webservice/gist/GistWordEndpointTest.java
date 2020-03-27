package ca.pirurvik.iutools.webservice.gist;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ca.nrc.ui.web.testing.MockHttpServletResponse;
import ca.pirurvik.iutools.webservice.IUTServiceTestHelpers;
import ca.pirurvik.iutools.webservice.SpellCheckerAssertion;
import ca.pirurvik.iutools.webservice.SpellEndpoint;
import ca.pirurvik.iutools.webservice.SpellInputs;

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
