package org.iutools.webservice;

import ca.nrc.json.MapperFactory;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;

public abstract class AssertEndpointResult extends Asserter<EndpointResult> {
	protected abstract EndpointResult result();

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

	public AssertEndpointResult raisesNoError() {
		Assertions.assertEquals(
			null, result().errorMessage,
			baseMessage+"\nResponse raised error");
		return this;
	}
}