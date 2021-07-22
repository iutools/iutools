package org.iutools.webservice.worddict;

import ca.nrc.testing.AssertString;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;

public class AssertWordDictResult extends AssertEndpointResult {

	@Override
	protected WordDictResult result() {
		return (WordDictResult)gotObject;
	}

	public AssertWordDictResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertWordDictResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}
}
