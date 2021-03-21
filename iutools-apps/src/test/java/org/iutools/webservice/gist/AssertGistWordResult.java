package org.iutools.webservice.gist;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertObject;
import org.iutools.utilities.Alignment;
import org.iutools.morph.Gist;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;

import ca.nrc.datastructure.Pair;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

public class AssertGistWordResult extends AssertEndpointResult  {
	public AssertGistWordResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertGistWordResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	protected GistWordResult result() {
		return (GistWordResult)gotObject;
	}

	public AssertGistWordResult gistMorphemesEqual(String[] expMorphemes) throws Exception {
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
		String word = result().inputWord;
		return word;
	}

	private Gist getGist() {
		Gist gist = result().wordGist;
		return gist;
	}


	public void nthAlignmentIs(int nn, String lang1, String text1,
			String lang2, String text2) throws Exception {
		if (getAlignments().length < nn+1) {
			Assertions.fail(
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
		Alignment[] alignments = result().alignments;
		return alignments;
	}
}
