package org.iutools.cli;

import ca.nrc.datastructure.CloseableIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.corpus.WordInfo;
import org.iutools.corpus.sql.CompiledCorpus_SQL;
import org.iutools.corpus.sql.Row2WordInfo;
import org.iutools.sql.QueryProcessor;
import org.iutools.sql.ResultSetWrapper;

public class SPIKE__LoopThroughAllWordsInSQLdb {
	public static void main(String[] args) throws Exception {
		Logger logger = LogManager.getLogger("org.iutools.blah");
		logger.trace("Hello");

		System.out.println("Looping directly from SQL");
		loopDirectlyThroughSQL();
		
		try (CloseableIterator<String> iter = iteratorDirecltyFromSQL()) {
			System.out.println("\nLooping using an iterator generated directly from SQL");
			loopWithIterator(iter);
		}

		try (CloseableIterator<String> iter = iteratorFromCorpus()) {
			System.out.println("\n\nLooping using an iterator generated from CompiledCorpus_SQL");
			loopWithIterator(iter);
		}

		return;
	}

	private static void loopDirectlyThroughSQL() throws Exception {
		String sql = "SELECT * FROM WordInfo";
		ResultSetWrapper rsw = new QueryProcessor().query(sql);
		try (CloseableIterator<WordInfo> iter = rsw.iterator(new Row2WordInfo())) {
			int wordCounter = 0;
			String lastWord = null;
			while (iter.hasNext()) {
				wordCounter++;
				lastWord = iter.next().word;
//				System.out.println("word #"+wordCounter+"="+lastWord);
			}
			System.out.println("  Looped through "+wordCounter+" words. Last word was: "+lastWord);
		}
	}

	private static CloseableIterator<String> iteratorFromCorpus() throws Exception {
		CompiledCorpus_SQL corpus = (CompiledCorpus_SQL) new CompiledCorpusRegistry().getCorpus();
		CloseableIterator<String> iter = corpus.allWords();
		return iter;
	}

	private static void loopWithIterator(CloseableIterator<String> iter) throws Exception {
		int wordCounter = 0;
		String word = null;
		while (iter.hasNext()) {
			wordCounter++;
			word = iter.next();
//			System.out.println("  Word #"+wordCounter+"="+word);
		}
		System.out.println("  Looped through "+wordCounter+" words. Last word was: "+word);
	}

	private static CloseableIterator<String> iteratorDirecltyFromSQL() throws Exception {
		String sql = "SELECT word FROM WordInfo";
		ResultSetWrapper rsw = new QueryProcessor().query(sql);
		return rsw.colIterator("word", String.class);
	}
}
