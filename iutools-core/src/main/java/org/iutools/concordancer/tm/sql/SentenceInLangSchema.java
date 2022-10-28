package org.iutools.concordancer.tm.sql;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.sql.Row;
import org.iutools.sql.TableSchema;

public class SentenceInLangSchema extends TableSchema {

	public static final int MAX_SENTENCE_LEN = 1000;
	public static final int MAX_FROM_DOC_LEN = 100;
	public static final int MAX_SENT_ID_LEN = MAX_FROM_DOC_LEN+20;

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
				"  `text` varchar("+MAX_SENTENCE_LEN+") NOT NULL,\n" +
				"  `from_doc` varchar("+MAX_FROM_DOC_LEN+") NOT NULL,\n" +
				"  `pair_num` int(11) DEFAULT 0,\n" +
				"  `sentence_id` varchar("+MAX_SENT_ID_LEN+") NOT NULL,\n" +
				"   PRIMARY KEY (lang, from_doc, pair_num)\n"+
				") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

				"ALTER TABLE `"+tableName+"` ADD FULLTEXT(text);",
			};
		return statements;
	}

	@Override
	public boolean rowIsCompatible(Row row) {
		String reasonForImcompatibility = null;
		//			Pair.of("text", MAX_SENTENCE_LEN)

		Pair<String,Integer>[] toCheck = new Pair[] {
			Pair.of("text", MAX_SENTENCE_LEN),
			Pair.of("from_doc", MAX_FROM_DOC_LEN),
			Pair.of("sentence_id", MAX_SENT_ID_LEN),
		};
		for (Pair<String,Integer> colsToCheck: toCheck) {
			String colName = colsToCheck.getLeft();
			Integer maxLen = colsToCheck.getRight();
			String gotVal = (String)row.getColumn(colName);
			Integer gotLen = gotVal.length();
			if (gotLen > maxLen) {
				reasonForImcompatibility =
					"String for "+colName+" was too long (="+gotLen+", max="+maxLen+")\n"+
					"Value was:\n"+gotVal;
				break;
			}
		}

		boolean answer = reasonForImcompatibility == null;
		return answer;
	}
}
