package org.iutools.morphemedict;

import java.util.ArrayList;
import java.util.List;

import ca.nrc.testing.AssertSequence;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;

public class AssertMorphSearchResults extends Asserter<List<MorphDictionaryEntry>> {

	public AssertMorphSearchResults(List<MorphDictionaryEntry> _gotResults, String mess) {
		super(_gotResults, mess);
	}

	protected List<MorphDictionaryEntry> results() {
		return gotObject;
	}

	public AssertMorphSearchResults examplesForMorphemeStartWith(
		String morpheme, Pair<String,Long>... expWordExamples) throws Exception {
		List<Pair<String,Long>> gotWordExamples = null;
		for (MorphDictionaryEntry aMorphResult: results()) {
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
			Pair<String,Long>[] gotWordExamplesArr = new Pair[gotWordExamples.size()];
			for (int ii=0; ii < gotWordExamples.size(); ii++) {
				gotWordExamplesArr[ii] = gotWordExamples.get(ii);
			}
			new AssertSequence(gotWordExamplesArr,
				baseMessage+"\nExamples of words for morpheme "+morpheme+" were wrong")
				.startsWith(expWordExamples);
				;
//			AssertObject.assertDeepEquals(
//				baseMessage+"\nExamples of words for morpheme "+morpheme+" were wrong",
//				expWordExamples, gotWordExamples);
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
		for (MorphDictionaryEntry aMorphResult: results()) {
			morphemes.add(aMorphResult.morphemeWithId);
		}
		return morphemes;
	}
}
