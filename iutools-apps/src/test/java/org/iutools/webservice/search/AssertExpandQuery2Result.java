package org.iutools.webservice.search;

import ca.nrc.testing.AssertString;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;

public class AssertExpandQuery2Result extends AssertEndpointResult  {
	public AssertExpandQuery2Result(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertExpandQuery2Result(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertExpandQuery2Result expandedQueryIs(String expQuery) throws Exception {
		AssertString.assertStringEquals(
			baseMessage+"\nExpanded query not as expected for orig query:'"+result().origQuery+"'",
			expQuery, result().expandedQuery);

		return this;
	}

	private ExpandQuery2Result result() {
		return (ExpandQuery2Result)gotObject;
	}
}
