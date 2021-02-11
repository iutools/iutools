package org.iutools.webservice.search;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.webservice.IUTServiceTestHelpers;
import org.iutools.webservice.relatedwords.RelatedWordsResponse;

public class AssertExpandQueryResponse extends Asserter<MockHttpServletResponse> {
	public AssertExpandQueryResponse(MockHttpServletResponse _gotObject) {
		super(_gotObject);
	}

	public AssertExpandQueryResponse(MockHttpServletResponse _gotObject, String mess) {
		super(_gotObject, mess);
	}

	protected ExpandQueryResponse response() throws Exception {
		ExpandQueryResponse resp =
			IUTServiceTestHelpers.toExpandQueryResponse(this.gotObject);
			return resp;
	}

	public void expandedQueryIs(String expQuery) throws Exception {
		ExpandQueryResponse resp = response();
		AssertString.assertStringEquals(
			baseMessage+"\nExpanded query not as expected for orig query:'"+resp.origQuery+"'",
			expQuery, resp.expandedQuery);
	}
}
