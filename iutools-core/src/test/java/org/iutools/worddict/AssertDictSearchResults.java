package org.iutools.worddict;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertSequence;
import ca.nrc.testing.Asserter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class AssertDictSearchResults
	extends Asserter<Pair<List<String>, Long>> {
	public AssertDictSearchResults(Pair<List<String>, Long> _gotObject) {
		super(_gotObject);
	}

	public AssertDictSearchResults(Pair<List<String>, Long> _gotObject, String mess) {
		super(_gotObject, mess);
	}

	protected Pair<List<String>, Long> results() {
		return (Pair<List<String>, Long>)gotObject;
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
		String[] gotIDs = results().getLeft().toArray(new String[0]);
		for (int ii=0; ii < gotIDs.length; ii++) {
			gotIDs[ii] = Document.removeType(gotIDs[ii]);
		}
		new AssertSequence<String>(gotIDs,
			baseMessage+"\nSearch results did not start with the expected words")
			.startsWith(expTopIDs);
		return this;
	}
}
