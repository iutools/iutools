package org.iutools.sql;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertNumber;
import org.junit.jupiter.api.Assertions;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class SQLTestHelpers {

	public static final String defaultTestDB = "iutools_test_db";

	private static boolean connPoolOverheadEncured = false;

	public static void assertIsFaster(String operation, String fasterImpl,
		Map<String, Double> times) {
		System.out.println("Running times for operation "+operation);
		System.out.println(new PrettyPrinter().pprint(times));
		String slowerImpl = "sql";
		if (fasterImpl.equals("sql")) {
			slowerImpl = "es";
		}
		double fasterTime = times.get(fasterImpl);
		double slowerTime = times.get(slowerImpl);
		Assertions.assertTrue(
			fasterTime <= slowerTime,
			fasterImpl+" should have been faster than "+slowerImpl+"\n"+
			"NOTE: This test can fail intermittently if the machine was busier at the time\n"+
			"  when the "+fasterImpl+" implementation was run.");
	}

	public static void assertAboutSameSpeed(
		String operation, Map<String, Long> times, double tolerance) throws Exception {
		System.out.println("Running times for operation "+operation);
		System.out.println(new PrettyPrinter().pprint(times));

		// First, find the fastest implementaiton.
		long fastestTime = Long.MAX_VALUE;
		String fastestImpl = null;
		for (String implName: times.keySet()) {
			long implTime = times.get(implName);
			if (implTime < fastestTime) {
				fastestImpl = implName;
				fastestTime = implTime;
			}
		}
		System.out.println("Fastest implentation was: "+fastestImpl+"="+fastestTime);

		// Then make sure that all implementations are within tolerance of
		// the fastest implementation
		for (String implName: times.keySet()) {
			long implTime = times.get(implName);
			System.out.println("  Checking speed of implementation "+implName);
//			AssertNumber.performanceHasNotChanged(operation, implTime, fastestImpl, tolerance);
			AssertNumber.performanceHasNotChanged(
				operation, 1.0*implTime, 1.0*fastestTime, tolerance);
		}
	}


	/**
	 * Get and close hundreds of connections from the pool to avoid encurring
	 * pool initialisation overhead during tests.
	 * @throws Exception
	 */
	public synchronized void encurConnectionPoolOverhead() throws Exception {
		if (!connPoolOverheadEncured) {
			ConnectionPool pool = new ConnectionPool();
			for (int ii=0; ii < 500; ii++) {
				try (Connection conn = pool.getConnection()) {
				}
			}
			connPoolOverheadEncured = true;
		}
		return;
	}
}
