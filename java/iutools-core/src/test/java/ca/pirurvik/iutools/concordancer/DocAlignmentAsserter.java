package ca.pirurvik.iutools.concordancer;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

import ca.nrc.datastructure.Pair;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import ca.pirurvik.iutools.concordancer.DocAlignment.Problem;

public class DocAlignmentAsserter {
	private static final DocAlignment pageAlignment = null;

	String baseMessage = "";
	DocAlignment gotDocAlignment = null;
	
	
	public static DocAlignmentAsserter assertThat(
			DocAlignment pageAligment) {
		DocAlignmentAsserter asserter =  
			new DocAlignmentAsserter(pageAligment);
		return asserter;
	}

	public static DocAlignmentAsserter assertThat(
			DocAlignment pageAligment,  String _baseMess) {
		DocAlignmentAsserter asserter = 
			new DocAlignmentAsserter(pageAligment, _baseMess);
		return asserter;
	}
	
	public DocAlignmentAsserter(DocAlignment pageAligment) {
		this.gotDocAlignment = pageAligment;
	}

	public DocAlignmentAsserter(
			DocAlignment _pageAligment, String _baseMess) {
		init__AlignmentResultAsserter(_pageAligment, _baseMess);
	}

	private void init__AlignmentResultAsserter(
			DocAlignment _pageAligment, String _baseMess) {
		this.baseMessage = _baseMess;
		this.gotDocAlignment = _pageAligment;
	}

	public void containsAlignment(Alignment expAlignment) {
		
		boolean found = false;
		String errMess =
			"Alignments did not contain an expected alignment.\n"+
			"Expected: "+expAlignment.toString()+"\n"+
			"Got:\n";
				
		for (Alignment anAlignment: gotDocAlignment.alignments) {
			errMess += "  "+anAlignment.toString()+"\n";
			if (anAlignment.toString().equals(expAlignment.toString())) {
				found = true;
				break;
			}
		}
		
		Assert.assertTrue(errMess, found);			
	}

	public DocAlignmentAsserter wasSuccessful() {
		return wasSuccessful(true);
	}

	public DocAlignmentAsserter wasSuccessful(boolean expSuccess) {
		Assert.assertEquals(
				baseMessage+"\nSuccess status of alignments was not as expected.",
				expSuccess, gotDocAlignment.success);
		return this;
	}
	
	

	public void alignmentsEqual(String mess, String lang1, String lang2, 
			Pair<String, String>[] expAlPairs) throws Exception {

		String[] expAlStrs = new String[expAlPairs.length];
		for (int ii=0; ii < expAlPairs.length; ii++) {
			expAlStrs[ii] = 
					"(" +
					lang1 + ":" + expAlPairs[ii].getFirst() + 
					" <--> " +
					lang2 + ":" + expAlPairs[ii].getSecond() + 
					")";
		}
		List<Alignment> gotAlList = this.gotDocAlignment.getAligments();
		String[] gotAlStrs = new String[gotAlList.size()];
		for (int ii=0; ii < gotAlList.size(); ii++) {
			gotAlStrs[ii] = gotAlList.get(ii).toString();
		}
		
		AssertObject.assertDeepEquals(
				"Alignments texts were not as expected.", 
				expAlStrs, gotAlStrs);
	}

	public DocAlignmentAsserter urlForLangEquals(
				String lang, URL expURL) throws Exception {
		URL gotURL = gotDocAlignment.getPageURL(lang);
		AssertString.assertStringEquals(
				"URL of the "+lang+" page was not as expected.",
				expURL.toString(), gotURL.toString());;
		return this;
	}

	public DocAlignmentAsserter pageInLangContains(String lang, String expText) {
		String gotText = gotDocAlignment.getPageContent(lang);
		AssertString.assertStringContains(
				baseMessage+"\nContent of the "+lang+" page was not as expected", 
				gotText, expText);	
		return this;
	}

	public DocAlignmentAsserter contentIsPlainText(String... langs) {
		
		for (String aLang: langs) {
			String langContent = gotDocAlignment.getPageContent(aLang);
			
			Matcher matcher = Pattern.compile("(</([^>])>)").matcher(langContent);
			if (matcher.find() && matcher.group(0).length() < 20) {
				Assert.fail(
					"Content for language "+aLang+" contained HTML tags\n"+
					"First tag found: "+matcher.group(0));
			}
		}
		return this;
	}

	public DocAlignmentAsserter hasNoContentForLang(String lang) {
		String content = gotDocAlignment.getPageContent(lang);
		Assert.assertEquals(
			"Alignment should not have had content for language "+lang,
			null, content);
		
		return this;
	}

	public DocAlignmentAsserter hasNoAlignments() {
		List<Alignment> gotAlignments = gotDocAlignment.alignments;
		Assert.assertEquals(
			"Document alignment should not have contained any alignments", 
			0, gotAlignments.size());
		return this;
	}
	
	public DocAlignmentAsserter containsSentenceStartingWith(String lang, String expSent) {
		String errMess = 
			"Could not find sentences for lang="+lang+": "+expSent+"\n"+
			"Sentences were:\n"
			;
		boolean found = false;
		for (String gotSent: gotDocAlignment.getPageSentences(lang)) {
			if (gotSent.startsWith(expSent)) {
				found = true;
				break;
			}
			errMess += "   '"+gotSent+"'\n";
		}
		
		Assert.assertTrue(errMess, found);
		
		return this;
	}
	

	public DocAlignmentAsserter encounteredProblems(Problem[] expProblems) {
		
		Iterator<DocAlignment.Problem> probIter = 
				gotDocAlignment.problemsEncountered.keySet().iterator();
		
			String encounteredProbsMess = 
				"'\nProblems actually encountered were:\n   "+
				StringUtils.join(probIter, "\n   ")+"\n";
		
		for (Problem probDescr: expProblems) {
			String errMess = 
				baseMessage+"\nShould have encountered problem '"+probDescr+
				encounteredProbsMess;

			Assert.assertTrue(
				errMess,
				gotDocAlignment.problemsEncountered.get(probDescr) != null);
		}
		return this;
	}

	private Problem[] toProblemObjects(String[] problemsStr) {
		Problem[] problems = new Problem[problemsStr.length];
		for (int ii=0; ii < problemsStr.length; ii++) {
			Problem iithProblem = Problem.valueOf(problemsStr[ii]);
			problems[ii] = iithProblem;
		}
			
		return problems;
	}
}
