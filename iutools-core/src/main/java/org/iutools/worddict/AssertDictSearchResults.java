package org.iutools.worddict;

import ca.nrc.datastructure.CloseableIterator;
import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.testing.AssertCollection;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertSequence;
import ca.nrc.testing.Asserter;

public class AssertDictSearchResults extends Asserter<WordDictSearchResult> {
	public AssertDictSearchResults(WordDictSearchResult _gotObject, String _mess) {
		super(_gotObject, _mess);
	}

	public AssertDictSearchResults(CloseableIterator<String> hitsIter, Long totalHits) {
		super(null, "");
		gotObject = new WordDictSearchResult(null, null);
		while (true) {
			try {
				String hit = hitsIter.next();
				gotObject.hits.add(hit);
			} catch (Exception e) {
				break;
			}
		}
	}

	protected WordDictSearchResult results() {
		return (WordDictSearchResult) gotObject;
	}

protected String[] resultingWords() {
		String[] gotIDs = results().hits.toArray(new String[0]);
		for (int ii=0; ii < gotIDs.length; ii++) {
			gotIDs[ii] = Document.removeType(gotIDs[ii]);
		}
		return gotIDs;
	}

	public AssertDictSearchResults containsAtLeast(Integer expMinWords) {
		AssertNumber.isGreaterOrEqualTo(
			baseMessage+"\nSearch results contained less hits than expected",
			results().hits.size(), expMinWords);
		return this;
	}

	public AssertDictSearchResults containsAtMost(Integer expMaxWords) {
		AssertNumber.isLessOrEqualTo(
			baseMessage+"\nSearch results contained more hits than expected",
			results().hits.size(), expMaxWords);
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
