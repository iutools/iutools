package org.iutools.sql;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

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
			try (ResultSetWrapper rsw = query(query)) {
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
		Connection conn = getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
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

	public ResultSetWrapper query(String query, Object... queryArgs) throws SQLException {
		return query((Connection) null, false, query, queryArgs);
	}

	public ResultSetWrapper query(Connection conn, String query, Object... queryArgs) throws SQLException {
		return query(conn, (Boolean)null, query, queryArgs);
	}

	public ResultSetWrapper query(Connection conn, Boolean scrollable, String query, Object... queryArgs) throws SQLException {
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
			try (ResultSetWrapper rsw = query(queryStr, queryArgs)) {
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
		try (ResultSetWrapper rsw = query(queryStr, queryArgs)) {
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
		try (ResultSetWrapper rsw = query(query)) {
		} catch (Exception e) {
			throw new SQLException(e);
		}
		return;
	}

	public void getRowWithID(String id, String tableName) {
	}

	public <T> void insertObject(T object, Row2Pojo<T> converter) throws SQLException {
		insertObject(object, converter, (Boolean)null);
	}

	public <T> void insertObject(T object, Row2Pojo<T> converter, Boolean replace) throws SQLException {
		if (replace == null) {
			replace = true;
		}
		JSONObject rowJson = converter.toRowJson(object);
		insertRow(rowJson, converter, replace);
		return;
	}

	public <T> void insertObjects(List<T> objects, Row2Pojo<T> converter,
		Boolean replace) throws SQLException {
		if (replace == null) {
			replace = true;
		}
		List<JSONObject> rows = new ArrayList<JSONObject>();
		for (T anObject: objects) {
			JSONObject rowJson = converter.toRowJson(anObject);
			rows.add(rowJson);
		}
		insertRows(rows, converter, replace);
	}


	private void insertRow(JSONObject row, Row2Pojo converter, Boolean replace) throws SQLException {
		List<JSONObject> justOneRow = new ArrayList<JSONObject>();
		justOneRow.add(row);
		insertRows(justOneRow, converter, replace);
	}

	public void replaceRow(JSONObject row, Row2Pojo converter) throws SQLException {
		List<JSONObject> justOneRow = new ArrayList<JSONObject>();
		justOneRow.add(row);
		replaceRows(justOneRow, converter);
		return;
	}

	public void replaceRows(List<JSONObject> rows, Row2Pojo converter) throws SQLException {
		insertRows(rows, converter, true);
		return;
	}

	public void insertRows(List<JSONObject> rows, Row2Pojo converter, Boolean replace) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.QueryProcessor.insertRows");
		if (replace == null) {
			replace = true;
		}
		if (rows != null && !rows.isEmpty()) {
			List<Object[]> rowValues = new ArrayList<Object[]>();
			JSONObject firstRow = rows.get(0);
			List<String> colNames = converter.schemaColNames();
			String sqlColNames = String.join(", ", colNames);
			String query =
				"INSERT INTO " + converter.tableName() + "\n" +
				"  ("+sqlColNames+")\n" +
				"VALUES\n"+
				"  (";
			for (int ii=0; ii < colNames.size(); ii++) {
				if (ii > 0) {
					query += ", ";
				}
				query += "?";
			}
			query += ")";
			if (replace) {
				query += "\n" + sqlOnDuplicateUpdate(converter.schema);
			}
			query += ";";

			int rowCount = 0;
			for (JSONObject row: rows) {
				rowCount++;
				converter.ensureRowIsCompatibleWithSchema(firstRow);
				Object[] valuesThisRow = converter.colValues(row);
				rowValues.add(valuesThisRow);
				logger.trace("after row #"+rowCount+", query size="+query.length());
			}
			queryBatch(query, rowValues);
		}
		logger.trace("Exiting");
		return;
	}

	public String sqlOnDuplicateUpdate(TableSchema schema) {
		String sql = "";
		List<String> colNames = schema.columnNames();
		int colCounter = 0;
		for (String colName: colNames) {
			colCounter++;
			if (colCounter == 1) {
				sql = "ON DUPLICATE KEY UPDATE\n";
			}
			sql += "  " + colName + " = VALUES("+colName+")";
			if (colCounter != colNames.size()) {
				sql += ",";
			}
			sql += "\n";
		}
		return sql;
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
				try (ResultSetWrapper rsw = query(conn, false, statement)) {
				} catch (Exception e) {
					throw new SQLException(e);
				}
			}
		}
	}
}
