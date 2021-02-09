package org.iutools.webservice.relatedwords;

import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.webservice.IUTServiceTestHelpers;
import org.junit.Before;
import org.junit.Test;

public class RelatedWordsEndpointTest {

		RelatedWordsEndpoint endPoint = null;

		@Before
		public void setUp() throws Exception {
			endPoint = new RelatedWordsEndpoint();
		}


		/***********************
		 * VERIFICATION TESTS
		 ***********************/

		@Test
		public void test__RelatedWordsEndpoint__HappyPath() throws Exception {
			RelatedWordsInputs relatedWordsInputs =
				new RelatedWordsInputs("inuksuk");

			MockHttpServletResponse response =
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.RELATED_WORDS,
					relatedWordsInputs
			);

			new AssertRelatedWordsResponse(response)
				.relatedWordsAre(
					"inussummik", "inuksunnguat", "inuksui", "inuksuup",
					"inuksummi"
				);
		}
}
