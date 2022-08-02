package org.iutools.concordancer.tm.sql;

import org.iutools.sql.TableSchema;

public class AlignmentSchema extends TableSchema {

	public AlignmentSchema() {
		super("Alignment", "alignmentID");
	}

	public AlignmentSchema(String _tableName, String _idColumnName) {
		super(_tableName, _idColumnName);
	}

	@Override
	public String[] schemaStatements() {
		return new String[0];
	}
}
