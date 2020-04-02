package ca.pirurvik.iutools.webservice.gist;

import java.util.List;

import org.junit.Assert;

import ca.nrc.introspection.Introspection;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertObject;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import ca.pirurvik.iutools.concordancer.Alignment;
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

	public GistPrepareContentAsserter iuSentencesEquals(
			String[][] expIUSentences) throws Exception {
		AssertObject.assertDeepEquals(baseMessage+"\nIU sentences not as expected", 
				expIUSentences, responseIUSentences());
		return this;
	}
	
	public GistPrepareContentAsserter enSentencesEquals(
			String[][] expEnSentences) throws Exception {
		AssertObject.assertDeepEquals(baseMessage+"\nEn sentences not as expected", 
				expEnSentences, gotResponse().enSentences);
		return this;
	}
	
	
	public GistPrepareContentAsserter inputWasActualContent(boolean expStatus) {
		Assert.assertEquals(expStatus, gotResponse().wasActualText);
		return this;
	}

	private List<String[]> responseIUSentences() {
		List<String[]> gotIUSentences = 
			gotResponse().iuSentences;
		
		return gotIUSentences;
	}
	
	private GistPrepareContentResponse gotResponse() {
		return ((GistPrepareContentResponse)gotObject);
	}

	public void containsAlignment(Alignment expAlignment) {
		
		Assert.assertEquals(
			"IU and EN alignments did not contain the same number of sentences", 
			gotResponse().iuSentences.size(), gotResponse().enSentences.size());
		
		String alignments = "Alignments were:\n";
		boolean found = false;
		for (int ii=0; ii < gotResponse().iuSentences.size(); ii++) {
			String[] gotIuSentence = gotResponse().iuSentences.get(ii);
			String gotIuText = String.join("", gotIuSentence);
			String[] gotEnSentence = gotResponse().enSentences.get(ii);
			String gotEnText = String.join("", gotEnSentence);
			Alignment gotAlignment = 
				new Alignment("iu", gotIuText, "en", gotEnText);
			System.out.println("** containsAlignment: Looking at gotAlignment="+gotAlignment);
			if (gotAlignment.toString().equals(expAlignment.toString())) {
				found = true;
				break;
			}
			alignments += "   "+gotAlignment.toString()+"\n";
		}
		
		Assert.assertTrue(
			"Alignment not found: "+expAlignment+"\n"+alignments, 
			found);
	}

	
}
