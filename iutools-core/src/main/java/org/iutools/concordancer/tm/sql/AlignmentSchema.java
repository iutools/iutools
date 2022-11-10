package org.iutools.concordancer.tm.sql;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.sql.TableSchema;
import org.json.JSONObject;

public class AlignmentSchema extends TableSchema {

	public static final int MAX_SENTENCE_LEN = 1000;
	public static final int MAX_FROM_DOC_LEN = 100;
	// We assume that the alignments may contain every character from both
	// en and iu (hence x2) plus 50% more for punctuation etc (hence the 1.5x)
	public static final int MAX_WORD_ALIGN_LEN = (int)(2*1.5*MAX_SENTENCE_LEN);

	public AlignmentSchema() {
		super("Alignment", "id");
	}

	@Override
	public String[] unsortedColumnNames() {
		return new String[] {
			"from_doc", "pair_num", "web_domain", "topics_json", "langs_json",
			"en_text", "en_length", "iu_text", "iu_length", "word_aligns_json",
			"has_word_alignments",
		};
	}

	@Override
	public String[] schemaStatements() {
		String[] statements = new String[]{
				"CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n" +
				"  `langs_json` varchar(20) DEFAULT '[\"en\", \"fr\"]',\n" +
				"  `web_domain` varchar(200) DEFAULT '[]',\n" +
				"  `topics_json` varchar(200) DEFAULT '[]',\n" +
				"  `en_text` varchar("+MAX_SENTENCE_LEN+") DEFAULT '',\n" +
				"  `en_length` int(11) NOT NULL,\n" +
				"  `iu_text` varchar("+MAX_SENTENCE_LEN+") DEFAULT '',\n" +
				"  `iu_length` int(11) NOT NULL,\n" +
				"  `from_doc` varchar("+MAX_FROM_DOC_LEN+") NOT NULL,\n" +
				"  `pair_num` int(11) NOT NULL,\n" +
				"  `word_aligns_json` varchar("+MAX_WORD_ALIGN_LEN+") DEFAULT NULL,\n" +
				"  `has_word_alignments` boolean,\n" +
				"   PRIMARY KEY (from_doc, pair_num)\n"+
				") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

				"ALTER TABLE `"+tableName+"` ADD FULLTEXT(en_text);",
				"ALTER TABLE `"+tableName+"` ADD FULLTEXT(iu_text);",
			};
		return statements;
	}

	@Override
	protected boolean rowColValuesAreCompatible(JSONObject row) {
		Pair<String,Integer>[] blah = new Pair[] {};
		boolean compatible =
			rowColValuesAreCompatible(row, new Pair[] {
				Pair.of("web_domain", 200), Pair.of("topics_json", 200),
				Pair.of("en_text", MAX_SENTENCE_LEN),
				Pair.of("iu_text", MAX_SENTENCE_LEN),
				Pair.of("from_doc", MAX_FROM_DOC_LEN),
				Pair.of("word_aligns_json", MAX_WORD_ALIGN_LEN)
			});
		return compatible;
	}
}
