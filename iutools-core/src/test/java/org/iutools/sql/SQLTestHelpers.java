package org.iutools.sql;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertObject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class SQLTestHelpers {

	public static final String defaultTestDB = "iutools_test_db";

	private static boolean connPoolOverheadEncured = false;

	private static DecimalFormat fmtPercent = new DecimalFormat("#%");


	public static void assertSqlNotSignificantlySlowerThanES(String operation,
		Map<String, Double> times) {
		assertSqlNotSignificantlySlowerThanES(operation, times, (Double)null);
	}

	public static void assertSqlNotSignificantlySlowerThanES(String operation,
		Map<String, Double> times, Double tolerance) {
		if (tolerance == null) {
			tolerance = 0.0;
		}
		System.out.println("Running times for operation "+operation);
		System.out.println(new PrettyPrinter().pprint(times));
		double sqlTime = times.get("sql");
		double esTime = times.get("es");
		double gotDelta = sqlTime - esTime;
		double gotDetalPerc = gotDelta / esTime;
		double maxDelta = tolerance * esTime;
		DecimalFormat fmtPercent = new DecimalFormat("#%");

		String deltaStats =
			"  SQL time  : "+sqlTime+"\n"+
			"  ES time   : "+esTime+"\n"+
			"  Got delta : "+gotDelta+" ("+fmtPercent.format(gotDetalPerc)+")\n";
		System.out.println(deltaStats);

		Assertions.assertTrue(
			gotDelta < maxDelta,
			"SQL implementation was slower than ES by more than "+
			deltaStats+
			"NOTE: This assertion can fail intermittently if the machine was busier at the time\n"+
			"  when the SQL implementation was run.");
	}

	public static void assertSqlNotSignificantlySlowerThanES_NEW(
		String operation, Map<String, TimingResults> times) {
		assertSqlNotSignificantlySlowerThanES_NEW(operation, times, (Double)null);
	}

	public static void assertSqlNotSignificantlySlowerThanES_NEW(
		String operation, Map<String, TimingResults> times, Double tolerance) {
		if (tolerance == null) {
			tolerance = 0.0;
		}
		TimingResults timesSql = times.get("sql");
		double sqlTime = timesSql.msecsPerCase();
		TimingResults timesES = times.get("es");
		double esTime = timesES.msecsPerCase();
		double gotDelta = sqlTime - esTime;
		double gotDetalPerc = gotDelta / esTime;
		double maxDelta = tolerance * esTime;
		DecimalFormat fmtPercent = new DecimalFormat("#%");

		String deltaStats =
			"  SQL time  : "+sqlTime+"\n"+
			"  ES time   : "+esTime+"\n"+
			"  Got delta : "+gotDelta+" ("+fmtPercent.format(gotDetalPerc)+")\n";
		System.out.println(deltaStats);

		if (gotDelta > maxDelta) {
			timesSql.reportSlowerCases(timesES);
		}

		Assertions.assertTrue(
			gotDelta <= maxDelta,
			"SQL implementation was slower than ES by more than "+
			deltaStats+
			"NOTE: This assertion can fail intermittently if the machine was busier at the time\n"+
			"  when the SQL implementation was run.");
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
		ResultSetWrapper rsw = new QueryProcessor().query(sql);
		return Pair.of(rsw.statement, rsw.rs);
	}

	/**
	 * Open a ResultSetWrapper for testing purposes.
	 */
	public static ResultSetWrapper openResultSetWrapper() throws Exception {
		String sql = "SHOW TABLES;";
		ResultSetWrapper rsw = new QueryProcessor().query(sql);
		return rsw;
	}

	public static class TimingResults {
		public Map<String,Long> casesMsecs = new HashMap<String,Long>();
		public long totalMsecs = 0;

		// Note: The total number of instances may be greater than the total
		//  number of unique cases (because some cases may be evaluated more
		//  than once).
		public long totalInstances = 0;

		public void onNewCase(String caseName, long caseMsecs) {
			if (!casesMsecs.containsKey(caseName)) {
				casesMsecs.put(caseName, new Long(0));
			}
			casesMsecs.put(caseName, casesMsecs.get(caseName) + caseMsecs);
			totalMsecs += caseMsecs;
			totalInstances++;
		}

		public double msecsPerCase() {
			return 1.0 * totalMsecs / totalInstances;
		}

		public void reportSlowerCases(TimingResults otherResults) {
			reportSlowerCases(otherResults, (Double)null);

		}

		public void reportSlowerCases(TimingResults otherResults, Double tolerance) {
			if (tolerance == null) {
				tolerance = 0.0;
			}
			Map<String,Double> slowCases = new HashMap<String,Double>();
			for (String aCase: casesMsecs.keySet()) {
				Long thisTime = this.casesMsecs.get(aCase);
				Long otherTime = otherResults.casesMsecs.get(aCase);
				Long delta = (thisTime - otherTime);
				double deltaRelative = 1.0* delta / otherTime;
				if (delta > tolerance) {
					slowCases.put(aCase, deltaRelative);
				}
			}
			Stream<Map.Entry<String,Double>> sortedSlowCases =
				slowCases.entrySet().stream()
					.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
			System.out.println("Significantly slower cases below (in decreasing order of speed):");
			for (Map.Entry<String,Long> aCase: sortedSlowCases.toArray(Map.Entry[]::new)) {
					System.out.println(
						"  "+aCase.getKey()+": was longer by "+
						fmtPercent.format(aCase.getValue())+
						" > " + fmtPercent.format(tolerance));
			}

		}



	}
}
