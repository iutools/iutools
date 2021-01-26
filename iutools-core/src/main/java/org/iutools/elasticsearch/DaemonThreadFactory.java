package org.iutools.elasticsearch;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {

	/**
	 * Hands out threads from the wrapped threadfactory with setDeamon(true), so the
	 * threads won't keep the JVM alive when it should otherwise exit.
	 */
	private final ThreadFactory factory;

	/**
	 * Construct a ThreadFactory with setDeamon(true) using
	 * Executors.defaultThreadFactory()
	 */
	public DaemonThreadFactory() {
		this(Executors.defaultThreadFactory());
	}

	/**
	 * Construct a ThreadFactory with setDeamon(true) wrapping the given factory
	 *
	 * @param factory
	 *            factory to wrap
	 */
	public DaemonThreadFactory(ThreadFactory factory) {
		if (factory == null)
			throw new NullPointerException("factory cannot be null");
		this.factory = factory;
	}

	public Thread newThread(Runnable r) {
		final Thread t = factory.newThread(r);
		t.setDaemon(true);
		return t;
	}
}
