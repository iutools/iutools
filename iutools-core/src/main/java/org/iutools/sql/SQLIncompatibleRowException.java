package org.iutools.sql;

import java.sql.SQLException;

public class SQLIncompatibleRowException extends SQLException {
	public SQLIncompatibleRowException(String mess) {
		super(mess);
	}
}
