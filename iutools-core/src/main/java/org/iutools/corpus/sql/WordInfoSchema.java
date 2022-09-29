package org.iutools.corpus.sql;

import org.iutools.sql.TableSchema;

public class WordInfoSchema extends TableSchema {

	public WordInfoSchema() {
		super("WordInfo", "word");
	}

	@Override
	public String[] schemaStatements() {
		String[] statements = new String[]{
				"CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n" +
				"  `word` varchar(100) NOT NULL,\n" +
				"  `corpusName` varchar(100) NOT NULL,\n" +
				"  `frequency` int(11) DEFAULT 0,\n" +
				"  `decompositionsSampleJSON` text DEFAULT NULL,\n" +
				"  `topDecompositionStr` text DEFAULT NULL,\n" +
				"  `totalDecompositions` int(11) DEFAULT 0,\n" +
				"  `wordInOtherScript` text NOT NULL,\n" +
				"  `wordNgrams` text NOT NULL,\n" +
				"  `morphemeNgrams` text NOT NULL,\n" +
				"  `wordRoman` text NOT NULL,\n" +
				"  `wordSyllabic` text NOT NULL,\n" +
				"   PRIMARY KEY (word, corpusName)\n"+
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

				"ALTER TABLE `WordInfo` ADD FULLTEXT(wordNgrams);",
				"ALTER TABLE `WordInfo` ADD FULLTEXT(morphemeNgrams);"
			};
		return statements;
	}
}