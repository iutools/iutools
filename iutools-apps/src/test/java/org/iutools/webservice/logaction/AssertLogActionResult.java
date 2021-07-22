package org.iutools.webservice.logaction;

import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.tokenize.TokenizeResult;
import org.junit.jupiter.api.Assertions;

public class AssertLogActionResult extends AssertEndpointResult {

	@Override
	protected LogActionResult result() {
		return (LogActionResult)gotObject;
	}

	public AssertLogActionResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertLogActionResult(EndpointResult _result) {
		super(_result);
	}

	public AssertLogActionResult taskIDIsSet() {
		Assertions.assertNotNull(result().taskID,
			"The Log service should have created a task ID for that action");
		return this;
	}

	public AssertLogActionResult taskIDIsNot(String otherID, String mess) {
		Assertions.assertNotEquals(
			result().taskID, otherID,
			baseMessage+"\nThe two task IDs should have differed"
		);
		return this;
	}

	public AssertLogActionResult taskIDequals(String expID, String mess) {
		Assertions.assertEquals(expID, result().taskID,
			baseMessage+"\n"+mess+
				"\nThe task ID was not as expected");
		return this;
	}
}
