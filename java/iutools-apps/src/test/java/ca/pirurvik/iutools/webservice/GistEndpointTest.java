package ca.pirurvik.iutools.webservice;

import org.junit.Before;
import org.junit.Test;

import ca.inuktitutcomputing.utilities.Alignment;
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
	public void test__GistEndpoint__Roman__HappyPath() throws Exception {
		GistInputs gistInputs = new GistInputs("takujaujuq");
		
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.GIST,
					gistInputs
				);
		
		// Benoit, pour le moment, GistEndpoint retourne un array de 
		// decomps vide. Mais éventuellement, du devras changer cette 
		// expectation.
		String[] expDecompsAsStrings = new String[] {
			"{taku:taku/1v}{ja:jaq/1vn}{u:u/1nv}{juq:juq/1vn}",
			"{taku:taku/1v}{ja:jaq/1vn}{u:u/1nv}{juq:juq/tv-ger-3s}"
		};
		Alignment[] expSentencePairs = new Alignment[] {
			new Alignment(
				"en"," Mr. Chairman, and this wording and the reason I was asking the question at that time was that you know one really looked that, you can see that, if the community doesnât feel that it is fair or right whether they are right or not, that the government is obligated to act and that could be anywhere.",
				"in","iksivautaq, ukualu uqarsimaningit tainnalu apiqqutigiluaqqauvara taiksumani tamanna <span class='highlighted'>takujaujuq</span>, takuksaujuq, nunaliujut nammaktutiqanngippata tamarutik tammangikkutik, gavamakkut qanuiligiariaqaqput tamanna namituinnaujunnaqtuni."
				)
		};
		IUTServiceTestHelpers.assertGistResponseIsOK(response, 
				expDecompsAsStrings, expSentencePairs);
	}
}
