package ca.pirurvik.iutools.morphemesearcher;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;

public class AssertMorphSearchResults extends Asserter<List<MorphSearchResults>> {

	public AssertMorphSearchResults(List<MorphSearchResults> _gotResults, String mess) {
		super(_gotResults, mess);
	}

	protected List<MorphSearchResults> results() {
		return gotObject;
	}

	public AssertMorphSearchResults examplesForMorphemeAre(
		String morpheme, Pair<String,Long>... expWordExamples) throws Exception {
		List<Pair<String,Long>> gotWordExamples = null;
		for (MorphSearchResults aMorphResult: results()) {
			String aMorpheme = aMorphResult.morphemeWithId;
			if (aMorpheme.equals(morpheme)) {
				gotWordExamples = new ArrayList<Pair<String,Long>>();
				List<ScoredExample> scoredExamples = aMorphResult.words;
				for (ScoredExample anExample: scoredExamples) {
					gotWordExamples.add(Pair.of(anExample.word, anExample.frequency));
				}
				break;
			}
		}
		
		if (gotWordExamples == null) {
			Assert.fail(
				baseMessage+
				"\nNo examples found for morpheme "+morpheme+
				"\nMorphemes found were: "+PrettyPrinter.print(gotMorphemes()));
		} else {
			AssertObject.assertDeepEquals(
				baseMessage+"\nExamples of words for morpheme "+morpheme+" were wrong",
				expWordExamples, gotWordExamples);
		}
		
		return this;
	}

	public AssertMorphSearchResults foundMorphemes(String... expMorphemes) 
		throws Exception {
		AssertObject.assertDeepEquals(
			baseMessage+"\nList of morphemes found was not as expected.", 
			expMorphemes, gotMorphemes());
		return this;
	}
	
	protected List<String> gotMorphemes() {
		List<String> morphemes = new ArrayList<String>();
		for (MorphSearchResults aMorphResult: results()) {
			morphemes.add(aMorphResult.morphemeWithId);
		}
		return morphemes;
	}
}
