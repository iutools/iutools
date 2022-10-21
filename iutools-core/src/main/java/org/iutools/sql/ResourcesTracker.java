package org.iutools.sql;

import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Keeps track of opened SQL resources (ResultSet, Statement)
 * Note that this only keeps track of resources we opened/closed through the
 * following classes:
 *
 * - QueryProcessor
 * - ResultSetWrapper
 * - ResultSetIterator
 * - ResultSetColIterator
 */
public class ResourcesTracker {

	private static Set<Statement> openedStatements = new HashSet<Statement>();
	private static Set<ResultSet> openedResultSets = new HashSet<ResultSet>();

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
		return openedResultSets.size();
	}

	public static synchronized int totalStatements() {
		return openedStatements.size();
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
