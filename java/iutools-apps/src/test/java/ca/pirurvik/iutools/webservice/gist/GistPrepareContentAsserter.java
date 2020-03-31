package ca.pirurvik.iutools.webservice.gist;

import java.util.List;

import ca.nrc.introspection.Introspection;
import ca.nrc.testing.AssertObject;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import ca.pirurvik.iutools.testing.Asserter;
import ca.pirurvik.iutools.webservice.IUTServiceTestHelpers;

public class GistPrepareContentAsserter extends Asserter {
	

	public static GistPrepareContentAsserter assertThat(MockHttpServletResponse response, 
			String mess) throws Exception {
		return new GistPrepareContentAsserter(response, mess);
	}
	
	public GistPrepareContentAsserter(
			MockHttpServletResponse gotResponse, String mess) throws Exception {
		super(gotResponse, mess, GistPrepareContentResponse.class);
		this.gotObject = IUTServiceTestHelpers.toGistPrepareContentResponse(gotResponse);
	}

	public void iuSentencesEquals(String[][] expIUSentences) throws Exception {
		AssertObject.assertDeepEquals(baseMessage+"\nIU sentences not as expected", 
				expIUSentences, responseIUSentences());
	}

	private List<String[]> responseIUSentences() {
		int x = 1;
		List<String[]> gotIUSentences = 
			((GistPrepareContentResponse)gotObject).iuSentences;
		
		return gotIUSentences;
	}
	
}
