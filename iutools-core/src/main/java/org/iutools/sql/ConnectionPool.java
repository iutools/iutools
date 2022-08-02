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
import java.util.Map;

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
		try {
			String dbName = new IUConfig().sqlDbName();
			if (isTesting()) {
				dbName += "_test";
			}
			BasicDataSource ds = dataSource4DB(dbName);
			connection = ds.getConnection();
		} catch (ConfigException e) {
			throw new SQLException(e);
		}

		return connection;
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
}
