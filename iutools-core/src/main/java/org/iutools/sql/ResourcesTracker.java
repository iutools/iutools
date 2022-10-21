package org.iutools.sql;

import org.apache.logging.log4j.Logger;
import org.iutools.utilities.StopWatch;
import org.iutools.utilities.StopWatchException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Keeps track of "managed" SQL resources (ResultSet, Statement).
 * By "managed", we mean resources that are created by classes in our SQL framework,
 * more precisely:
 *
 * - QueryProcessor
 * - ResultSetWrapper
 * - ResultSetIterator
 * - ResultSetColIterator
 */
public class ResourcesTracker {

	private static Set<Statement> openedStatements = new HashSet<Statement>();
	private static Set<ResultSet> openedResultSets = new HashSet<ResultSet>();

	private static StopWatch sleepTimeTracker = new StopWatch();

	public static synchronized void updateResourceStatus(Statement stmt) {
		if (stmt != null) {
			try {
				if (stmt.isClosed()) {
					openedStatements.remove(stmt);
				} else {
					openedStatements.add(stmt);
				}
			} catch (SQLException throwables) {
				// Nothing to do
			}
		}
	}

	public static synchronized void updateResourceStatus(ResultSet rs) {
		if (rs != null) {
			try {
				if (rs.isClosed()) {
					openedResultSets.remove(rs);
				} else {
					openedResultSets.add(rs);
				}
			} catch (SQLException throwables) {
				// Nothing to do
			}
		}
	}

	public static synchronized int totalResultSets() {
		return totalResultSets((Boolean)null);
	}

	public static synchronized int totalResultSets(Boolean doubleCheck) {
		if (doubleCheck == null) {
			doubleCheck = false;
		}
		if (doubleCheck) {
			doubleCheckResultSets();
		}
		return openedResultSets.size();
	}

	public static synchronized int totalStatements() {
		return totalStatements((Boolean)null);
	}

	public static synchronized int totalStatements(Boolean doubleCheck) {
		if (doubleCheck == null) {
			doubleCheck = false;
		}
		if (doubleCheck) {
			doubleCheckStatements();
		}
		return openedStatements.size();
	}

	private static void doubleCheckStatements() {
		// Double check that the Statements we have in our tracked sets are actually
		// still open
		possiblySleep();
		Set<Statement> closedStmts = new HashSet<Statement>();
		for (Statement stmt: openedStatements) {
			try {
				if (stmt.isClosed()) {
					closedStmts.add(stmt);
				}
			} catch (SQLException throwables) {
			}
		}
		for (Statement stmt: closedStmts) {
			openedStatements.remove(stmt);
		}
	}

	private static void doubleCheckResultSets() {
		// Double check that the ResultSets we have in our tracked sets are actually
		// still open
		possiblySleep();
		Set<ResultSet> closedRS = new HashSet<ResultSet>();
		for (ResultSet rs: openedResultSets) {
			try {
				if (rs.isClosed()) {
					closedRS.add(rs);
				}
			} catch (SQLException throwables) {
			}
		}
		for (ResultSet rs: closedRS) {
			openedResultSets.remove(rs);
		}
	}

	private synchronized static void possiblySleep() {
		// Sleep a bit to give resources time to close.
		// Only do this if we haven't sleeped in a while
		try {
			if (sleepTimeTracker.totalTime(TimeUnit.SECONDS) < 1) {
				sleepTimeTracker.start();
				Thread.sleep(1000);
			}
		} catch (StopWatchException | InterruptedException e) {
		}
	}

	public static synchronized void traceResourceCounts(String mess, Logger logger) {
		if (logger.isTraceEnabled()) {
			mess += "\n" +
				"  Number of open SQL resources:\n" +
				"    Statement : "+totalStatements()+"\n"+
				"    ResultSet : "+totalResultSets();
		}
	}

}
