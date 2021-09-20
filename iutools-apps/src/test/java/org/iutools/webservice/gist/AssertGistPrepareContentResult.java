package org.iutools.webservice.gist;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import org.iutools.concordancer.SentencePair;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.worddict.WordDictResult;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class AssertGistPrepareContentResult extends AssertEndpointResult {

	@Override
	protected GistPrepareContentResult result() {
		return (GistPrepareContentResult) gotObject;
	}

	public AssertGistPrepareContentResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertGistPrepareContentResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}
	
	public AssertGistPrepareContentResult iuSentencesEquals(
			String[][] expIUSentences) throws Exception {
		AssertObject.assertDeepEquals(
			baseMessage+"\nIU sentences not as expected",
				expIUSentences, responseIUSentences());
		return this;
	}
	
	public AssertGistPrepareContentResult enSentencesEquals(
			String[][] expEnSentences) throws Exception {
		AssertObject.assertDeepEquals(
			baseMessage+"\nEn sentences not as expected",
				expEnSentences, result().enSentences);
		return this;
	}
	
	
	public AssertGistPrepareContentResult inputWasActualContent(boolean expStatus) {
		Assert.assertEquals(expStatus, result().wasActualText);
		return this;
	}

	private List<String[]> responseIUSentences() {
		List<String[]> gotIUSentences =
		result().iuSentences;
		
		return gotIUSentences;
	}

	public AssertGistPrepareContentResult containsAlignment(
		SentencePair expAlignment) {
		containsAlignment(expAlignment, (Integer)null);
		return this;
	}

	public AssertGistPrepareContentResult containsAlignment(
	SentencePair expAlignment, Integer misalignmentTolerance) {

		if (misalignmentTolerance == null) {
			misalignmentTolerance = 5;
		}

		Assert.assertEquals(
			"IU and EN alignments did not contain the same number of sentences",
			result().iuSentences.size(), result().enSentences.size());
		
		String alignments = "Alignments were:\n";
		boolean found = false;
		String expIuText = expAlignment.getText("iu");
		String expEnText = expAlignment.getText("en");
		Integer iuSeenAt = null;
		Integer enSeenAt = null;

		// Check to see if the IU and En sentences appear in alignments that
		// are within misalignmentTolerance positions of each other
		// (in other words, we tolerate if the alignment is off by say, 5
		// positions)
		for (int ii=0; ii < result().iuSentences.size(); ii++) {
			String[] gotIuSentence = result().iuSentences.get(ii);
			String gotIuText = String.join("", gotIuSentence);
			String[] gotEnSentence = result().enSentences.get(ii);
			String gotEnText = String.join("", gotEnSentence);
			SentencePair gotAlignment =
				new SentencePair("iu", gotIuText, "en", gotEnText);

			if (gotIuText.equals(expIuText)) {
				iuSeenAt = ii;
			}
			if (gotEnText.equals(expEnText)) {
				enSeenAt = ii;
			}
			if (iuSeenAt != null && enSeenAt != null &&
				Math.abs(iuSeenAt-enSeenAt) < misalignmentTolerance) {
				found = true;
				break;
			}
			alignments += "   "+gotAlignment.toString()+"\n";
		}
		
		Assert.assertTrue(
			"SentencePair not found: "+expAlignment+"\n"+alignments,
			found);
		
		return this;
	}

	public AssertGistPrepareContentResult hasNoContentForLang(String lang) {
		List<String[]> gotContent = null;
		if (lang.equals("iu")) {
			gotContent = result().iuSentences;
		} else if (lang.equals("en")) {
			gotContent = result().enSentences;
		}
		Assert.assertEquals(
			"There should not have been any content for language "+lang, 
			new ArrayList<String[]>(), gotContent);
		
		return this;		
	}

	public AssertGistPrepareContentResult hasContentForLang(String lang) {
		List<String[]> gotContent = null;
		if (lang.equals("iu")) {
			gotContent = result().iuSentences;
		} else if (lang.equals("en")) {
			gotContent = result().enSentences;
		}

		Assert.assertTrue(
			"There should have been any content for language "+lang,
			gotContent.size() > 0);

		return this;
	}

	public AssertGistPrepareContentResult hasNoAlignments() {
		boolean gotAvailable = result().getAlignmentsAvailable();
		Assert.assertFalse("Alignments should NOT have been available", gotAvailable);
		return this;
	}

	public AssertGistPrepareContentResult hasSomeAlignments() {
		boolean gotAvailable = result().getAlignmentsAvailable();
		Assert.assertTrue("Alignments SHOULD have been available", gotAvailable);
		return this;
	}

	public AssertGistPrepareContentResult couldNotFetchIUContent() throws Exception {
		AssertObject.assertDeepEquals(
			"Should NOT have been able to fetch content of IU page", 
			new ArrayList<String[]>(), result().iuSentences);
		return this;
	}

	public AssertGistPrepareContentResult couldNotFetchEnContent() 
			throws Exception {
		AssertObject.assertDeepEquals(
				"Should NOT have been able to fetch content of EN page", 
				new ArrayList<String[]>(), result().enSentences);
		return this;
	}

	public AssertGistPrepareContentResult containsIUSentenceStartingWith(String expSentence) {
		boolean found = false;
		String mess = baseMessage+"SentencePair did not contain IU sentence that starts with: "+
				expSentence+"\nIU sentence were:\n";
		for (String[] aSentTokens: result().iuSentences) {
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

	public AssertGistPrepareContentResult raisesError(String expError) {
		return raisesError("", expError);
	}

	public AssertGistPrepareContentResult raisesError(String mess, String expError) {
		AssertString.assertStringEquals(
			baseMessage+"\n"+mess,
			expError, result().errorMessage);
		return this;
	}
}
