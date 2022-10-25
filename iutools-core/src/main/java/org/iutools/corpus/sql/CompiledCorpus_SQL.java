package org.iutools.corpus.sql;

import java.io.File;
import java.sql.*;
import java.util.*;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.UserIO;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.corpus.CompiledCorpus;
import static ca.nrc.dtrc.elasticsearch.request.Sort.Order;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import org.iutools.datastructure.CloseableIteratorWrapper;
import org.iutools.morph.Decomposition;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.sql.*;
import org.iutools.text.ngrams.NgramCompiler;

public class CompiledCorpus_SQL extends CompiledCorpus {

	private static final String WORDS_TABLE = new WordInfoSchema().tableName;
	private static final String LAST_LOADED_DATE_TABLE = new LastLoadedDateSchema().tableName;
	public String corpusName = null;

	public CompiledCorpus_SQL(String corpusName) throws CompiledCorpusException {
		super(corpusName);
		init__CompiledCorpus_SQL(corpusName);
		return;
	}

	private void init__CompiledCorpus_SQL(String _corpusName) throws CompiledCorpusException {
		corpusName = _corpusName;
		this.corpusName = _corpusName;
		try {
			new QueryProcessor().ensureTableIsDefined(new WordInfoSchema());
			new QueryProcessor().ensureTableIsDefined(new LastLoadedDateSchema());
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	@Override
	public long totalWords() throws CompiledCorpusException {
		String query =
			"FROM WordInfo\n"+
			"  WHERE `corpusName` = ?;"
			;
		long total = count(query, corpusName);

		return total;
	}

	@Override
	public long totalWordsWithDecomps() throws CompiledCorpusException {
		String query =
			"FROM WordInfo\n"+
			"WHERE\n"+
		   "  `corpusName` = ? AND\n" +
			"  `decompositionsSampleJSON` <> '[]';"
			;
		long total = count(query, corpusName);

		return total;
	}


	@Override
	public WordInfo info4word(String word) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.info4word");
		logger.trace("word="+word);
      WordInfo wordInfo = null;
      String queryStr =
			"SELECT word, frequency, totalDecompositions, topDecompositionStr, decompositionsSampleJSON FROM "+WORDS_TABLE+"\n"+
			"WHERE\n"+
			"  `word` = ? AND \n" +
			"  `corpusName` = ?;";
      try (ResultSet rs = query2(queryStr, word, corpusName)) {
			wordInfo = rs2winfo(rs);
			if (logger.isTraceEnabled()) {
				logger.trace("returning wordInfo=\n" + new PrettyPrinter().print(wordInfo));
			}
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}

		return wordInfo;
	}

	@Override
	public CloseableIterator<WordInfo> winfosContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.winfosContainingNgram");
		logger.trace("invoked with ngram="+ngram+", corpusName="+corpusName);
		try {
			ngram = formatCharNgram4SqlSearching_wordNgramsField(ngram);
			logger.trace("formatted ngram="+ngram);
			boolean onlyWordsWithDecompositions = ArrayUtils.contains(options, SearchOption.EXCL_MISSPELLED);
			String selectWhat = " ";
			if (ArrayUtils.contains(options, SearchOption.WORD_ONLY)) {
				selectWhat = " word ";
			}
			List<WordInfo> words = new ArrayList<WordInfo>();
			String queryStr =
				"SELECT * FROM " + WORDS_TABLE + "\n"+
				"  WHERE\n"+
				"    MATCH(wordNgrams) AGAINST(?) AND\n"+
			   "    corpusName = ? ";
			if (onlyWordsWithDecompositions) {
				queryStr += "AND\n    `topDecompositionStr` IS NOT NULL";
			}
			queryStr += "\n"+sqlOrderBy("frequency:desc");
			queryStr += ";";
			logger.trace("Querying with queryStr="+queryStr);
			// Note: We DON'T use try-with here because the returned iterator
			//   will manage the SQL resources and close them when it is not needed
			//   anymore
			//
			ResultSetWrapper rsw = new QueryProcessor().query3(queryStr, ngram, corpusName);
			logger.trace("Done querying");
			return rsw.iterator(new Sql2WordIinfo());
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	public void putInfo4word(WordInfo winfo) throws CompiledCorpusException {
		putInfo4word(winfo, (Boolean)null);
	}

	public void putInfo4word(WordInfo winfo, Boolean replace) throws CompiledCorpusException {
		List<WordInfo> justOneWord = new ArrayList<WordInfo>();
		justOneWord.add(winfo);
		putInfo4words(justOneWord);
		return;
	}

	public void putInfo4words(List<WordInfo> winfos) throws CompiledCorpusException {
		putInfo4words(winfos, (Boolean)null);
	}

	public void putInfo4words(List<WordInfo> winfos, Boolean replace) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.putInfo4words");
		if (winfos != null && winfos.size() > 0) {
			List<Row> rows = new ArrayList<Row>();
			for (WordInfo winfo: winfos) {
				WordInfo_SQL winfoSQL = new WordInfo_SQL(winfo);
				Row row = winfoSQL.toSQLRow();
				row.setColumn("corpusName", corpusName);
				rows.add(row);
			}
			try {
				new QueryProcessor().insertRows(rows, true);
			} catch (SQLException e) {
				throw new CompiledCorpusException(e);
			}
		}
		return;

	}

	private WordInfo rs2winfo(ResultSet rs) throws CompiledCorpusException {
		WordInfo wordInfo = null;
		try {
			wordInfo = QueryProcessor.rs2pojo(rs, new Sql2WordIinfo());
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
		return wordInfo;
	}

	@Override
	public boolean containsWord(String word) throws CompiledCorpusException {
		WordInfo winfo = info4word(word);
		boolean answer = (winfo != null);
		return answer;
	}


	protected String formatCharNgram4SqlSearching_wordNgramsField(String ngram) throws CompiledCorpusException {
		try {
			ngram = TransCoder.ensureScript(TransCoder.Script.ROMAN, ngram);
		} catch (TransCoderException e) {
			throw new CompiledCorpusException(e);
		}
		ngram = ngram.replaceAll("\\s+", "");
		ngram = new NgramCompiler().replaceCaretAndDollar(ngram);
		return ngram;
	}

	protected static String formatMorphNgram4SqlSearching_OLD(String... morphemes) throws CompiledCorpusException {
		String ngram = "";
		if (morphemes != null && morphemes.length > 0) {
			boolean atStart = false;
			if (morphemes[0].equals("^")) {
				atStart = true;
				morphemes = Arrays.copyOfRange(morphemes, 1, morphemes.length);
			}
			if (morphemes.length > 0) {
				if (morphemes[0].startsWith("^")) {
					atStart = true;
					morphemes[0] = morphemes[0].substring(1);
				}
				boolean atEnd = false;
				if (morphemes.length > 0 && morphemes[morphemes.length - 1].equals("$")) {
					atEnd = true;
					morphemes = Arrays.copyOfRange(morphemes, 0, morphemes.length - 1);
				}
				String lastMorpheme = morphemes[morphemes.length - 1];
				if (lastMorpheme.endsWith("$")) {
					atEnd = true;
					lastMorpheme = lastMorpheme.substring(0, lastMorpheme.length() - 1);
					morphemes[morphemes.length - 1] = lastMorpheme;
				}

				ngram = Decomposition.morphIDs2DecompString(morphemes);
				ngram = ngram.replaceAll("(^\\s+|\\s+$)", "");
				ngram = ngram.replaceAll("\\}\\s*\\{", "\\} \\{");
				if (!atStart) {
					ngram = "%" + ngram;
				}
				if (!atEnd) {
					ngram = ngram + "%";
				}

				try {
					ngram = TransCoder.ensureScript(TransCoder.Script.ROMAN, ngram);
				} catch (TransCoderException e) {
					throw new CompiledCorpusException(e);
				}
			}
		}
		return ngram;
	}

	public CloseableIterator<String> wordsContainingNgram(String ngram,
		SearchOption... options) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.wordsContainingNgram");
		logger.trace("invoked with ngram="+ngram+", corpusName="+corpusName);
		ResultSetColIterator<String> iter = null;
		ngram = formatCharNgram4SqlSearching_wordNgramsField(ngram);
		logger.trace("formatted ngram="+ngram);
		boolean onlyWordsWithDecompositions = ArrayUtils.contains(options, SearchOption.EXCL_MISSPELLED);
		String selectWhat = " * ";
		if (ArrayUtils.contains(options, SearchOption.WORD_ONLY)) {
			// We only care about the value of the 'word' field
			selectWhat = " word ";
		}
		List<String> words = new ArrayList<String>();
		String queryStr =
			"SELECT "+selectWhat+" FROM " + WORDS_TABLE + "\n"+
			"  WHERE\n"+
			"    MATCH(wordNgrams) AGAINST(?) AND\n"+
			"    corpusName = ? ";
		if (onlyWordsWithDecompositions) {
			// We only want words that have a decomposition
			queryStr += "AND\n    `topDecompositionStr` IS NOT NULL";
		}
		queryStr += "\n"+sqlOrderBy("frequency:desc");
		queryStr += ";";
		logger.trace("Querying with queryStr="+queryStr);
		try {
			ResultSetWrapper rsw = new QueryProcessor().query3(queryStr, ngram, corpusName);
			logger.trace("Done querying");
			iter = rsw.colIterator("word", String.class);
		} catch (Exception e) {
			throw new CompiledCorpusException(e);
		}

		return iter;
	}

	@Override
	public CloseableIterator<WordInfo> wordInfosContainingNgram(String ngram, Set<String> fields) throws CompiledCorpusException {
		int x = 1/0;
		return null;
	}

	@Override
	public long totalWordsWithCharNgram(String ngram, SearchOption... options)
		throws CompiledCorpusException {
		ngram = formatCharNgram4SqlSearching_wordNgramsField(ngram);

		boolean onlyWordsWithDecompositions = ArrayUtils.contains(options, SearchOption.EXCL_MISSPELLED);
		String selectWhat = " ";
		if (ArrayUtils.contains(options, SearchOption.WORD_ONLY)) {
			selectWhat = " word ";
		}
		List<String> words = new ArrayList<String>();
		String queryStr =
			"FROM " + WORDS_TABLE + "\n" +
			"  WHERE \n"+
			"    corpusName = ? AND \n" +
			"    MATCH(wordNgrams) AGAINST(?)";
		if (onlyWordsWithDecompositions) {
			queryStr +=
				" AND\n    topDecompositionStr IS NOT NULL";
		}
		queryStr += ";";
		long total  = count(queryStr, corpusName, ngram);

		return total;
	}

	@Override
	public long totalWordsWithNgram(String ngram) throws CompiledCorpusException {
		String queryStr =
			"FROM " + WORDS_TABLE + "\n"+
			"  WHERE\n"+
			"    MATCH(wordNgrams) AGAINST(?) AND\n"+
			"    corpusName = ? ";
		long total = count(queryStr, ngram, corpusName);

		return total;
	}

	@Override
	public long totalOccurences() throws CompiledCorpusException {
		String queryStr =
			"FROM " + WORDS_TABLE + "\n" +
			"  WHERE \n"+
			"    `corpusName` = ? ;";
		Long total =
			aggregateNumerical("sum", "frequency", queryStr, corpusName)
			.longValue();

		return total;
	}

	@Override
	public long totalOccurencesOf(String word) throws CompiledCorpusException {
		WordInfo winfo = info4word(word);
		return winfo.frequency;
	}

	public List<WordInfo> wordsContainingMorpheme(String morpheme,
		Integer maxWords, String... sortCriteria) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.wordsContainingMorpheme");

		logger.trace("Invoked with morpheme=" + morpheme);

		List<WordInfo> wordInfos = new ArrayList<WordInfo>();

		String matchAgainstField = "morphemeNgramsWrittenForms";
		if (morpheme.contains("_") || morpheme.contains("/")) {
			// This is the morpheme ID, not just its written form
			matchAgainstField = "morphemeNgrams";
		}

		if (logger.isErrorEnabled() && morpheme == null) {
			logger.error("morpheme is null");
		}
		if (morpheme != null) {
			String morphQuery = WordInfo_SQL.formatNgramAsSearchableString(morpheme);

			String queryStr =
				"SELECT word, frequency, topDecompositionStr, decompositionsSampleJSON FROM " + WORDS_TABLE + "\n" +
				"WHERE\n" +
				"  corpusName = ? AND \n" +
				"  MATCH("+matchAgainstField+") AGAINST(?)";
			queryStr += sqlOrderBy(sortCriteria);
			if (maxWords != null) {
				queryStr += "\nLIMIT 0, " + maxWords;
			}
			queryStr += ";";
			// Note: We do a try-with conn, so that the ResultSet will be closed
			// when we are done.
			//
			try (ResultSet rs = query2(queryStr, corpusName, morphQuery)) {
				wordInfos = QueryProcessor.rs2pojoLst(rs, new Sql2WordIinfo());
			} catch (SQLException e) {
				throw new CompiledCorpusException(e);
			}

			logger.trace("Returning");
		}

		logger.trace("Returnin total of "+wordInfos.size()+" words");
		return wordInfos;
	}

	@Override
	public Iterator<String> wordsContainingMorphNgram(String[] morphemes) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.wordsContainingMorphNgram");

		List<String> words = new ArrayList<String>();
		if (morphemes != null) {
			String decompQuery = WordInfo_SQL.formatNgramAsSearchableString(morphemes);
			String queryStr =
				"SELECT word FROM " + WORDS_TABLE + "\n" +
				"WHERE\n" +
				"  corpusName = ? AND \n" +
				"  MATCH(morphemeNgrams) AGAINST(?)\n"
//				"ORDER BY frequency DESC"
				;
			queryStr += sqlOrderBy("frequency:desc");

			// We use Try-with to ensure that the ResultSet will be closed when
			// we are done (or if an exception is raised).
			try (ResultSet rs = this.query2(queryStr, corpusName, decompQuery)) {
				List<WordInfo> winfos = QueryProcessor.rs2pojoLst(rs, WordInfo.class);
				for (WordInfo winfo: winfos) {
					words.add(winfo.word);
				}
			} catch (SQLException e) {
				throw new CompiledCorpusException(e);
			}
		}
		return words.iterator();
	}

	private String sqlOrderBy(String... sortCriteria) throws CompiledCorpusException {
		String sql = "";
		int critCounter = 0;
		for (String criterion: sortCriteria) {
			critCounter++;
			Pair<String,Order> colNameAndOrder = parseSortOrderDescr(criterion);
			if (critCounter == 1) {
				sql += "ORDER BY ";
			} else {
				sql += ", ";
			}
			sql += "`"+colNameAndOrder.getLeft()+"` "+colNameAndOrder.getRight().toString().toUpperCase();
		}
		return sql;
	}

	@Override
	public void addWordOccurence(
		String word, String[][] sampleDecomps, Integer totalDecomps,
		long freqIncr) throws CompiledCorpusException {

		Logger tLogger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.addWordOccurences");
		tLogger.trace("invoked, word="+word);

		WordInfo winfo = info4word(word);

		if (winfo == null) {
			// This word has yet to be added to the ES index
			winfo = new WordInfo(word);
		}

		winfo.frequency += freqIncr;
		winfo.setDecompositions(sampleDecomps, totalDecomps);
		putInfo4word(winfo);
		tLogger.trace("Exiting for word="+word);

		return;
	}

	@Override
	public void deleteAll(Boolean force) throws CompiledCorpusException {
		if (force == null) {
			force = false;
		}

		boolean delete = true;
		if (!force) {
			delete =
					new UserIO().prompt_yes_or_no(
							"Delete all content of the SQL corpus " +
									corpusName);
		}
		if (delete) {
			String queryStr =
				"DELETE FROM "+WORDS_TABLE+"\n"+
				"WHERE\n"+
				"  `corpusName` = ?;";
			// We use try-with to ensure that the ResultSet will be closed even if
			// an exception is raised.
			try (ResultSet rs = query2(queryStr, corpusName)){
			} catch (SQLException e) {
				throw new CompiledCorpusException(e);
			}
		}
		return;
	}

	@Override
	public void deleteWord(String word) throws CompiledCorpusException {
		String queryStr =
			"DELETE FROM "+WORDS_TABLE+"\n"+
			"WHERE\n"+
			"  `corpusName` = ? AND\n"+
			"  `word` = ?;";

		// We use try-with to ensure that the ResultSet will be closed even if
		// an exception is raised.
		try (ResultSet rs = query2(queryStr, corpusName, word)){
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	@Override
	public CloseableIterator<String> allWords() throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.allWords");
		String queryStr =
			"SELECT word from "+WORDS_TABLE+"\n"+
			"WHERE\n"+
			"  corpusName = ?;";

		CloseableIterator<String> iter = new CloseableIteratorWrapper<String>(Collections.emptyIterator());
		// Note: In this case, we DO NOT use try-with because the returned iterator
		//   requires that the ResultSet be still opened.
		//   When the iterator is finalized, it will close its result set.
		try {
			ResultSetWrapper rsw = new QueryProcessor().query3(queryStr, corpusName);
			iter = rsw.colIterator("word", String.class);
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
		logger.trace("Returning iter="+iter);
		return iter;
	}

	public void clearWords(Boolean promptUser) throws CompiledCorpusException {
		if (promptUser == null) {
			promptUser = true;
		}
		boolean shouldClear = true;
		promptUser = promptUser && totalWords() > 0;
		if (promptUser) {
			UserIO userIO = new UserIO().setVerbosity(UserIO.Verbosity.Level0);
			shouldClear = userIO.prompt_yes_or_no(
				"Corpus " + corpusName + " already exists." +
				"\nWould you like to overwrite it?\n");
		}
		try {
			if (shouldClear) {
				String query =
					"DELETE FROM " + WORDS_TABLE + "\n" +
					"WHERE `corpusName` = ?;";
				// We use try-with to ensure that the ResultSet will be closed even if
				// an exception is raised.
				try (ResultSet rs = queryProcessor().query2(query, corpusName)) {
				}
			}
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	public long totalWordsWithNoDecomp() throws CompiledCorpusException {
		String queryStr =
			"FROM " + WORDS_TABLE + "\n" +
			"  WHERE \n"+
			"    `corpusName` = ? AND\n" +
			"    `decompositionsSampleJSON` = '[]';";
		long total = count(queryStr, corpusName);

		return total;
	}

	public long totalOccurencesWithNoDecomp() throws CompiledCorpusException {
		String queryStr =
			"FROM " + WORDS_TABLE + "\n" +
			"  WHERE \n"+
			"    `corpusName` = ? AND\n" +
			"    `decompositionsSampleJSON` = '[]';";
		Long total =
			aggregateNumerical("sum", "frequency", queryStr, corpusName)
			.longValue();

		return total;
	}

	@Override
	public Long totalOccurencesWithDecomps() throws CompiledCorpusException {
		String queryStr =
			"FROM " + WORDS_TABLE + "\n" +
			"  WHERE \n"+
			"    `corpusName` = ? AND\n" +
			"    `decompositionsSampleJSON` <> '[]';";
		Long total =
			aggregateNumerical("sum", "frequency", queryStr, corpusName)
			.longValue();

		return total;
	}

	protected QueryProcessor queryProcessor() {
		return new QueryProcessor();
	}


	private ResultSet query2(String queryStr, Object... queryArgs) throws CompiledCorpusException {
		try {
			return new QueryProcessor().query2(queryStr, queryArgs);
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	private long count(String query, Object... queryArgs) throws CompiledCorpusException {
		try {
			return new QueryProcessor().count(query, queryArgs);
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	private Double aggregateNumerical(String aggrFctName, String fldName, String queryStr,
		Object... queryArgs) throws CompiledCorpusException {
		try {
			return new QueryProcessor().aggregateNumerical(aggrFctName, fldName, queryStr, queryArgs);
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	@Override
	public long morphemeNgramFrequency(String[] morphemes) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.morphemeNgramFrequency");

		Double freq = 0.0;
		if (morphemes != null) {
			String decompQuery = WordInfo_SQL.formatNgramAsSearchableString(morphemes);
			String queryStr =
				"FROM " + WORDS_TABLE + "\n" +
				"WHERE\n" +
				"  corpusName = ? AND \n" +
				"  MATCH(morphemeNgrams) AGAINST(?);";
			freq =
				aggregateNumerical("sum", "frequency", queryStr, corpusName, decompQuery);
		}
		return freq.longValue();
	}

	public Iterator<String> wordsWithNoDecomposition() throws CompiledCorpusException {
		String queryStr =
			"SELECT word FROM " + WORDS_TABLE + "\n"+
			"  WHERE\n" +
			"    corpusName = ? AND \n" +
			"    topDecompositionStr IS NULL"
			;

		List<String> words = new ArrayList<String>();
		// We use try-with to ensure that the ResultSet will be closed even
		// if an exception is raised.
		try (ResultSet rs = query2(queryStr, corpusName)){
			List<WordInfo> winfos = QueryProcessor.rs2pojoLst(rs, WordInfo.class);
			for (WordInfo winfo : winfos) {
				words.add(winfo.word);
			}
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
		return words.iterator();
	}

	public long lastLoadedDate() throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.lastLoadedDate");

		Long date = new Long(0);
		tLogger.trace("invoked");
		String queryStr =
			  "SELECT * FROM "+LAST_LOADED_DATE_TABLE+"\n"+
			  "WHERE\n"+
			  "  `corpusName` = ?;";
		// We use try-with to ensure that the ResultSet will be closed even
		// if an exception is raised.
		try (ResultSet rs = query2(queryStr, corpusName)) {
			Map lastLoadedMap = QueryProcessor.rs2pojo(rs, Map.class);
			if (lastLoadedMap != null) {
				date = Long.parseLong((String) lastLoadedMap.get("timestamp"));
			}
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
		return date;
	}

	protected void changeLastUpdatedHistory(Long timestamp) throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.changeLastUpdatedHistory");

		LastLoadedDate lastLoadedRecord = new LastLoadedDate();
		lastLoadedRecord.timestamp = timestamp;

		try {
			Row row = lastLoadedRecord.toSQLRow();
			row.setColumn("corpusName", corpusName);
			new QueryProcessor().replaceRow(row);
			tLogger.trace("DONE putting the updated winfo");
		} catch (SQLException e) {
			throw new CompiledCorpusException(
				"Error putting last loaded date for corpus "+corpusName, e);
		}

		return;
	}

	@Override
	public  void loadJsonFile(File jsonFile, Boolean verbose,
		Boolean overwrite, String corpusName) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.loadFromFile");
		if (verbose == null) {
			verbose = true;
		}
		if (overwrite == null) {
			overwrite = false;
		}
		if (corpusName == null) {
			if (this.corpusName != null) {
				corpusName = this.indexName;
			} else {
				corpusName = corpusName4File(jsonFile);
			}
		}

		new CompiledCorpus_SQLLoader(jsonFile, corpusName, overwrite, verbose)
			.load();

		return;
	}

	@Override
	public boolean exists() throws CompiledCorpusException {
		boolean allTablesDefined = true;

		try {
			for (String tableName : new String[]{
			new WordInfoSchema().tableName,
			new LastLoadedDateSchema().tableName
			}) {
				if (!new QueryProcessor().tableIsDefined(tableName)) {
					allTablesDefined = false;
					break;
				}
			}
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
		return allTablesDefined;
	}
}
