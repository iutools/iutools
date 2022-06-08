package org.iutools.webservice.gist;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertObject;
import org.iutools.concordancer.SentencePair;
import org.iutools.morph.Gist;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;

import ca.nrc.datastructure.Pair;

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

	private SentencePair[] getAlignments() {
		SentencePair[] alignments = result().alignments;
		return alignments;
	}

	public AssertGistWordResult mostAlignmentsContains(String lang, String... expressions) {
		return mostAlignmentsContains(lang, 0.0, expressions);
	}

	public AssertGistWordResult mostAlignmentsContains(String lang, double tolerance,
		String... expressions) {
		SentencePair[] alignments = result().alignments;
		int totalAlignments = alignments.length;
		List<SentencePair> faultyAlignments = new ArrayList<SentencePair>();
		for (SentencePair anAlignment: alignments) {
			String sentence = anAlignment.getText(lang).toLowerCase();
			boolean found = false;
			for (String expr: expressions) {
				expr = expr.toLowerCase();
				if (sentence.contains(expr)) {
					found = true;
					break;
				}
			}
			if (!found) {
				faultyAlignments.add(anAlignment);
			}
		}

		double faultyRatio = 0.0;
		if (totalAlignments > 0) {
			faultyRatio = 1.0 * faultyAlignments.size() / totalAlignments;
		}
		String mess =
			"Too many alignments (ratio="+faultyRatio+") were missing an expected expression on the '"+lang+"' side.\n"+
			"Exp expression: "+String.join(", ", expressions)+"\n"+
			"Faulty alignments:\n"+PrettyPrinter.print(faultyAlignments)+"\n"
			;
		AssertNumber.isLessOrEqualTo(mess, faultyRatio, tolerance);

		return this;
	}
}
