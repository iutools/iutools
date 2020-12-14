package org.iutools.webservice.gist;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import ca.nrc.testing.AssertObject;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.concordancer.Alignment;
import org.iutools.testing.Asserter;
import org.iutools.webservice.IUTServiceTestHelpers;

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

	public GistPrepareContentAsserter containsAlignment(Alignment expAlignment) {
		
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
		
		return this;
	}

	public GistPrepareContentAsserter hasNoContentForLang(String lang) {
		List<String[]> gotContent = null;
		if (lang.equals("iu")) {
			gotContent = gotResponse().iuSentences;
		} else if (lang.equals("en")) {
			gotContent = gotResponse().enSentences;
		}
		Assert.assertEquals(
			"There should not have been any content for language "+lang, 
			new ArrayList<String[]>(), gotContent);
		
		return this;		
	}

	public GistPrepareContentAsserter hasContentForLang(String lang) {
		List<String[]> gotContent = null;
		if (lang.equals("iu")) {
			gotContent = gotResponse().iuSentences;
		} else if (lang.equals("en")) {
			gotContent = gotResponse().enSentences;
		}

		Assert.assertTrue(
			"There should have been any content for language "+lang,
			gotContent.size() > 0);

		return this;
	}

	public GistPrepareContentAsserter hasNoAlignments() {
		boolean gotAvailable = gotResponse().getAlignmentsAvailable();
		Assert.assertFalse("Alignments should NOT have been available", gotAvailable);
		return this;
	}

	public GistPrepareContentAsserter hasSomeAlignments() {
		boolean gotAvailable = gotResponse().getAlignmentsAvailable();
		Assert.assertTrue("Alignments SHOULD have been available", gotAvailable);
		return this;
	}

	public GistPrepareContentAsserter couldNotFetchIUContent() throws Exception {
		AssertObject.assertDeepEquals(
			"Should NOT have been able to fetch content of IU page", 
			new ArrayList<String[]>(), gotResponse().iuSentences);
		return this;
	}

	public GistPrepareContentAsserter couldNotFetchEnContent() 
			throws Exception {
		AssertObject.assertDeepEquals(
				"Should NOT have been able to fetch content of EN page", 
				new ArrayList<String[]>(), gotResponse().enSentences);
		return this;
	}

	public GistPrepareContentAsserter containsIUSentenceStartingWith(String expSentence) {
		boolean found = false;
		String mess = baseMessage+"Alignment did not contain IU sentence that starts with: "+
				expSentence+"\nIU sentence were:\n";
		for (String[] aSentTokens: gotResponse().iuSentences) {
			String gotSent = String.join("", aSentTokens);
			if (gotSent.startsWith(expSentence)) {
				found = true;
				break;
			}
			mess += "  "+gotSent+"\n";
		}		
		
		Assert.assertTrue(mess, found);
		
		return this;
	}

	
}
