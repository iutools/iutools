package org.iutools.corpus.sql;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.dtrc.elasticsearch.DocIterator;
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
import org.iutools.corpus.WordWithMorpheme;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.morph.Decomposition;
import org.iutools.morph.r2l.DecompositionState;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.sql.ColValueIterator;
import org.iutools.sql.Row;
import org.iutools.sql.QueryProcessor;
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
			"SELECT * FROM "+WORDS_TABLE+"\n"+
			"WHERE\n"+
			"  `word` = ? AND \n" +
			"  `corpusName` = ?;";
      Pair<ResultSet,Connection> rsWithConn = query2(queryStr, word, corpusName);
      // Note: We do a try-with conn, so that the ResultSet's connection will
		// be closed when we are done with the result
		//
		try(Connection conn = rsWithConn.getRight()) {
			ResultSet rs = rsWithConn.getLeft();
			wordInfo = rs2winfo(rs);
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("returning wordInfo=\n"+new PrettyPrinter().print(wordInfo));
		}

		return wordInfo;
	}

	@Override
	public Iterator<WordInfo> winfosContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException {
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
				"SELECT word FROM " + WORDS_TABLE + "\n"+
				"  WHERE\n"+
				"    MATCH(wordNgrams) AGAINST(?) AND\n"+
			   "    corpusName = ? ";
			if (onlyWordsWithDecompositions) {
				queryStr += "AND\n    `topDecompositionStr` IS NOT NULL";
			}
			queryStr += ";";
			logger.trace("Querying with queryStr="+queryStr);
			Pair<ResultSet,Connection> rsWithConn = query2(queryStr, ngram, corpusName);
			// Note: We do a try-with conn, so that the ResultSet's conneciton will
			// be closed when we are done.
			try (Connection conn=rsWithConn.getRight()) {
				ResultSet rs = rsWithConn.getLeft();
				logger.trace("Done querying");
				List<WordInfo> winfos = QueryProcessor.rs2pojoLst(rs, WordInfo.class);
				for (WordInfo winfo : winfos) {
					words.add(winfo);
				}
				return words.iterator();
			}
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

	protected static String formatMorphNgram4SqlSearching(String[] morphemes) throws CompiledCorpusException {
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

	public Iterator<String> wordsContainingNgram(String ngram,
		SearchOption... options) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.wordsContainingNgram");
		logger.trace("invoked with ngram="+ngram+", corpusName="+corpusName);
		try {
			ngram = formatCharNgram4SqlSearching_wordNgramsField(ngram);
			logger.trace("formatted ngram="+ngram);
			boolean onlyWordsWithDecompositions = ArrayUtils.contains(options, SearchOption.EXCL_MISSPELLED);
			String selectWhat = " ";
			if (ArrayUtils.contains(options, SearchOption.WORD_ONLY)) {
				selectWhat = " word ";
			}
			List<String> words = new ArrayList<String>();
			String queryStr =
				"SELECT word FROM " + WORDS_TABLE + "\n"+
				"  WHERE\n"+
				"    MATCH(wordNgrams) AGAINST(?) AND\n"+
			   "    corpusName = ? ";
			if (onlyWordsWithDecompositions) {
				queryStr += "AND\n    `topDecompositionStr` IS NOT NULL";
			}
			queryStr += ";";
			logger.trace("Querying with queryStr="+queryStr);
			Pair<ResultSet,Connection> rsWithConn = query2(queryStr, ngram, corpusName);
			// Note: We do a try-with conn, so that the ResultSet's conneciton will
			// be closed when we are done.
			try (Connection conn=rsWithConn.getRight();
				  ResultSet rs = rsWithConn.getLeft()) {
				logger.trace("Done querying");
				List<WordInfo> winfos = QueryProcessor.rs2pojoLst(rs, WordInfo.class);
				for (WordInfo winfo : winfos) {
					words.add(winfo.word);
				}
				return words.iterator();
			}
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	@Override
	public DocIterator<WordInfo> wordInfosContainingNgram(String ngram, Set<String> fields) throws CompiledCorpusException {
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

	public List<WordWithMorpheme> wordsContainingMorpheme(String morpheme,
		Integer maxWords, String... sortCriteria) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.wordsContainingMorpheme");

		logger.trace("Invoked with morpheme=" + morpheme);

		List<WordWithMorpheme> words = new ArrayList<WordWithMorpheme>();
		if (logger.isErrorEnabled() && morpheme == null) {
			logger.error("morpheme is null");
		}
		if (morpheme != null) {
			String morphQuery = "%" + morpheme + "%";

			String queryStr =
			"SELECT * FROM " + WORDS_TABLE + "\n" +
			"  WHERE\n" +
			"    corpusName = ? AND \n" +
			"    topDecompositionStr LIKE ?\n";
			queryStr += sqlOrderBy(sortCriteria);
			if (maxWords != null) {
				queryStr += "\nLIMIT 0, " + maxWords;
			}
			queryStr += ";";
			Pair<ResultSet, Connection> rsWithConn = query2(queryStr, corpusName, morphQuery);
			// Note: We do a try-with conn, so that the ResultSet's connection will
			// be closed when we are done.
			//
			try (Connection conn = rsWithConn.getRight()) {
				ResultSet rs = rsWithConn.getLeft();
				List<WordInfo> wordInfos = null;
				try {
					wordInfos = QueryProcessor.rs2pojoLst(rs, new Sql2WordIinfo());
				} catch (SQLException e) {
					throw new CompiledCorpusException(e);
				}

				Pattern morphPatt = Pattern.compile("(^|\\s)([^\\s]*" + morpheme + "[^\\s]*)(\\s|$)");
				for (WordInfo winfo : wordInfos) {
					logger.trace("Looking at word " + winfo.word);

					String morphId = null;
					Matcher morphMatcher = morphPatt.matcher("\\{" + winfo.topDecompositionStr + "\\/");
					if (morphMatcher.find()) {
						morphId = morphMatcher.group(2);
					}

					String topDecomp =
					DecompositionState.formatDecompStr(
					winfo.topDecompositionStr,
					Morpheme.MorphFormat.WITH_BRACES);

					WordWithMorpheme aWord =
					new WordWithMorpheme(
					winfo.word, morphId, topDecomp,
					winfo.frequency, winfo.decompositionsSample);
					words.add(aWord);
				}
			} catch (SQLException e) {
				throw new CompiledCorpusException(e);
			}

			logger.trace("Returning");
		}

		return words;
	}

	@Override
	public Iterator<String> wordsContainingMorphNgram(String[] morphemes) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.wordsContainingMorphNgram");

		List<String> words = new ArrayList<String>();
		if (morphemes != null) {
			String decompQuery = formatMorphNgram4SqlSearching(morphemes);
			String queryStr =
				"SELECT word FROM " + WORDS_TABLE + "\n" +
				"  WHERE\n" +
				"    corpusName = ? AND \n" +
				"    topDecompositionStr LIKE ?";
			Pair<ResultSet, Connection> rsWithConn = this.query2(queryStr, corpusName, decompQuery);
			try (Connection conn = rsWithConn.getRight()) {
				ResultSet rs = rsWithConn.getLeft();
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

	private String sqlOrderBy(String[] sortCriteria) throws CompiledCorpusException {
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
			Pair<ResultSet, Connection> rsWithConn = query2(queryStr, corpusName);
			try {
				rsWithConn.getRight().close();
			} catch (SQLException e) {
				throw new CompiledCorpusException(e);
			}
		}
	}

	@Override
	public void deleteWord(String word) throws CompiledCorpusException {
		String queryStr =
			"DELETE FROM "+WORDS_TABLE+"\n"+
			"WHERE\n"+
			"  `corpusName` = ? AND\n"+
			"  `word` = ?;";
		Pair<ResultSet, Connection> rsWithConn = query2(queryStr, corpusName, word);
		try {
			rsWithConn.getRight().close();
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	@Override
	public Iterator<String> allWords() throws CompiledCorpusException {
		String queryStr =
			"SELECT word from "+WORDS_TABLE+"\n"+
			"WHERE\n"+
			"  corpusName = ?;";
		Pair<ResultSet, Connection> rsWithConn = query2(queryStr, corpusName);
		Iterator<String> iter = Collections.emptyIterator();
		try (Connection conn = rsWithConn.getRight()) {
			iter = new ColValueIterator<String>(rsWithConn.getLeft(), "word");
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
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
				Pair<ResultSet, Connection> rsWithConn = queryProcessor().query2(query, corpusName);
				rsWithConn.getRight().close();
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


	private Pair<ResultSet,Connection> query2(String queryStr, Object... queryArgs) throws CompiledCorpusException {
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
			String decompQuery = formatMorphNgram4SqlSearching(morphemes);
			String queryStr =
				"FROM " + WORDS_TABLE + "\n" +
				"  WHERE\n" +
				"    corpusName = ? AND \n" +
				"    topDecompositionStr LIKE ?";
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
		Pair<ResultSet, Connection> rsWithConn = query2(queryStr, corpusName);
		// Note: We do a try-with conn, so that the ResultSet's connection will
		// be closed when we are done.
		//
		try (Connection conn = rsWithConn.getRight()) {
			ResultSet rs = rsWithConn.getLeft();

			List<String> words = new ArrayList<String>();
			try {
				List<WordInfo> winfos = QueryProcessor.rs2pojoLst(rs, WordInfo.class);
				for (WordInfo winfo : winfos) {
					words.add(winfo.word);
				}
			} catch (SQLException e) {
				throw new CompiledCorpusException(e);
			}
			return words.iterator();
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	public long lastLoadedDate() throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.lastLoadedDate");

		Long date = new Long(0);
		tLogger.trace("invoked");
		String queryStr =
			  "SELECT * FROM "+LAST_LOADED_DATE_TABLE+"\n"+
			  "WHERE\n"+
			  "  `corpusName` = ?;";
		Pair<ResultSet, Connection> rsWithConn = query2(queryStr, corpusName);
		// Note: We do a try-with conn, so that the ResultSet's connection will
		// be closed when we are done.
		//
		try (Connection conn = rsWithConn.getRight()) {
			ResultSet rs = rsWithConn.getLeft();
			try {
				Map lastLoadedMap = QueryProcessor.rs2pojo(rs, Map.class);
				if (lastLoadedMap != null) {
					date = Long.parseLong((String) lastLoadedMap.get("timestamp"));
					int x = 1;
				}
			} catch (SQLException e) {
				throw new CompiledCorpusException(e);
			}
			return date;
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	public void changeLastUpdatedHistory() throws CompiledCorpusException {
		changeLastUpdatedHistory((Long)null);
	}

	protected void changeLastUpdatedHistory(Long timestamp) throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpus_SQL.changeLastUpdatedHistory");

		if (timestamp == null) {
			timestamp = System.currentTimeMillis();
		}
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
