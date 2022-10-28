package org.iutools.sql;

public abstract class TableSchema {

	public String tableName = null;
	public String idColumnName = null;

	public abstract String[] schemaStatements();

	public TableSchema(String _tableName, String _idColumnName) {
		init__TableSchema(_tableName, _idColumnName);
	}

	private void init__TableSchema(String _tableName, String _idColumnName) {
		this.tableName = _tableName;
		this.idColumnName = _idColumnName;
	}

	public boolean rowIsCompatible(Row row) {
		return true;
	}
}
