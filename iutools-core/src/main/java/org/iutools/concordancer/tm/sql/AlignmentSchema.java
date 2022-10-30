package org.iutools.concordancer.tm.sql;

import org.iutools.sql.TableSchema;

public class AlignmentSchema extends TableSchema {

	public static final int MAX_SENTENCE_LEN = 1000;
	public static final int MAX_FROM_DOC_LEN = 100;
	public static final int MAX_SENT_ID_LEN = MAX_FROM_DOC_LEN+20;
	// We assume that the alignments may contain every character from both
	// en and iu (hence x2) plus 50% more for punctuation etc (hence the 1.5x)
	public static final int MAX_WORD_ALIGN_LEN = (int)(2*1.5*MAX_SENTENCE_LEN);

	public AlignmentSchema() {
		super("Alignment", "id");
	}

	@Override
	public String[] unsortedColumnNames() {
		return new String[] {
			"from_doc", "pair_num", "web_domain", "topics", "sentences", "langs_json",
			"en_text", "iu_text", "word_aligns_json"
		};
	}

	@Override
	public String[] schemaStatements() {
		String[] statements = new String[]{
				"CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n" +
				"  `langs_json` varchar(20) NOT NULL,\n" +
				"  `en_text` varchar("+MAX_SENTENCE_LEN+") NOT NULL,\n" +
				"  `iu_text` varchar("+MAX_SENTENCE_LEN+") NOT NULL,\n" +
				"  `from_doc` varchar("+MAX_FROM_DOC_LEN+") NOT NULL,\n" +
				"  `pair_num` int(11) DEFAULT 0,\n" +
				"  `word_aligns_json` varchar("+MAX_WORD_ALIGN_LEN+") DEFAULT 0,\n" +
				"   PRIMARY KEY (from_doc, pair_num)\n"+
				") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

				"ALTER TABLE `"+tableName+"` ADD FULLTEXT(en_text);",
				"ALTER TABLE `"+tableName+"` ADD FULLTEXT(iu_text);",
			};
		return statements;
	}
}
