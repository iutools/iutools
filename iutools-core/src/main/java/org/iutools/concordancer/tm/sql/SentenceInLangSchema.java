package org.iutools.concordancer.tm.sql;

import org.iutools.sql.TableSchema;

public class SentenceInLangSchema extends TableSchema {

	public SentenceInLangSchema() {
		super("AlignedSentences", "sentence_id");
	}

	public SentenceInLangSchema(String _tableName, String _idColumnName) {
		super(_tableName, _idColumnName);
	}

	@Override
	public String[] schemaStatements() {
		String[] statements = new String[] {
				"CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n" +
				"  `lang` varchar(2) NOT NULL,\n" +
				"  `text` varchar(100) NOT NULL,\n" +
				"  `from_doc` varchar(100) NOT NULL,\n" +
				"  `pair_num` int(11) DEFAULT 0,\n" +
				"   PRIMARY KEY (lang, from_doc, pair_num)\n"+
				") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

				"ALTER TABLE `WordInfo` ADD FULLTEXT(text);",
			};
		return statements;
	}
}
