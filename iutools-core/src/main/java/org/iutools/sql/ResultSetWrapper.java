package org.iutools.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class for easy manipulation of a ResultSet.
 */
public class ResultSetWrapper implements AutoCloseable {

	protected ResultSet rs = null;

	/** Statement used to generate the ResultSet. It MAY need to be closed when
	 * we close or finalize the wrapper.
	 */
	protected Statement statement = null;

	/** If true, then it means we used the wrapper to create an iterator. in that
	 * case, the iterator will be responsible for closing the Statement and ResultSet.
	 */
	private boolean iteratorCreated = false;

	private List<String> _colNamesSorted = null;

	private ObjectMapper mapper = new ObjectMapper();

	public ResultSetWrapper(ResultSet _rs)  {
		init__ResultSetWrapper(_rs, (Statement)null);
	}

	public ResultSetWrapper(ResultSet _rs, Statement _statement)  {
		init__ResultSetWrapper(_rs, statement);
	}

	public void init__ResultSetWrapper(ResultSet _rs, Statement _statement)  {
		this.rs = _rs;
		this.statement = _statement;
		ResourcesTracker.updateResourceStatus(rs);
		ResourcesTracker.updateResourceStatus(statement);
	}

	public List<String> colNames()  {
		if (_colNamesSorted == null) {
			_colNamesSorted = colNames(rs);
		}
		return _colNamesSorted;
	}

	public static List<String> colNames(ResultSet resSet)  {
		List<String> names = new ArrayList<String>();
		try {
			ResultSetMetaData rsMetaData = resSet.getMetaData();
			int count = rsMetaData.getColumnCount();
			for (int i = 1; i <= count; i++) {
				names.add(rsMetaData.getColumnName(i));
			}
		} catch (Exception e) {
			// For some reason, getColumnCount() sometimes crashes with a null
			// pointer exception. When that happens, assume the column names is
			// empty set
		}
		Collections.sort(names);

		return names;
	}

	@Override
	public void finalize() {
		try {
			close();
		} catch (Exception e) {
			throw new RuntimeException("Unable to close the SQL resources upon finalisation", e);
		}
	}

	@Override
	public void close() throws Exception {
		if (! iteratorCreated) {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// Means the ResultSet was already closed.
				}
			}
			ResourcesTracker.updateResourceStatus(rs);
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// Means the Statement was already closed.
				}
			}
			ResourcesTracker.updateResourceStatus(statement);
		}
	}

	public static JSONObject pullNextRowData(ResultSet resSet) {
		JSONObject rowJson = null;
		if (resSet != null) {
			// Note: If any SQLExceptions are raised in this try, it means there
			//   we have reached the end of the ResultSet. In that situation,
			//   we don't do anything and leave nextPojo=null
			try {
				// Advance the ResultSet cursor
				resSet.next();

				// Get the row data at the current cursor position
				rowJson = ResultSetWrapper.rowAtCursor(resSet);
			} catch (SQLException e) {
				// Means there was no more row to be pulled from the ResultSet
				rowJson = null;
			}
		}
		return rowJson;
	}

	public static JSONObject rowAtCursor(ResultSet resSet) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultSetWrapper.rowAtCursor");
		JSONObject rowJson = new JSONObject();
		if (resSet == null) {
			logger.trace("ResultSet is null!");
		} else {
			List<String> colNames = colNames(resSet);
			try {
				colNames.forEach(cn -> {
					try {
						rowJson.put(cn, resSet.getObject(cn));
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				});
			} catch (RuntimeException e) {
				// If the forEach raised a RuntimeException, it means there were
				// no more rows left in the ResultSet
				return null;
			}
		}

		return rowJson;
	}


	public <T> CloseableIterator<T> iterator(Sql2Pojo<T> converter) throws SQLException {
		CloseableIterator<T> iter = new ResultSetIterator<T>(rs, statement, converter);
		// Remember that we created an iterator.
		// As a result, the wrapper will let the iterator close SQL resources.
		iteratorCreated = true;
		return iter;
	}

	/**
	 * Create an iterator that iterates through the ResultSet rows, and extract
	 * the value of a single column.
	 *  @param colName : Name of the column
	 * @param colClass : Class of the the value stored in the column.
	 * @return
	 */
	public <C> ResultSetColIterator<C> colIterator(String colName, Class<C> colClass)
		throws SQLException {

		ResultSetColIterator<C> iter =
			new ResultSetColIterator<C>(rs, statement, colName, colClass);
		return iter;
	}

	public <T> List<T> toPojoLst(Sql2Pojo<T> converter) throws Exception {
		return toPojoLst(converter, (Integer)null);
	}

	public <T> List<T> toPojoLst(Sql2Pojo<T> converter, Integer maxRows)
		throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultSetWrapper.toPojoLst");
		List<T> pojos = new ArrayList<T>();
		// First, convert the ResultsSet into a list of JSONObjects
		List<JSONObject> jsonObjects = toJSONObjects(maxRows);
		logger.trace("Size of jsonObject="+jsonObjects.size());

		// Next, convert the JSONObjects into POJOs
		for (JSONObject aJsonObj : jsonObjects) {
			T aPojo = converter.toPOJO(aJsonObj);
			pojos.add(aPojo);
		}

		return pojos;
	}

	private List<JSONObject> toJSONObjects(Integer maxRows) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultSetWrapper.toJSONObjects");
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		if (rs == null) {
			logger.trace("resultSet is null!");
		} else {
			// First, get the ResultSet's column names
			//
			ResultSetMetaData md = rs.getMetaData();
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
			while (rs.next() && (maxRows == null || rowCount < maxRows)) {
				rowCount++;
				logger.trace("looking at next result");
				JSONObject row = new JSONObject();
				colNames.forEach(cn -> {
					try {
						row.put(cn, rs.getObject(cn));
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				});
				jsonObjects.add(row);
			}
		}
		return jsonObjects;
	}

	/** Convert a ResultSet to a SINGLE Plain Old Java Object (POJO).
	 * Raises an exception if the size of the ResultSet != 1.
	 */
	public <T> T toPojo(Sql2Pojo<T> converter) throws SQLException {
		T pojo = null;
		List<T> pojoList = toPojoLst(converter, new Integer(1));
		int size = pojoList.size();
		if (size > 1) {
			throw new SQLException(
				"ResultSet contained more than a single row; #rows="+size);
		} else if (size == 1) {
			pojo = pojoList.get(0);
		}
		return pojo;
	}

	public boolean isEmpty() throws SQLException {
		boolean isEmpty = true;
		try {
			if (rs.next()) {
				isEmpty = false;
				rs.beforeFirst();
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		return isEmpty;
	}
}
