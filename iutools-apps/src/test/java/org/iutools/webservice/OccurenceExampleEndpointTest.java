package org.iutools.webservice;

import org.junit.Before;
import org.junit.Test;

import ca.inuktitutcomputing.morph.Gist;
import ca.inuktitutcomputing.utilities.Alignment;
import ca.nrc.ui.web.testing.MockHttpServletResponse;

public class OccurenceExampleEndpointTest {

	OccurenceExampleEndpoint endPoint = null;
	
	@Before
	public void setUp() throws Exception {
		endPoint = new OccurenceExampleEndpoint();
	}
	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/	
	
	@Test
	public void test__OccurenceExampleEndpoint__HappyPath() throws Exception {
		OccurenceExampleInputs occurenceInputs = new OccurenceExampleInputs("takujaujuq");
		
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.MORPHEMEEXAMPLE,
					occurenceInputs
				);
		ExampleWordWithMorpheme expected = new ExampleWordWithMorpheme(
				"takujaujuq", 
				new Gist("takujaujuq"),
				new Alignment[] {
					new Alignment(
						"iu","iksivautaq, ukualu uqarsimaningit tainnalu apiqqutigiluaqqauvara taiksumani tamanna takujaujuq, takuksaujuq, nunaliujut nammaktutiqanngippata tamarutik tammangikkutik, gavamakkut qanuiligiariaqaqput tamanna namituinnaujunnaqtuni.",
						"en","Mr. Chairman, and this wording and the reason I was asking the question at that time was that you know one really looked that, you can see that, if the community doesn’t feel that it is fair or right whether they are right or not, that the government is obligated to act and that could be anywhere."
						)
				}
				);
		IUTServiceTestHelpers.assertOccurenceExampleResponseIsOK(response, expected);
	}
	
 
}
