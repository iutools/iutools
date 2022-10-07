package org.iutools.sql;

import org.iutools.concordancer.tm.sql.SentenceInLang;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class ResultsSetIterator<T> implements Iterator<T>, Closeable {
	private Connection conn = null;
	private ResultSet rs = null;
	private Sql2Pojo<T> converter = null;

	protected Boolean hasNextItem = null;
	protected T nextItem = null;
	protected boolean nextItemReady = false;

	public ResultsSetIterator(ResultSet rs, Connection conn, Sql2Pojo<T> converter) {
		this.rs = rs;
		this.conn = conn;
		this.converter = converter;
		return;
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
			nextItem = QueryProcessor.rs2pojo(rs, converter);
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
	}
}
