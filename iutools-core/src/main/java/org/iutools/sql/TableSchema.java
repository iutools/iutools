package org.iutools.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class TableSchema {

	public abstract String[] unsortedColumnNames();
	public abstract String[] schemaStatements();

	public String tableName = null;
	public String idColumnName = null;

	List<String> _colNames = null;

	ObjectMapper mapper = new ObjectMapper();

	public TableSchema(String _tableName, String _idColumnName) {
		init__TableSchema(_tableName, _idColumnName);
	}

	private void init__TableSchema(String _tableName, String _idColumnName) {
		this.tableName = _tableName;
		this.idColumnName = _idColumnName;
	}

	public List<String> columnNames() {
		if (_colNames == null) {
			_colNames = new ArrayList<String>();
			Collections.addAll(_colNames, unsortedColumnNames());
			Collections.sort(_colNames);
		}
		return _colNames;
	}

	public boolean rowIsCompatible(JSONObject row) throws SQLException {
		List<String> schemaColNames = columnNames();
		Set<String> rowColNames = row.keySet();
		String errMess = null;
		for (String colName: rowColNames) {
			if (!schemaColNames.contains(colName)) {
				try {
					errMess =
						"SQL ERROR:\n"+
						"Row column " + colName + " does not exist in schema " + getClass().getSimpleName() + ".\n" +
						"Row columns:\n " + mapper.writeValueAsString(rowColNames) + "\n" +
						"Schema colums:\n " + mapper.writeValueAsString(schemaColNames)
						;
					System.err.println(errMess);
				} catch (JsonProcessingException e) {
					throw new SQLException(e);
				}
			}
		}

		return (errMess == null);
	}

}
