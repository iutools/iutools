package org.iutools.webservice.search;

import ca.nrc.testing.AssertString;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;

public class AssertExpandQueryResult extends AssertEndpointResult  {
	public AssertExpandQueryResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertExpandQueryResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertExpandQueryResult expandedQueryIs(String expQuery) throws Exception {
		AssertString.assertStringEquals(
			baseMessage+"\nExpanded query not as expected for orig query:'"+result().origQuery+"'",
			expQuery, result().expandedQuery);

		return this;
	}

	private ExpandQueryResult result() {
		return (ExpandQueryResult)gotObject;
	}
}
