package org.iutools.sql;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertObject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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
		String operation, Map<String, Double> times, double tolerance) throws Exception {
		System.out.println("Running times for operation "+operation);
		System.out.println(new PrettyPrinter().pprint(times));

		// First, find the fastest implementaiton.
		double fastestTime = Long.MAX_VALUE;
		String fastestImpl = null;
		for (String implName: times.keySet()) {
			double implTime = times.get(implName);
			if (implTime < fastestTime) {
				fastestImpl = implName;
				fastestTime = implTime;
			}
		}
		System.out.println("Fastest implentation was: "+fastestImpl+"="+fastestTime);

		// Then make sure that all implementations are within tolerance of
		// the fastest implementation
		for (String implName: times.keySet()) {
			double implTime = times.get(implName);
			System.out.println("  Checking speed of implementation "+implName);
//			AssertNumber.performanceHasNotChanged(operation, implTime, fastestImpl, tolerance);
			AssertNumber.performanceHasNotChanged(
				operation, 1.0*implTime, 1.0*fastestTime, tolerance);
		}
	}


	public static void assertNoSlowerThan(
		String operation, String implOfConcern, Map<String, Double> times, double tolerance) throws Exception {
		System.out.println("Running times for operation "+operation);
		System.out.println(new PrettyPrinter().pprint(times));

		// First, find the fastest implementaiton.
		double fastestTime = Long.MAX_VALUE;
		String fastestImpl = null;
		for (String implName: times.keySet()) {
			double implTime = times.get(implName);
			if (implTime < fastestTime) {
				fastestImpl = implName;
				fastestTime = implTime;
			}
		}
		System.out.println("Fastest implentation was: "+fastestImpl+"="+fastestTime);

		// Then make sure that the implementation of concern is not "too" slow
		// compared to the fastest one.
		double implOfConcernTime = times.get(implOfConcern);
		double delta = (implOfConcernTime - fastestTime) / fastestTime;
		System.out.println("Delta of "+implOfConcern+" w.r.t "+fastestImpl+": "+delta);
		if (delta > 0 && Math.abs(delta) > tolerance) {
			Assertions.fail("Implementation "+implOfConcern+" was slower than "+fastestImpl+" a factor of "+delta+" (max allowed: "+tolerance+").");
		}
	}

	public static void assertOpenedResourcesAre(String mess,
		int expStatements, int expResultSets) throws Exception {
		Map<String,Integer> expResources = new HashMap<String,Integer>();
		expResources.put("Statement", expStatements);
		expResources.put("ResultSet", expResultSets);

		Map<String,Integer> gotResources = new HashMap<String,Integer>();
		gotResources.put("Statement", ResourcesTracker.totalStatements());
		gotResources.put("ResultSet", ResourcesTracker.totalResultSets());

		AssertObject.assertDeepEquals(mess,
			expResources, gotResources);
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

	/**
	 * Open a Statement and ResultSet for testing purposes.
	 * NOTE: We build them using QueryProcessor.query() so that the resources
	 *   are "managed" (i.e. tracked by ResourcesTracker).
	 */
	public static Pair<Statement, ResultSet> openManagedResources() throws Exception {
		String sql = "SHOW TABLES;";
		ResultSetWrapper rsw = new QueryProcessor().query3(sql);
		return Pair.of(rsw.statement, rsw.rs);
	}

	/**
	 * Open a ResultSetWrapper for testing purposes.
	 */
	public static ResultSetWrapper openResultSetWrapper() throws Exception {
		String sql = "SHOW TABLES;";
		ResultSetWrapper rsw = new QueryProcessor().query3(sql);
		return rsw;
	}
}
