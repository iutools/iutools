package org.iutools.sql;

import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class ResultsSetIterator<T> implements Iterator<T>, Closeable {
	private ResultSet rs = null;
	private Row2Pojo<T> converter = null;

	protected Boolean hasNextItem = null;
	protected T nextItem = null;
	protected boolean nextItemReady = false;

	private ResultSetWrapper rsw = null;

	public ResultsSetIterator(ResultSet rs, Row2Pojo<T> converter) {
		this.rs = rs;
		this.converter = converter;
		ResourcesTracker.updateResourceStatus(rs);
		this.rsw = new ResultSetWrapper(rs);
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
		try {
			hasNextItem = false;
			rs.next();
			nextItem = rsw.toPojo(converter);
			hasNextItem = true;
			nextItemReady = true;
		} catch (SQLException e) {
			// If an exception is raised, it means that there are no more items
			// Leave hasNextItem at false.
		}
	}


	@Override
	public T next() {
		if (!hasNext()) {
			throw new RuntimeException("Tried to get item passed the last item.");
		}
		T nextValue = nextItem;
		nextItemReady = false;
		return nextValue;
	}

	@Override
	public void close() throws IOException {
		try {
			rs.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}
		ResourcesTracker.updateResourceStatus(rs);
	}
}
