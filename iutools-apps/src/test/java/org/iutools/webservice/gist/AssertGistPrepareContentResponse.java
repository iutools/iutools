package org.iutools.webservice.gist;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.concordancer.Alignment;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AssertGistPrepareContentResponse extends Asserter<MockHttpServletResponse> {

	GistPrepareContentResponse response;

	public AssertGistPrepareContentResponse(MockHttpServletResponse _gotObject) throws IOException {
		super(_gotObject);
		init(_gotObject);
	}

	public AssertGistPrepareContentResponse(MockHttpServletResponse _gotObject, String mess) throws IOException {
		super(_gotObject, mess);
		init(_gotObject);
	}

	private void init(MockHttpServletResponse gotMockResp) throws IOException {
		String json = gotMockResp.getOutput();
		response =
			new ObjectMapper()
			.readValue(json, GistPrepareContentResponse.class);
	}

	public AssertGistPrepareContentResponse iuSentencesEquals(
			String[][] expIUSentences) throws Exception {
		AssertObject.assertDeepEquals(baseMessage+"\nIU sentences not as expected",
				expIUSentences, responseIUSentences());
		return this;
	}
	
	public AssertGistPrepareContentResponse enSentencesEquals(
			String[][] expEnSentences) throws Exception {
		AssertObject.assertDeepEquals(baseMessage+"\nEn sentences not as expected", 
				expEnSentences, response.enSentences);
		return this;
	}
	
	
	public AssertGistPrepareContentResponse inputWasActualContent(boolean expStatus) {
		Assert.assertEquals(expStatus, response.wasActualText);
		return this;
	}

	private List<String[]> responseIUSentences() {
		List<String[]> gotIUSentences = 
			response.iuSentences;
		
		return gotIUSentences;
	}
	
	public AssertGistPrepareContentResponse containsAlignment(Alignment expAlignment) {
		
		Assert.assertEquals(
			"IU and EN alignments did not contain the same number of sentences", 
			response.iuSentences.size(), response.enSentences.size());
		
		String alignments = "Alignments were:\n";
		boolean found = false;
		for (int ii=0; ii < response.iuSentences.size(); ii++) {
			String[] gotIuSentence = response.iuSentences.get(ii);
			String gotIuText = String.join("", gotIuSentence);
			String[] gotEnSentence = response.enSentences.get(ii);
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

	public AssertGistPrepareContentResponse hasNoContentForLang(String lang) {
		List<String[]> gotContent = null;
		if (lang.equals("iu")) {
			gotContent = response.iuSentences;
		} else if (lang.equals("en")) {
			gotContent = response.enSentences;
		}
		Assert.assertEquals(
			"There should not have been any content for language "+lang, 
			new ArrayList<String[]>(), gotContent);
		
		return this;		
	}

	public AssertGistPrepareContentResponse hasContentForLang(String lang) {
		List<String[]> gotContent = null;
		if (lang.equals("iu")) {
			gotContent = response.iuSentences;
		} else if (lang.equals("en")) {
			gotContent = response.enSentences;
		}

		Assert.assertTrue(
			"There should have been any content for language "+lang,
			gotContent.size() > 0);

		return this;
	}

	public AssertGistPrepareContentResponse hasNoAlignments() {
		boolean gotAvailable = response.getAlignmentsAvailable();
		Assert.assertFalse("Alignments should NOT have been available", gotAvailable);
		return this;
	}

	public AssertGistPrepareContentResponse hasSomeAlignments() {
		boolean gotAvailable = response.getAlignmentsAvailable();
		Assert.assertTrue("Alignments SHOULD have been available", gotAvailable);
		return this;
	}

	public AssertGistPrepareContentResponse couldNotFetchIUContent() throws Exception {
		AssertObject.assertDeepEquals(
			"Should NOT have been able to fetch content of IU page", 
			new ArrayList<String[]>(), response.iuSentences);
		return this;
	}

	public AssertGistPrepareContentResponse couldNotFetchEnContent() 
			throws Exception {
		AssertObject.assertDeepEquals(
				"Should NOT have been able to fetch content of EN page", 
				new ArrayList<String[]>(), response.enSentences);
		return this;
	}

	public AssertGistPrepareContentResponse containsIUSentenceStartingWith(String expSentence) {
		boolean found = false;
		String mess = baseMessage+"Alignment did not contain IU sentence that starts with: "+
				expSentence+"\nIU sentence were:\n";
		for (String[] aSentTokens: response.iuSentences) {
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

	protected AssertGistPrepareContentResponse raisesError(String expError) {
		return raisesError("", expError);
	}

	protected AssertGistPrepareContentResponse raisesError(String mess, String expError) {
		AssertString.assertStringEquals(
			baseMessage+"\n"+mess,
			expError, response.errorMessage);
		return this;
	}	
}
