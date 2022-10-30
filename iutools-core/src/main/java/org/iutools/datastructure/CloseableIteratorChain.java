package org.iutools.datastructure;

import ca.nrc.datastructure.CloseableIterator;
import org.apache.commons.collections4.iterators.IteratorChain;

import java.util.Iterator;

public class CloseableIteratorChain<T> extends IteratorChain<T>
	implements CloseableIterator<T> {

	CloseableIterator[] iters = null;

	public CloseableIteratorChain(CloseableIterator<T>... _iterators) {
		super(_iterators);
		iters = _iterators;
	}

	@Override
	public void close() throws Exception {
		for (Iterator iter: iters) {
			((CloseableIterator)iter).close();
		}
	}
}
