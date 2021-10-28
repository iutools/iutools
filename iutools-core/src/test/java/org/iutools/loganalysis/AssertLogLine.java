package org.iutools.loganalysis;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;

import java.util.HashSet;
import java.util.Set;

public class AssertLogLine extends Asserter<LogLine> {
	public AssertLogLine(LogLine _gotObject) {
		super(_gotObject);
	}

	public AssertLogLine(LogLine _gotObject, String mess) {
		super(_gotObject, mess);
	}

	protected LogLine line() {
		return (LogLine)gotObject;
	}

	public AssertLogLine isEqualTo(LogLine expLine) throws Exception {
		Assertions.assertEquals(expLine.getClass(), line().getClass(),
			"Log line generated the wrong class of object");

		Set<String> ignoreFields = new HashSet<String>();
		ignoreFields.add("json");
		AssertObject.assertDeepEquals(
			"Object generated from log line was not as expected.",
			expLine, line(), ignoreFields, 0);
		return this;
	}

}
