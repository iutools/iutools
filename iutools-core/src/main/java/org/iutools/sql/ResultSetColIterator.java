package org.iutools.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.Statement;

public class ResultSetColIterator<C> extends CloseableIterator<C> {

	private ResultSet rs = null;
	private Statement statement = null;
	private String colName = null;
	private JSONObject nextRowData = null;

	public <C> ResultSetColIterator(ResultSet _rs, Statement _statement,
		String _colName, Class<C> _clazz) {
		super();
		this.rs = _rs;
		this.statement = _statement;
		this.colName = _colName;
		pullNextRowData();
	}

	private void pullNextRowData() {
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultSetColIterator.pullNextRowData");
			nextRowData = null;
		if (rs != null) {
			nextRowData = ResultSetWrapper.pullNextRowData(rs);
		}
		return;
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
		boolean answer = (nextRowData != null);
		return answer;
	}

	@Override
	public C next() {
		C nextColValue =  (C) nextRowData.get(colName);
		nextRowData = ResultSetWrapper.pullNextRowData(rs);
		return nextColValue;
	}
}
