package org.iutools.webservice.gist;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import ca.inuktitutcomputing.morph.Gist;
import ca.inuktitutcomputing.utilities.Alignment;
import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertObject;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.testing.Asserter;
import org.iutools.webservice.IUTServiceTestHelpers;

public class GistWordAsserter extends Asserter {

	public static GistWordAsserter assertThat(MockHttpServletResponse response, 
			String mess) throws Exception {
		return new GistWordAsserter(response, mess);
	}
	
	public GistWordAsserter(
			MockHttpServletResponse gotResponse, String mess) throws Exception {
		super(gotResponse, mess, GistWordResponse.class);
		gotObject = IUTServiceTestHelpers.toGistWordResponse(gotResponse);
	}

	public GistWordAsserter gistMorphemesEqual(String[] expMorphemes) throws Exception {
		Gist gotGist = getGist();
		List<String> gotMorphemes = new ArrayList<String>();
		
		for (Pair<String, String> morpheme: gotGist.wordComponents) {
			gotMorphemes.add(morpheme.getFirst());
		}
		AssertObject.assertDeepEquals(
			"Gist of word "+getInputWord()+" was not as expecte", 
			expMorphemes, gotMorphemes);
		return this;
	}

	private String getInputWord() {
		String word = getGistWordResponse().inputWord;
		return word;
	}

	private Gist getGist() {
		Gist gist = getGistWordResponse().wordGist;
		return gist;
	}

	private GistWordResponse getGistWordResponse() {
		return (GistWordResponse) this.gotObject;	
	}

	public void nthAlignmentIs(int nn, String lang1, String text1, 
			String lang2, String text2) throws Exception {
		if (getAlignments().length < nn+1) {
			Assert.fail(
				baseMessage+"\nAlignment results contained less than "+
						nn+" alignments.\n"+"Alignments were: \n"+
						PrettyPrinter.print(getAlignments()));
		}
		Alignment nthAlignment = getAlignments()[nn-1];
		String[] gotAlignment = new String[] {
			nthAlignment.sentences.get(lang1),
			nthAlignment.sentences.get(lang2)
		};
		String[] expAlignment = new String[] {text1, text2};
		AssertObject.assertDeepEquals(
				baseMessage+"\nAlignment #"+nn+" was not as expected.", 
				expAlignment, gotAlignment);
	}

	private Alignment[] getAlignments() {
		Alignment[] alignments = getGistWordResponse().alignments;
		return alignments;
	}
}
