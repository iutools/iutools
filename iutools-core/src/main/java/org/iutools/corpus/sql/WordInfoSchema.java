package org.iutools.corpus.sql;

import org.iutools.sql.TableSchema;
import org.json.JSONObject;

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
				"  `morphemeNgramsWrittenForms` text NOT NULL,\n" +
				"  `wordRoman` text NOT NULL,\n" +
				"  `wordSyllabic` text NOT NULL,\n" +
				"   PRIMARY KEY (word, corpusName)\n"+
				") \n" +
				// Use MyISAM beacause it is faster than InnoDB and we don't need
				// transactional integrity.
				"  ENGINE=MyISAM DEFAULT CHARSET=utf8;",

				"ALTER TABLE `WordInfo` ADD FULLTEXT(wordNgrams);",
				"ALTER TABLE `WordInfo` ADD FULLTEXT(morphemeNgrams);",
				"ALTER TABLE `WordInfo` ADD FULLTEXT(morphemeNgramsWrittenForms);",
			};
		return statements;
	}

	@Override
	protected boolean rowColValuesAreCompatible(JSONObject row) {
		// Nothing to validate
		return true;
	}

	@Override
	public String[] unsortedColumnNames() {
		return
			new String[] {
				"word", "corpusName", "frequency", "decompositionsSampleJSON",
				"topDecompositionStr", "totalDecompositions",
				"wordInOtherScript", "wordNgrams", "morphemeNgrams",
				"morphemeNgramsWrittenForms", "wordRoman", "wordSyllabic",
			};
	}
}
