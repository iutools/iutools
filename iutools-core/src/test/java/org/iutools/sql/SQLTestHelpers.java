package org.iutools.sql;

import ca.nrc.json.PrettyPrinter;
import org.junit.jupiter.api.Assertions;

import java.util.Map;

public class SQLTestHelpers {

	public static final String defaultTestDB = "iutools_test_db";

	public static void assertIsFaster(String operation, String fasterImpl, Map<String, Long> times) {
		System.out.println("Running times for operation "+operation);
		System.out.println(new PrettyPrinter().pprint(times));
		String slowerImpl = "sql";
		if (fasterImpl.equals("sql")) {
			slowerImpl = "es";
		}
		long fasterTime = times.get(fasterImpl);
		long slowerTime = times.get(slowerImpl);
		Assertions.assertTrue(
			fasterTime <= slowerTime,
			fasterImpl+" should have been faster than "+slowerImpl+"\n"+
			"NOTE: This test can fail intermittently if the machine was busier at the time\n"+
			"  when the "+fasterImpl+" implementation was run.");
	}

}
