package org.iutools.webservice.log;

import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.junit.jupiter.api.Assertions;

public class AssertLogResult extends AssertEndpointResult {
	public AssertLogResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertLogResult(EndpointResult _result) {
		super(_result);
	}

	public AssertLogResult taskIDIsSet() {
		Assertions.assertNotNull(result().taskID,
			"The Log service should have created a task ID for that action");
		return this;
	}

	public AssertLogResult taskIDIsNot(String otherID, String mess) {
		Assertions.assertNotEquals(
			result().taskID, otherID,
			baseMessage+"\nThe two task IDs should have differed"
		);
		return this;
	}

	public AssertLogResult taskIDequals(String expID, String mess) {
		Assertions.assertEquals(expID, result().taskID,
			baseMessage+"\n"+mess+
				"\nThe task ID was not as expected");
		return this;
	}

	private LogResult result() {
		return (LogResult)gotObject;
	}
}
