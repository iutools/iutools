package org.iutools.webservice.relatedwords;


import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.morphrelatives.MorphologicalRelative;
import org.iutools.webservice.IUTServiceTestHelpers;

import java.util.ArrayList;
import java.util.List;

public class AssertRelatedWordsResponse extends Asserter<MockHttpServletResponse> {
	public AssertRelatedWordsResponse(MockHttpServletResponse _gotObject) {
		super(_gotObject);
	}

	public AssertRelatedWordsResponse(MockHttpServletResponse _gotObject, String mess) {
		super(_gotObject, mess);
	}

	protected RelatedWordsResponse response() throws Exception {
		RelatedWordsResponse resp =
			IUTServiceTestHelpers.toRelatedWordsResponse(this.gotObject);
		return resp;
	}

	public AssertRelatedWordsResponse relatedWordsAre(String... expWords)
		throws Exception {
		MorphologicalRelative[] gotWordsObj = response().relatedWords;
		List<String> gotWords = new ArrayList<String>();
		for (MorphologicalRelative aRelObj: gotWordsObj) {
			gotWords.add(aRelObj.getWord());
		}
		AssertObject.assertDeepEquals(
			baseMessage+"\nList of related words was not as expected",
			expWords, gotWords);

		return this;
	}
}
