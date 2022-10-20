package org.iutools.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Class for easy manipulation of a ResultSet.
 */
public class ResultSetWrapper implements AutoCloseable {

	private ResultSet rs = null;

	/** Statement used to generate the ResultSet. It MAY need to be closed when
	 * we close or finalize the wrapper.
	 */
	private Statement statement = null;

	/** If true, then it means we used the wrapper to create an iterator. in that
	 * case, the iterator will be responsible for closing the Statement and ResultSet.
	 */
	private boolean iteratorCreated = false;

	private List<String> _colNamesSorted = null;

	public ResultSetWrapper(ResultSet _rs, Statement _statement)  {
		this.rs = _rs;
		this.statement = _statement;
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
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// Means the Statement was already closed.
				}
			}
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

}
