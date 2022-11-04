package org.iutools.sql;

import ca.nrc.json.PrettyPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class ColValueIterator<T> implements Iterator<T>, Closeable {

	protected ResultSet rs = null;
	protected String colName = null;

	protected Boolean hasNextItem = null;
	protected T nextItem = null;
	protected boolean nextItemReady = false;
	private PrettyPrinter prettyPrinter = new PrettyPrinter();
	private ResultSetWrapper rsUtils = null;

	public ColValueIterator(ResultSet _rs, String _colName) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.ColValueIterator.constructor");
		rs = _rs;
		ResourcesTracker.updateResourceStatus(rs);
		colName = _colName;
		if (logger.isTraceEnabled()) {
			logger.trace("Constructed iterator for ResultSet with columns: "+
				prettyPrinter.pprint(ResultSetWrapper.colNames(rs)));
			logger.trace("this="+this);
		}
		return;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}

	@Override
	public boolean hasNext() {
		if (!nextItemReady) {
			fetchNextItem();
		}
		boolean answer = nextItemReady;
		return answer;
	}

	private void fetchNextItem() {
		Logger logger = LogManager.getLogger("org.iutools.sql.ColValueIterator.fetchNextItem");
		if (logger.isTraceEnabled()) {
			logger.trace("this="+this);
			logger.trace("colunm names are: "+prettyPrinter.pprint(ResultSetWrapper.colNames(rs)));
		}
		try {
			hasNextItem = false;
			rs.next();
			nextItem = (T) currColValue();
			hasNextItem = true;
			nextItemReady = true;
		} catch (SQLException e) {
			// If an exception is raised, it means that there are no more items
			// Leave hasNextItem at false.
		}
	}

	private Object currColValue() throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.ColValueIterator.currColValue");
		Object colValue = null;
		if (logger.isTraceEnabled()) {
			logger.trace("Fetching colName="+colName+" from ResultSet with columns: "+
				new PrettyPrinter().print(ResultSetWrapper.colNames(rs)));
		}
		try {
			colValue = rs.getObject(colName);
		} catch (Exception e) {
			List<String> colNames = ResultSetWrapper.colNames(rs);
			throw new SQLException(
				"Could not get next value of column "+colName + "\n" +
				"Existing columns in ResultSet were: "+new PrettyPrinter().print(colNames),
				e);
		}
		return colValue;


	}

	@Override
	public T next()  {
		if (!hasNext()) {
			throw new RuntimeException("Tried to get item passed the last item.");
		}
		nextItemReady = false;
		return nextItem;
	}

	@Override
	public void close() throws IOException {
		Logger logger = LogManager.getLogger("org.iutools.sql.ColValueIterator.constructor");
		logger.trace("invoked");
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			ResourcesTracker.updateResourceStatus(rs);
		}
	}
}
