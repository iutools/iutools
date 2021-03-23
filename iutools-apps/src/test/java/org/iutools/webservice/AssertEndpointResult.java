package org.iutools.webservice;

import ca.nrc.json.MapperFactory;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;

public class AssertEndpointResult extends Asserter<EndpointResult> {
	public AssertEndpointResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertEndpointResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertEndpointResult jsonEquals(String expJson) throws Exception {
		String gotJson =
			MapperFactory
				.mapper(MapperFactory.MapperOptions.SORT_FIELD)
				.writeValueAsString(gotObject);
		AssertString.assertStringEquals(
			baseMessage+"\nJSON not as expected",
			expJson, gotJson
		);
		return this;
	}
}