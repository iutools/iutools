package ca.pirurvik.iutools.concordancer;

import java.net.URL;
import java.util.List;

import org.junit.Assert;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;

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
			if (anAlignment.toString().equals(expAlignment.toString())) {
				errMess += "  "+anAlignment.toString()+"\n";
				found = true;
				break;
			}
		}
		
		
		Assert.assertTrue(errMess, found);			
	}

	public DocAlignmentAsserter wasSuccessful() {
		Assert.assertTrue(
				baseMessage+"\nAlignment should have been successful.",
				gotDocAlignment.success);
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
}
