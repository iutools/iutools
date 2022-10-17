package org.iutools.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashSet;
import java.util.Set;

public class ResultSetUtils {
	private ResultSet rs = null;

	public ResultSetUtils(ResultSet _rs) {
		this.rs = _rs;
	}

	public Set<String> columnNames()  {
		Set<String> names = new HashSet<String>();
		try {
			ResultSetMetaData rsMetaData = rs.getMetaData();
			int count = rsMetaData.getColumnCount();
			for (int i = 1; i <= count; i++) {
				names.add(rsMetaData.getColumnName(i));
			}
		} catch (Exception e) {
			// For some reason, getColumnCount() sometimes crashes with a null
			// pointer exception. When that happens, assume the column names is
			// empty set
		}
		return names;
	}
}
