package ca.pirurvik.iutools.webservice;

import org.junit.Before;
import org.junit.Test;

import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertObject;
import ca.nrc.ui.web.testing.MockHttpServletResponse;

public class GistEndpointTest {

	GistEndpoint endPoint = null;
	
	@Before
	public void setUp() throws Exception {
		endPoint = new GistEndpoint();
	}
	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/	
	
	@Test
	public void test__DELETE_ME_LATER() throws Exception {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		analyzer.decomposeWord("inuktitut");
	}
	
	@Test
	public void test__GistEndpoint__Roman__HappyPath() throws Exception {
		GistInputs gistInputs = new GistInputs("inuktitut");
		
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.GIST,
					gistInputs
				);
		
		// Benoit, pour le moment, GistEndpoint retourne un array de 
		// decomps vide. Mais éventuellement, du devras changer cette 
		// expectation.
		String[] expDecompsAsStrings = new String[] {};
		Pair<String,String>[] expSentencePairs = new Pair[] {};
		IUTServiceTestHelpers.assertGistResponseIsOK(response, 
				expDecompsAsStrings, expSentencePairs);
	}
}
