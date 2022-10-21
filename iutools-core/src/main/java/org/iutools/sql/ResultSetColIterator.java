package org.iutools.sql;

import org.apache.commons.dbcp2.DelegatingResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

public class ResultSetColIterator<C> implements CloseableIterator<C> {

	DelegatingResultSet DELETE_ME = null;

	private ResultSet rs = null;
	private Statement statement = null;
	private String colName = null;
	private JSONObject nextRowData = null;

	public <C> ResultSetColIterator(ResultSet _rs, Statement _statement,
		String _colName, Class<C> _clazz) {
		super();
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultSetColIterator.constructor");
		this.rs = _rs;
		this.statement = _statement;
		this.colName = _colName;
		pullNextRowData();
		traceState("<- Upon exit", logger);
		return;
	}

	private void pullNextRowData() {
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultSetColIterator.pullNextRowData");
		traceState("-> Upon entry", logger);
		nextRowData = null;
		if (rs != null) {
			nextRowData = ResultSetWrapper.pullNextRowData(rs);
		}
		traceState("<- Upon exit", logger);
		return;
	}

	@Override
	public void close() throws Exception {
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultSetColIterator.close");
		traceState("-> Upon entry", logger);
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
		ResourcesTracker.updateResourceStatus(rs);
		ResourcesTracker.updateResourceStatus(statement);
		traceState("<- Upon exit", logger);
	}

	@Override
	public boolean hasNext() {
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultSetColIterator.hasNext");
		traceState("-> Upon entry", logger);
		boolean answer = (nextRowData != null && !nextRowData.keySet().isEmpty());
		traceState("<- Upon exit", logger);
		return answer;
	}

	@Override
	public C next() {
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultSetColIterator.next");
		traceState("-> Upon entry", logger);
		C nextColValue =  (C) nextRowData.get(colName);
		nextRowData = ResultSetWrapper.pullNextRowData(rs);
		traceState("<- Upon exit", logger);
		return nextColValue;
	}

	@Override
	public void forEachRemaining(Consumer<? super C> action) {
		Logger logger = LogManager.getLogger("org.iutools.sql.ResultSetColIterator.forEachRemaining");
		traceState("-> Upon entry", logger);
		while (hasNext()) {
			C nextColValue = next();
			action.accept(nextColValue);
		}
		traceState("<- Upon exit", logger);
		return;
	}


	protected void traceState(String mess, Logger logger)  {
		if (logger.isTraceEnabled()) {
			if (mess != null) {
				mess += "\n";
			} else {
				mess = "";
			}
			try {
				mess +=
					"  Iterator status:\n" +
					"    rs.isClosed()=" + rs.isClosed() + "\n" +
					"    statement.isClosed()=" + statement.isClosed() + "\n" +
					"    nextRowData=" + (nextRowData == null ? "null" : nextRowData.toString());
			} catch (SQLException e) {
				mess += "\nException raised while tracing the iterator: "+e;
			}
			logger.trace(mess);
		}
	}
}
