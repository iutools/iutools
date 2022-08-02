package org.iutools.corpus.sql;

import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import java.util.Iterator;

public class WordsIterator implements Iterator<WordInfo> {
	private static String WORDS_TABLE = new WordInfoSchema().tableName;

	private String corpusName = null;

	private int totalFetched = 0;
	private String lastFetchedWord = "___";
	private WordInfo currWord = null;


	public WordsIterator(String _corpusName) {
		corpusName = _corpusName;
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public WordInfo next() {
		return null;
	}

	private void fetchNext() throws CompiledCorpusException {
//		WordInfo wordInfo = null;
//		String queryStr =
//			"SELECT * word FROM "+WORDS_TABLE+"\n"+
//			"WHERE\n"+
//			"  corpusName = ? AND\n"+
//			"  word > ? ORDER BY word LIMIT 1;";
//		try {
//			Pair<ResultSet, Connection> rsWithConn =
//				new QueryProcessor().query2(queryStr, corpusName, lastNoidFetched);
//			try (Connection conn = rsWithConn.getRight()) {
//				ResultSet rs = rsWithConn.getLeft();
//				lastFetchedWinfo = QueryProcessor.rs2pojo(rs, new Sql2WordIinfo());
//			}
//		} catch (SQLException e) {
//			throw new CompiledCorpusException(e);
//		}

	}
}
