package org.iutools.sql;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
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

	public static boolean rsIsEmpty(ResultSet results) throws SQLException {
		boolean isEmpty = true;
		try {
			if (results.next()) {
				isEmpty = false;
				results.beforeFirst();
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		return isEmpty;
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
			try (ResultSet rs = query2(query)) {
				isDefined = !(QueryProcessor.rsIsEmpty(rs));
				cacheTableIsDefined(tableName, isDefined);
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

	public ResultSet query2(String query, Object... queryArgs) throws SQLException {
		return query2((Connection) null, false, query, queryArgs);
	}

	public ResultSet query2(Connection conn, String query, Object... queryArgs) throws SQLException {
		return query2(conn, (Boolean)null, query, queryArgs);
	}


	public ResultSet query2(Connection conn, Boolean scrollable, String query, Object... queryArgs) throws SQLException {
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
					prettyPrinter.pprint(new ResultSetUtils(rs).columnNames()));
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
		return rs;
	}


	public long count(String queryStr, Object... queryArgs) throws SQLException {
		long rowCount = 0;
		queryStr = "SELECT COUNT(*) AS rowCount "+queryStr+";";
		// We use try-with to ensure that the ResultSet will be closed even
		// if an exception is raised.
		try (ResultSet rs = query2(queryStr, queryArgs)) {
			rs.next();
			rowCount = rs.getLong("rowCount");
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
		try (ResultSet rs = query2(queryStr, queryArgs)) {
			rs.next();
			aggrValue = rs.getDouble("aggrValue");
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
		try (ResultSet rs = query2(query)) {

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
		Row row = new Row(object);
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

	/**
	 * Given a ResultSet and a column name, return the value of that column
	 * in the current row of the ResultSet
	 */
	public static Object rs2CurrColValue(ResultSet rs, String colName) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.QueryProcessor.rs2CurrColValue");
		Object colValue = null;
		if (logger.isTraceEnabled()) {
			logger.trace("Fetching colName="+colName+" from ResultSet with columns: "+
				new PrettyPrinter().print(new ResultSetUtils(rs).columnNames()));
		}
		try {
			colValue = rs.getObject(colName);
		} catch (Exception e) {
			Set<String> colNames = new ResultSetUtils(rs).columnNames();
			throw new SQLException(
				"Could not get next value of column "+colName + "\n" +
				"Existing columns in ResultSet were: "+new PrettyPrinter().print(colNames),
				e);
		}
		return colValue;
	}

	/** Convert a ResultSet to a SINGLE Plain Old Java Object (POJO).
	 * Raises an exception if the size of the ResultSet != 1.
	 */
	public static <T> T rs2pojo(ResultSet resultSet, Class<T> clazz) throws SQLException {
		T pojo = null;
		List<T> pojoList = rs2pojoLst(resultSet, clazz, 1);
		int size = pojoList.size();
		if (size > 1) {
			throw new SQLException(
				"ResultSet contained more than a single row; #rows="+size);
		} else if (size == 1) {
			pojo = pojoList.get(0);
		}
		return pojo;
	}

	/** Convert a ResultSet to a SINGLE Plain Old Java Object (POJO).
	 * Raises an exception if the size of the ResultSet != 1.
	 */
	public static <T> T rs2pojo(ResultSet resultSet, Sql2Pojo<T> converter) throws SQLException {
		T pojo = null;
		List<T> pojoList = rs2pojoLst(resultSet, converter, new Integer(1));
		int size = pojoList.size();
		if (size > 1) {
			throw new SQLException(
				"ResultSet contained more than a single row; #rows="+size);
		} else if (size == 1) {
			pojo = pojoList.get(0);
		}
		return pojo;
	}

	public static <T> List<T> rs2pojoLst(ResultSet resultSet, Class<T> clazz)
		throws SQLException {
		return rs2pojoLst(resultSet, clazz, (Integer)null);
	}

	/**
	 * Converts an SQL ResultSet to a list of Plain Old Java Objects (POJOs).
	 */
	public static <T> List<T> rs2pojoLst(ResultSet resultSet, Class<T> clazz,
		Integer maxRows) throws SQLException {
		List<T> pojos = new ArrayList<T>();
		try {
			// First, convert the ResultsSet into a list of JSONObjects
			List<JSONObject> jsonObjects = rs2JSONObjects(resultSet, maxRows);

			// Next, convert the JSONObjects into POJOs
			ObjectMapper mapper = new ObjectMapper();
			for (JSONObject aJsonObj : jsonObjects) {
				String jsonStr = aJsonObj.toString();
				T aPojo = mapper.readValue(jsonStr, clazz);
				pojos.add(aPojo);
			}
		} catch (RuntimeException | JsonProcessingException e) {
			throw new SQLException(e);
		}

		return pojos;
	}

	public static <T> T rs2Iterator(ResultSet resultSet, Sql2Pojo<T> converter,
		Integer maxRows) throws SQLException {
		T pojo = null;
		List<T> pojoList = rs2pojoLst(resultSet, converter, maxRows);
		int size = pojoList.size();
		if (size > 1) {
			throw new SQLException(
				"ResultSet contained more than a single row; #rows="+size);
		} else if (size == 1) {
			pojo = pojoList.get(0);
		}
		return pojo;
	}

	public static <T> List<T> rs2pojoLst(ResultSet resultSet, Sql2Pojo<T> converter)
		throws SQLException {
		return rs2pojoLst(resultSet, converter, (Integer)null);
	}

	/**
	 * Converts an SQL ResultSet to a list of Plain Old Java Objects (POJOs).
	 */
	public static <T> List<T> rs2pojoLst(ResultSet resultSet, Sql2Pojo<T> converter,
		Integer maxRows) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.QueryProcessor.rs2pojoLst");
		List<T> pojos = new ArrayList<T>();
		try {
			// First, convert the ResultsSet into a list of JSONObjects
			List<JSONObject> jsonObjects = rs2JSONObjects(resultSet, maxRows);
			logger.trace("Size of jsonObject="+jsonObjects.size());

			// Next, convert the JSONObjects into POJOs
			for (JSONObject aJsonObj : jsonObjects) {
				T aPojo = converter.toPOJO(aJsonObj);
				pojos.add(aPojo);
			}
		} catch (RuntimeException e) {
			throw new SQLException(e);
		}

		return pojos;
	}

	public static List<JSONObject> rs2JSONObjects(ResultSet resultSet) throws SQLException {
		return rs2JSONObjects(resultSet, (Integer)null);
	}

	public static List<JSONObject> rs2JSONObjects(ResultSet resultSet, Integer maxRows) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.QueryProcessor.rs2JSONObjects");
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		ObjectMapper mapper = new ObjectMapper();
		if (resultSet == null) {
			logger.trace("resultSet is null!");
		} else {
			// First, get the ResultSet's column names
			//
			ResultSetMetaData md = resultSet.getMetaData();
			int numCols = md.getColumnCount();
			logger.trace("numCols="+numCols);
			List<String> colNames = IntStream.range(0, numCols)
			.mapToObj(i -> {
				try {
					return md.getColumnName(i + 1);
				} catch (SQLException e) {
					e.printStackTrace();
					return "?";
				}
			})
			.collect(Collectors.toList());

			// Next, generate a list of JSONObjects, each object corresponding to
			// one row of the ResultsSet.
			//
			int rowCount = 0;
			while (resultSet.next() && (maxRows == null || rowCount < maxRows)) {
				rowCount++;
				logger.trace("looking at next result");
				JSONObject row = new JSONObject();
				colNames.forEach(cn -> {
					try {
						row.put(cn, resultSet.getObject(cn));
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				});
				jsonObjects.add(row);
			}
		}
		return jsonObjects;
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
				try (ResultSet rs = query2(conn, false, statement)) {
				}
			}
		}
	}

}
