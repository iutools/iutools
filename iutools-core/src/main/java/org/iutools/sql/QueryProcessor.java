package org.iutools.sql;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Class for processing an SQL query */
public class QueryProcessor {

	private static Map<String,Boolean> tableIsDefinedCache = new HashMap<String,Boolean>();

	private ObjectMapper mapper = new ObjectMapper();
	private PrettyPrinter prettyPrinter = new PrettyPrinter();

	protected Connection getConnection() throws SQLException {
		Connection conn = null;
		try {
			conn = new ConnectionPool().getConnection();
		} catch (SQLException e) {
			throw new SQLException("Could not get a connection from the pool", e);
		}
		return conn;
	}

	private synchronized Boolean uncacheTableIsDefined(String tableName) throws SQLException {
		Boolean isDefined = null;
		if (tableIsDefinedCache.containsKey(tableName)) {
			isDefined = tableIsDefinedCache.get(tableName);
		}
		return isDefined;
	}

	private synchronized void cacheTableIsDefined(String tableName, Boolean isDefined) {
		tableIsDefinedCache.put(tableName, isDefined);
	}

	protected boolean tableIsDefined(TableSchema schema) throws SQLException {
		return tableIsDefined(schema.tableName);
	}

	public synchronized boolean tableIsDefined(String tableName) throws SQLException {
		Boolean isDefined = uncacheTableIsDefined(tableName);
		if (isDefined == null) {
			String query = "SHOW TABLES LIKE \""+tableName+"\";";
			// We use try-with to ensure that the ResultSet will be closed even
			// if an exception is raised.
			try (ResultSetWrapper rsw = query3(query)) {
				isDefined = !rsw.isEmpty();
				cacheTableIsDefined(tableName, isDefined);
			} catch (Exception e) {
				throw new SQLException(e);
			}
		}
		return isDefined;
	}


	/**
	 * Repeatadly runs a query on a series of arguments.
	 */
	public void queryBatch(String query, List<Object[]> argsBatch) throws SQLException {
		try (Connection conn = getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(query);
			int rowIndex = -1;
			Object[] firstRow = null;
			for (Object[] queryArgs : argsBatch) {
				rowIndex++;
				if (rowIndex == 0) {
					firstRow = queryArgs;
				}
				if (queryArgs.length != firstRow.length) {
					throw new SQLException(
						"Row #"+rowIndex+" did not have same lenght as first row.\n"+
						"Row Lenghts were:\n"+
						"  First : "+firstRow.length+"\n"+
						"  #"+rowIndex+" : "+queryArgs.length+"\n"+
						"Row values were:\n"+
						"  First : "+Arrays.toString(firstRow)+"\n"+
						"  #"+rowIndex+" : "+Arrays.toString(queryArgs)
						);
				}
				setPrepStatementArgs(stmt, queryArgs);
			}
			stmt.executeBatch();
			conn.commit();
		}
		return;
	}

	private void setPrepStatementArgs(PreparedStatement stmt, Object[] queryArgs) throws SQLException {
		for (int ii = 0; ii < queryArgs.length; ii++) {
			int argPos = ii + 1;
			Object arg = queryArgs[ii];
			if (arg != null && arg.getClass().getName().endsWith("JSONObject$Null")) {
				arg = null;
			}
			stmt.setObject(argPos, arg);
		}
		stmt.addBatch();
	}

	public ResultSetWrapper query3(String query, Object... queryArgs) throws SQLException {
		return query3((Connection) null, false, query, queryArgs);
	}

	public ResultSetWrapper query3(Connection conn, String query, Object... queryArgs) throws SQLException {
		return query3(conn, (Boolean)null, query, queryArgs);
	}

	public ResultSetWrapper query3(Connection conn, Boolean scrollable, String query, Object... queryArgs) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.QueryProcessor.query");
		if (logger.isTraceEnabled()) {
			logger.trace("query=\n"+query);
			String argsMess = "with args:";
			for (Object arg: queryArgs) {
				String argStr = "null";
				if (arg != null) {
					argStr = arg.toString();
				}
				argsMess += "\n  "+arg.toString();
			}
			logger.trace(argsMess);
		}
		if (scrollable == null) {
			scrollable = false;
		}
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try  {
			if (conn == null) {
				conn = getConnection();
			}
			logger.trace("conn to db: "+conn.getCatalog());
			stmt = conn.prepareStatement(query);
			setPrepStatementArgs(stmt, queryArgs);
			rs = stmt.executeQuery();
			if (logger.isTraceEnabled()) {
				logger.trace("Returning ResultSet with columns: "+
					prettyPrinter.pprint(ResultSetWrapper.colNames(rs)));
			}
		} catch (SQLException e) {
			String argsJson = null;
			argsJson = prettyPrinter.pprint(queryArgs);
			throw new SQLException(
				"Could not execute query:\n"+
				"query was:\n"+
				query+"\n"+
				"Arguments were:\n"+
				argsJson,
				e);
		}
		ResultSetWrapper wrapper = new ResultSetWrapper(rs, stmt);
		return wrapper;
	}

	public long count(String queryStr, Object... queryArgs) throws SQLException {
		long rowCount = 0;
		queryStr = "SELECT COUNT(*) AS rowCount "+queryStr+";";
		// We use try-with to ensure that the ResultSet will be closed even
		// if an exception is raised.
		try {
			try (ResultSetWrapper rsw = query3(queryStr, queryArgs)) {
				rsw.rs.next();
				rowCount = rsw.rs.getLong("rowCount");
			}
		} catch (Exception e) {
			throw new SQLException(e);
		}
		return rowCount;
	}

	public Double aggregateNumerical(String aggrFctName, String fldName,
		String queryStr, Object[] queryArgs) throws SQLException {

		Double aggrValue = null;

		queryStr =
			"SELECT "+aggrFctName+"("+fldName+") AS aggrValue "+queryStr;

		// We use try-with to ensure that the ResultSet will be closed even
		// if an exception is raised.
		try (ResultSetWrapper rsw = query3(queryStr, queryArgs)) {
			rsw.rs.next();
			aggrValue = rsw.rs.getDouble("aggrValue");
		} catch (Exception e) {
			// If an exception is raised, it probably means that there were no
			// hits at all. So leave aggrValue to 0.0.
			int x = 1;
		}
		return aggrValue;
	}


	public void dropTable(String tableName) throws SQLException {
		String query = "DROP TABLE IF EXISTS "+tableName;
		// We use try-with to ensure that the ResultSet will be closed even if an
		// exception is raised
		try (ResultSetWrapper rsw = query3(query)) {
		} catch (Exception e) {
			throw new SQLException(e);
		}
		return;
	}

	public void getRowWithID(String id, String tableName) {
	}

	public void insertObject(SQLPersistent object) throws SQLException {
		insertObject(object, (Boolean)null);
	}

	public void insertObject(SQLPersistent object, Boolean replace) throws SQLException {
		if (replace == null) {
			replace = true;
		}
		Row row = object.toRow();
		insertRow(row, replace);
		return;
	}

	private void insertRow(Row row, Boolean replace) throws SQLException {
		List<Row> justOneRow = new ArrayList<Row>();
		justOneRow.add(row);
		insertRows(justOneRow, replace);
	}

	public void replaceRow(Row row) throws SQLException {
		List<Row> justOneRow = new ArrayList<Row>();
		justOneRow.add(row);
		replaceRows(justOneRow);
		return;
	}

	public void replaceRows(List<Row> rows) throws SQLException {
		insertRows(rows, true);
		return;
	}

	public void insertRows(List<Row> rows, Boolean replace) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.QueryProcessor.insertRows");
		if (replace == null) {
			replace = true;
		}
		if (rows != null && !rows.isEmpty()) {
			List<Object[]> rowValues = new ArrayList<Object[]>();
			Row firstRow = rows.get(0);
			String tableName = firstRow.tableName;
			List<String> firstRowColNames = firstRow.colNames();
			String query =
				"INSERT INTO " + tableName + "\n" +
				"  ("+firstRow.sqlColNames()+")\n" +
				"VALUES\n"+
				"  (";
			for (int ii=0; ii < firstRowColNames.size(); ii++) {
				if (ii > 0) {
					query += ", ";
				}
				query += "?";
			}
			query += ")";
			if (replace) {
				query += "\n" + firstRow.sqlOnDuplicateUpdate();
			}
			query += ";";

			int rowCount = 0;
			for (Row row: rows) {
				rowCount++;
				row.ensureHasSameColumnNamesAs(firstRow);
				Object[] valuesThisRow = row.colValues();
				if (valuesThisRow.length != firstRowColNames.size()) {
					throw new SQLException("Row number "+rowCount+" did not have the ");
				}
				rowValues.add(valuesThisRow);
				if (!row.tableName.equals(tableName)) {
					throw new SQLException("Tried to replace rows in multiple tables at once: "+tableName+", "+row.tableName);
				}
				logger.trace("after row #"+rowCount+", query size="+query.length());
			}
			queryBatch(query, rowValues);
		}
		logger.trace("Exiting");
		return;
	}

	public void ensureTableIsDefined(TableSchema schema) throws SQLException{
		try {
			if (!tableIsDefined(schema.tableName)) {
				defineTable(schema);
			}
		} catch (SQLException e) {
			throw new SQLException(
				"Exception trying to ensure that SQL table "+schema.tableName+" is defined", e);
		}
	}

	private void defineTable(TableSchema schema) throws SQLException{
		try {
			String[] schemaStatements = schema.schemaStatements();
			execStatements(schemaStatements);
			cacheTableIsDefined(schema.tableName, true);
		} catch (RuntimeException e) {
			throw new SQLException(
				"Problem defining table "+schema.tableName,
				e);
		}
		return;
	}

	public void execStatements(String[] statements) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.QueryProcessor.runStatements");
		try (Connection conn = getConnection()) {
			for (String statement: statements) {
				logger.trace("Running statement:\n"+statement);
				// We use try-with to ensure that the ResultSet will be closed even
				// if an exception is raised.
				try (ResultSetWrapper rsw = query3(conn, false, statement)) {
				} catch (Exception e) {
					throw new SQLException(e);
				}
			}
		}
	}

}
