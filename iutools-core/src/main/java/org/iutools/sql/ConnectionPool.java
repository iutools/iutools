package org.iutools.sql;

import ca.nrc.config.ConfigException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.config.IUConfig;

import java.sql.Connection;
import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Pool of SQL connections for IUTools
 */
public class ConnectionPool {
	private final String DBMS_NAME = "drizzle";
	private final String SERVER_NAME = "localhost";

	public static Boolean _isTesting = null;
	
//	Follow recipe here...
//	https://www.baeldung.com/java-connection-pooling
	private static Map<String, BasicDataSource> dataSources =
		new HashMap<String, BasicDataSource>();

	private static Map<Long,Connection> thread2connIndex = new HashMap<Long,Connection>();

	protected void finalize() throws Throwable {
		// Take this opportunity to cleanup connections that are associated to
		// dead threads.
		cleanupThreadConnIndex();
	}

	private synchronized BasicDataSource dataSource4DB(String dbName) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.ConnectionPool.dataSource4DB");
		if (!dataSources.containsKey(dbName)) {
			BasicDataSource ds = new BasicDataSource();
			IUConfig config = null;
			String userName = null;
			String password = null;
			String portNum = null;
			try {
				config = new IUConfig();
				userName = config.sqlUserName();
				password = config.sqlPasswd();
				portNum = config.sqlPortNumber();
				ds.setDriverClassName("org.drizzle.jdbc.DrizzleDriver");

				ds.setUrl(
					"jdbc:" + this.DBMS_NAME + "://" +
					this.SERVER_NAME +
					":" + portNum +
					"/" + dbName + "?" +
					"rewriteBatchedStatements=true");
				ds.setUsername(userName);
				ds.setPassword(password);
				// Maximum number of active connections in the pool
				ds.setMaxTotal(100);
//				ds.setMaxTotal(10);
				ds.setMinIdle(5);
				ds.setMaxIdle(10);
				ds.setMaxOpenPreparedStatements(100);
				dataSources.put(dbName, ds);
			} catch (ConfigException e) {
				throw new SQLException(e);
			}
		}
		BasicDataSource ds = dataSources.get(dbName);
		if (logger.isTraceEnabled()) {
			logger.trace(""+ds);
		}
		return ds;
	}

	@JsonIgnore
	public synchronized Connection getConnection() throws SQLException {
		Connection connection = null;
		// we cleanup the connections index every time we ask for a new connection
		cleanupThreadConnIndex();
		Long currThread = Thread.currentThread().getId();
		if (!hasLiveConnection4Thread(currThread)) {
			// We don't have a live connection for the current thread.
			// Initialize one and put it in the connections index.
			try {
				String dbName = new IUConfig().sqlDbName();
				if (isTesting()) {
					dbName += "_test";
				}
				BasicDataSource ds = dataSource4DB(dbName);
				thread2connIndex.put(currThread, ds.getConnection());
			} catch(ConfigException e){
				throw new SQLException(e);
			}
		}

		return thread2connIndex.get(currThread);
	}

	private boolean hasLiveConnection4Thread(Long thrID) throws SQLException {
		boolean answer = false;
		if (thread2connIndex.containsKey(thrID)) {
			if (!thread2connIndex.get(thrID).isClosed()) {
				answer = true;
			}
		}
		return answer;
	}

	private void cleanupThreadConnIndex() {
		Set<Long> activeThreads = activeThreadIDs();
		for (Long thrId: thread2connIndex.keySet()) {
			if (!activeThreads.contains(thrId)) {
				// We have a connection for a thread that has terminated
				try {
					thread2connIndex.get(thrId).close();
				} catch (SQLException e) {
					// Probably means that the connection was already closed.
					// Just ignore the exception.
				}
				thread2connIndex.remove(thrId);
			}
		}
	}

	private synchronized boolean isTesting() {
		if (_isTesting == null) {
// TODO: Check if we are running through JUnit...
//		https://stackoverflow.com/questions/2341943/how-can-i-find-out-if-code-is-running-inside-a-junit-test-or-not
// But for now, assume we always are...
//
			_isTesting = true;
		}
		return _isTesting;
	}

	protected Set<Long> activeThreadIDs() {
		Set<Long> threadIDs = new HashSet<Long>();
		for (Thread thr: Thread.getAllStackTraces().keySet()) {
			threadIDs.add(thr.getId());
		}
		return threadIDs;
	}
}
