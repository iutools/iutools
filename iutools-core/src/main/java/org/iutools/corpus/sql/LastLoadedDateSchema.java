package org.iutools.corpus.sql;

import org.iutools.sql.TableSchema;

public class LastLoadedDateSchema extends TableSchema {

	public LastLoadedDateSchema() {
		super("LastLoadedDate", "corpusName");
	}

	@Override
	public String[] schemaStatements() {
		String[] statements = new String[]{
			"CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n" +
			"  `corpusName` VARCHAR(100) NOT NULL,\n" +
			"  `timestamp` LONG NOT NULL,\n" +
			"  `lastload` LONG DEFAULT NULL,\n" +
			"   PRIMARY KEY (corpusName)\n" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n" +
			"\n"
		};
		return statements;
	}
}