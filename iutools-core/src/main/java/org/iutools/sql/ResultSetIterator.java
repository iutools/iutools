package org.iutools.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ResultSetIterator<T> implements CloseableIterator<T> {

	private ResultSet rs = null;

	/**
	 * Statement that was used to generate the ResultSet.
	 * It should be closed when we close/finalize the iterator.
	 */
	private Statement statement = null;

	private Sql2Pojo<T> converter = null;

	private T nextPojo = null;

	private ObjectMapper mapper = new ObjectMapper();

	private List<String> _colNames = null;

	public ResultSetIterator(ResultSet _rs, Statement _statement,
		Sql2Pojo<T> _converter) throws SQLException {
		this.rs = _rs;
		this.statement = _statement;
		this.converter = _converter;
		pullNextPojo();
		return;
	}

	private void pullNextPojo() throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultsIterator.pullNextPojo");
		nextPojo = null;
		if (rs != null) {
			JSONObject nextRowJson = ResultSetWrapper.pullNextRowData(rs);
			if (nextRowJson != null && !nextRowJson.keySet().isEmpty()) {
				nextPojo = converter.toPOJO(nextRowJson);
			}
		}
		return;
	}

	private List<String> colNames() {
		if (_colNames == null) {
			_colNames = ResultSetWrapper.colNames(rs);
		}
		return _colNames;
	}

	@Override
	public void close() throws Exception {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
				// Means the ResultSet was already closed.
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (Exception e) {
				// Means the Statement was already closed.
			}
		}
	}

	@Override
	public boolean hasNext() {
		return nextPojo != null;
	}

	@Override
	public T next() {
		T nextCopy = nextPojo;
		try {
			pullNextPojo();
		} catch (SQLException e) {
			throw new RuntimeException("Unable to pull the next POJO from ResultSet", e);
		}

		return nextCopy;
	}
}
