package org.iutools.datastructure;

import org.iutools.sql.CloseableIterator;

import java.util.Iterator;

/**
 * Wrap a regular iterator into a ClosableIterator.
 * @param <T>
 */
public class CloseableIteratorWrapper<T> implements CloseableIterator<T> {

	protected Iterator<T> iter = null;

	public CloseableIteratorWrapper(Iterator<T> _iter) {
		super();
		this.iter = _iter;
	}

	@Override
	public void close() throws Exception {
		// Assume the wrapped iterator does not need closing
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public T next() {
		return iter.next();
	}
}
