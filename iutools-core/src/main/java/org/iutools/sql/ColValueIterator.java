package org.iutools.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class ColValueIterator<T> implements Iterator<T> {

	protected ResultSet rs = null;
	protected String colName = null;

	protected Boolean hasNextItem = null;
	protected T nextItem = null;
	protected boolean nextItemReady = false;

	public ColValueIterator(ResultSet _rs, String _colName) {
		rs = _rs;
		colName = _colName;
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
			nextItem = (T) QueryProcessor.rs2CurrColValue(rs, colName);
			hasNextItem = true;
			nextItemReady = true;
		} catch (SQLException e) {
			// If an exception is raised, it means that there are no more items
			// Leave hasNextItem at false.
		}
	}

	@Override
	public T next()  {
		if (!hasNext()) {
			throw new RuntimeException("Tried to get item passed the last item.");
		}
		return nextItem;
	}
}
