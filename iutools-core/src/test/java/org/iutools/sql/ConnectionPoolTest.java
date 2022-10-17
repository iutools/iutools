package org.iutools.sql;

import org.junit.Test;

import java.sql.Connection;

public class ConnectionPoolTest {

	@Test
	public void test__ConnectionPool__Synopsis() throws Exception {
		// Use ConnectionPool to get an SQL connection for a DB
		// Note:
		// - For each DB, there is only one connection per thread
		// - The connection for threads are closed automatically, so you
		//   do not need to close them yourself.
		//
		ConnectionPool pool = new ConnectionPool();
		Connection conn = pool.getConnection();
	}
}
