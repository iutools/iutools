package org.iutools.worddict;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.testing.AssertCollection;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertSequence;
import ca.nrc.testing.Asserter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AssertDictSearchResults
	extends Asserter<Pair<List<String>, Long>> {
	public AssertDictSearchResults(Pair<List<String>, Long> _results, String mess) {
		super(_results, mess);
	}

	public AssertDictSearchResults(Iterator<String> ids, Long totalHits) {
		super(null);
		init_fromIterator(ids, totalHits);
	}

	private void init_fromIterator(Iterator<String> iter, Long totalHits) {
		List<String> resultsList = new ArrayList<String>();
		final int MAX_HITS = 100;
		int counter = 0;
		while (iter.hasNext() && counter < MAX_HITS) {
			counter++;
			resultsList.add(iter.next());
		}
		this.gotObject = Pair.of(
			resultsList, totalHits
		);
	}

	protected Pair<List<String>, Long> results() {
		return (Pair<List<String>, Long>)gotObject;
	}

	protected String[] resultingWords() {
		String[] gotIDs = results().getLeft().toArray(new String[0]);
		for (int ii=0; ii < gotIDs.length; ii++) {
			gotIDs[ii] = Document.removeType(gotIDs[ii]);
		}
		return gotIDs;
	}

	public AssertDictSearchResults containsAtLeast(Integer expMinWords) {
		AssertNumber.isGreaterOrEqualTo(
			baseMessage+"\nSearch results contained less hits than expected",
			results().getRight(), expMinWords);
		return this;
	}

	public AssertDictSearchResults containsAtMost(Integer expMaxWords) {
		AssertNumber.isLessOrEqualTo(
			baseMessage+"\nSearch results contained more hits than expected",
			results().getRight(), expMaxWords);
		return this;
	}

	public AssertDictSearchResults hitsStartWith(String[] expTopIDs) throws Exception {
		String[] gotWords = resultingWords();
		new AssertSequence<String>(gotWords,
			baseMessage+"\nSearch results did not start with the expected words")
			.startsWith(expTopIDs);
		return this;
	}

	public void containsWords(String... expWords) {
		String[] gotWords = this.resultingWords();
		AssertCollection.assertContainsAll(
			baseMessage+"\nSearch results did not contain the expected words",
			expWords, gotWords
		);
	}
}
