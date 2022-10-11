package org.iutools.corpus;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.dtrc.elasticsearch.request.Sort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.corpus.sql.LastLoadedDateSchema;
import ca.nrc.debug.Debug;
import ca.nrc.dtrc.elasticsearch.*;
import static ca.nrc.dtrc.elasticsearch.request.Sort.Order;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.datastructure.trie.StringSegmenter;
import org.iutools.datastructure.trie.StringSegmenterException;
import org.iutools.datastructure.trie.StringSegmenter_Char;
import org.iutools.datastructure.trie.Trie;
import org.iutools.sql.Row;
import org.iutools.text.ngrams.NgramCompiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.nrc.dtrc.elasticsearch.index.IndexAPI;

/** This implementation of CompiledCorpus uses ElasticSearch as its data store. */
public abstract class CompiledCorpus {

	public abstract boolean exists() throws CompiledCorpusException;

	public abstract void loadJsonFile(File jsonFile, Boolean verbose,
		Boolean overwrite, String indexName) throws CompiledCorpusException;

	protected abstract void changeLastUpdatedHistory(Long timestamp) throws CompiledCorpusException;

	public abstract long totalWords() throws CompiledCorpusException;

	public abstract long totalWordsWithDecomps() throws CompiledCorpusException;

	public abstract long totalWordsWithNoDecomp() throws CompiledCorpusException;

	public abstract long totalOccurencesWithNoDecomp() throws CompiledCorpusException;

	public abstract Long totalOccurencesWithDecomps() throws CompiledCorpusException;

	public abstract long totalWordsWithCharNgram(String ngram, SearchOption... options)
	throws CompiledCorpusException;

	public abstract long totalWordsWithNgram(String ngram) throws CompiledCorpusException;

	public abstract long totalOccurences() throws CompiledCorpusException;

	public abstract long totalOccurencesOf(String word) throws CompiledCorpusException;

	public abstract boolean containsWord(String word) throws CompiledCorpusException;

	public abstract void addWordOccurence(
	String word, String[][] sampleDecomps, Integer totalDecomps,
	long freqIncr) throws CompiledCorpusException;

	public abstract void deleteAll(Boolean force) throws CompiledCorpusException;

	public abstract void deleteWord(String word) throws CompiledCorpusException;

	public abstract Iterator<String> allWords() throws CompiledCorpusException;

	public abstract Iterator<String> wordsWithNoDecomposition() throws CompiledCorpusException;

	public abstract WordInfo info4word(String word) throws CompiledCorpusException;

	public abstract Iterator<WordInfo> winfosContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException;

	public abstract Iterator<String> wordsContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException;

	public abstract DocIterator<WordInfo> wordInfosContainingNgram(String ngram, Set<String> fields) throws CompiledCorpusException;

	public abstract List<WordInfo> wordsContainingMorpheme(String morpheme, Integer maxWords, String... sortCriteria) throws CompiledCorpusException;

	public abstract Iterator<String> wordsContainingMorphNgram(String[] morphemes) throws CompiledCorpusException;

	public abstract long morphemeNgramFrequency(String[] morphemes) throws CompiledCorpusException;

	public abstract long lastLoadedDate() throws CompiledCorpusException;


	public static enum SearchOption {EXCL_MISSPELLED, WORD_ONLY}

	;

	protected String indexName = null;

	public String getIndexName() {
		return indexName;
	}

	protected ESFactory _esFactory = null;
	public final static String WORD_INFO_TYPE = "WordInfo_ES";
	public final static WordInfo winfoPrototype = new WordInfo("");

	public int searchBatchSize = 100;

	protected static Pattern pattSavePath = Pattern.compile(".*?(^|[^/\\\\.]*)\\.ES\\.json$");
	protected boolean esClientVerbose = true;

	protected String segmenterClassName = StringSegmenter_Char.class.getName();
	protected transient StringSegmenter segmenter = null;

	protected int decompsSampleSize = 10;

	// If boolean is set to true, then this will contain the 'address' of the
	// SpellChecker.
	protected Integer address = null;

	@JsonIgnore
	public transient String name;

	protected transient NgramCompiler charsNgramCompiler = null;
	protected transient NgramCompiler morphsNgramCompiler = null;

	protected static Boolean debug = null;

	public CompiledCorpus(String _indexName) throws CompiledCorpusException {
		init_CompiledCorpus(_indexName, (Boolean) null);
	}

	public CompiledCorpus(String _indexName, Boolean createIfNotExists) throws CompiledCorpusException {
		init_CompiledCorpus(_indexName, createIfNotExists);
	}

	public void init_CompiledCorpus(String _indexName, Boolean createIfNotExists) throws CompiledCorpusException {
		this.indexName = IndexAPI.canonicalIndexName(_indexName);
		return;
	}

	public CompiledCorpus setName(String _name) {
		name = _name;
		return this;
	}

	public CompiledCorpus setSegmenterClassName(String className) {
		segmenterClassName = className;
		return this;
	}

	public CompiledCorpus setSegmenterClassName(
	Class<? extends StringSegmenter> segClass) {
		segmenterClassName = segClass.getName();
		return this;
	}

	public CompiledCorpus setDecompsSampleSize(int size) {
		return this;
	}

	public void addWordOccurences(String[] words)
	throws CompiledCorpusException {
		for (String aWord : words) {
			addWordOccurence(aWord);
		}
	}

	public void addWordOccurences(Collection<String> words)
	throws CompiledCorpusException {
		String[] wordsArr = new String[words.size()];
		int pos = 0;
		for (String aWord : words) {
			wordsArr[pos] = aWord;
			pos++;
		}
		addWordOccurences(wordsArr);
	}

	public void addWordOccurence(String word) throws CompiledCorpusException {
		addWordOccurence(word, false);
	}

	public void addWordOccurence(String word, boolean frequenciesOnly) throws CompiledCorpusException {

		String[][] sampleDecomps = null;
		Integer totalDecomps = null;
		if (!frequenciesOnly) {
			String[][] decomps = null;
			try {
				decomps = getSegmenter().possibleSegmentations(word);
			} catch (TimeoutException | StringSegmenterException e) {
				throw new CompiledCorpusException(e);
			}

			if (decomps == null) {
				// Analyser timed out before we could find a decomp.
				// Set decomps to empty array instead of null, to distinguish
				// this from a situation where we simply have not yet computed
				// the decompositions for a word.
				sampleDecomps = new String[0][];
				totalDecomps = 0;
			} else {
				totalDecomps = decomps.length;
				int numToKeep = Math.min(totalDecomps, decompsSampleSize);
				sampleDecomps = Arrays.copyOfRange(decomps, 0, numToKeep);
			}
		}
		addWordOccurence(word, sampleDecomps, totalDecomps);
		return;
	}

	public void addWordOccurence(String word, String[][] sampleDecomps,
		Integer totalDecomps) throws CompiledCorpusException {
		addWordOccurence(word, sampleDecomps, totalDecomps, 1);
	}

	public Iterator<String> wordsContainingNgram(String ngram)
	throws CompiledCorpusException {
		return wordsContainingNgram(ngram, new SearchOption[0]);
	}

	public long totalWordsWithCharNgram(String ngram)
	throws CompiledCorpusException {
		return totalWordsWithCharNgram(ngram, new SearchOption[0]);
	}

	@JsonIgnore
	public StringSegmenter getSegmenter() throws CompiledCorpusException {
		if (segmenter == null) {
			Class cls;
			try {
				cls = Class.forName(segmenterClassName);
			} catch (ClassNotFoundException e) {
				throw new CompiledCorpusException(e);
			}
			try {
				segmenter = (StringSegmenter) cls.newInstance();
			} catch (Exception e) {
				throw new CompiledCorpusException(e);
			}
		}
		return segmenter;
	}

	public String[] decomposeWord(String word) throws CompiledCorpusException {
		String[] segments;
		try {
			segments = getSegmenter().segment(word);
		} catch (TimeoutException | StringSegmenterException e) {
			throw new CompiledCorpusException(e);
		}
		return segments;
	}

	public void disactivateSegmenterTimeout() throws CompiledCorpusException {
		getSegmenter().disactivateTimeout();
	}

	public boolean containsCharNgram(String ngram) throws CompiledCorpusException {
		boolean answer = false;
		try {
			answer = wordsContainingNgram(ngram).hasNext();
		} catch (NoSuchCorpusException e) {
			// If the corpus does not exist, just leave answer to false
		}
		return answer;
	}

	@JsonIgnore
	protected NgramCompiler getCharsNgramCompiler() {
		if (charsNgramCompiler == null) {
			charsNgramCompiler = new NgramCompiler(3, 6, true);
		}
		return charsNgramCompiler;
	}

	@JsonIgnore
	protected NgramCompiler getMorphsNgramCompiler() {
		if (morphsNgramCompiler == null) {
			morphsNgramCompiler = new NgramCompiler(0, 3, true);
		}
		return morphsNgramCompiler;
	}

	public WordInfo mostFrequentWordExtending(String[] morphemes)
	throws CompiledCorpusException {
		WordInfo mostFrequent = null;
		WordInfo[] mostFrequentWords = mostFrequentWordsExtending(morphemes, 1);
		if (mostFrequentWords != null && mostFrequentWords.length > 0) {
			mostFrequent = mostFrequentWords[0];
		}
		return mostFrequent;
	}

	public List<WordInfo> wordsContainingMorpheme(String morpheme) throws CompiledCorpusException {
		return wordsContainingMorpheme(morpheme, (Integer) null,
			new String[]{"frequency:desc"});
	}

	protected void updateWordDecompositions(String word,
		String[][] wordDecomps) throws CompiledCorpusException {
		String[][] sampleDecomps = decompsSample(wordDecomps);
		Integer totalDecomps = null;
		if (wordDecomps != null) {
			totalDecomps = wordDecomps.length;
		}
		addWordOccurence(word, sampleDecomps, totalDecomps, 0);
	}

	protected String[][] decompsSample(String[][] allDecomps) {
		String[][] sample = null;
		if (allDecomps != null) {
			int sampleSize = Math.min(decompsSampleSize, allDecomps.length);
			sample = Arrays.copyOfRange(allDecomps, 0, sampleSize);
		}
		return sample;
	}

	public boolean isEmpty() throws CompiledCorpusException {
		boolean answer = (0 == totalWords());
		return answer;
	}

	protected boolean debugMode() throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus.debugMode");
		if (debug == null) {
			List<String> loggerNames = new ArrayList<String>();
			for (String reqType : new String[]{"POST", "PUT", "DELETE", "GET"}) {
				for (String when : new String[]{"before", "after"}) {
					loggerNames.add(when + reqType);
				}
			}
			for (String aName : loggerNames) {
				if (LogManager.getLogger(aName).isTraceEnabled()) {
					debug = true;
					break;
				}
			}
			if (debug == null) {
				debug = false;
			}
		}
		tLogger.trace("returning debug=" + debug);
		return debug;
	}

	public void loadFromFile(File jsonFile, Boolean verbose) throws CompiledCorpusException {
		loadFromFile(jsonFile, verbose, null, null);
	}

	public void loadFromFile(File jsonFile, Boolean verbose, Boolean overwrite) throws CompiledCorpusException {
		loadFromFile(jsonFile, verbose, overwrite, (String) null);
	}

	public void loadFromFile(File jsonFile, Boolean verbose, Boolean overwrite,
									 String corpusName) throws CompiledCorpusException {
		loadJsonFile(jsonFile, verbose, overwrite, corpusName);
		changeLastUpdatedHistory();
		return;
	}

	public void updateDecompositionsIndex(WordInfo winfo) throws CompiledCorpusException {

	}


	public void regenerateMorphNgramsIndex() throws CompiledCorpusException {

	}

	protected String traceLabel(String label) throws CompiledCorpusException {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder
		.append("[")
		.append(label);
		if (debugMode()) {
			sBuilder
			.append(", address=")
			.append(this.address);
		}
		sBuilder.append("]: ");

		return sBuilder.toString();
	}

	public String[] topDecomposition(String word) throws CompiledCorpusException {
		String[] decomp = null;
		WordInfo winfo = info4word(word);
		if (winfo != null && winfo.topDecomposition() != null) {
			decomp = winfo.topDecomposition();
		} else {
			try {
				decomp = getSegmenter().segment(word);
			} catch (StringSegmenterException e) {
				throw new CompiledCorpusException(e);
			} catch (TimeoutException e) {
				// If we timeout, just leave the decomp at null
			}
		}
		return decomp;
	}


	public WordInfo[] mostFrequentWordsExtending(String[] morphemes, Integer N) throws CompiledCorpusException {
		return new WordInfo[0];
	}

	public Trie getMorphNgramsTrie() throws CompiledCorpusException {
		return null;
	}

	protected static String insertSpaces(String orig) {
		String withSpaces = orig.replaceAll("(.)", "$1 ");
		return withSpaces;
	}

	public void deleteAll() throws CompiledCorpusException {
		deleteAll(null);
	}

	public static String corpusName4File(File savePath) {
		String corpusName = null;
		Matcher matcher = pattSavePath.matcher(savePath.toString());
		if (matcher.matches()) {
			corpusName = matcher.group(1);
		}
		return corpusName;
	}

	public boolean isUpToDateWithFile(File corpusFile)
	throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus.isUpToDateWithFile");
		boolean uptodate = false;

		try {
			long lastLoaded = lastLoadedDate();
			long fileLastChanged = corpusFile.lastModified();
			tLogger.trace("lastLoaded=" + lastLoaded + ", fileLastChanged=" + fileLastChanged);
			if (lastLoaded > fileLastChanged) {
				uptodate = true;
			}
		} catch (Exception e) {
			tLogger.trace("** Raised an exception: " + e.getMessage() + "\nCall stack was:" + Debug.printCallStack(e));
			throw e;
		}

		tLogger.trace("returning uptodate=" + uptodate);
		return uptodate;
	}

	public static class LastLoadedDate extends Document {
		public static final String esTypeName = "LastLoadedDate";

		public Long timestamp = null;
		public String corpusName = "";

		public LastLoadedDate() {
			// There should ever be only one record of type LastLoadedDate
			// and its ID should be the following:
			this.setId("lastload");
			this.type = esTypeName;

		}

		public Row toSQLRow() throws CompiledCorpusException {
			Row row = null;
			try {
				ObjectMapper mapper = new ObjectMapper();
				String jsonStr = mapper.writeValueAsString(this);
				JSONObject jsonObj = new JSONObject(jsonStr);
				jsonObj.remove("_detect_language");
				jsonObj.remove("content");
				jsonObj.remove("creationDate");
				jsonObj.remove("additionalFields");
				jsonObj.remove("id");
				jsonObj.remove("idWithoutType");
				jsonObj.remove("lang");
				jsonObj.remove("longDescription");
				jsonObj.remove("morphemesSpaceConcatenated");
				jsonObj.remove("shortDescription");
				LastLoadedDateSchema schema = new LastLoadedDateSchema();
				row = new Row(jsonObj, schema.tableName, schema.idColumnName);
			} catch (JsonProcessingException e) {
				throw new CompiledCorpusException(e);
			}

			return row;

		}
	}

	public static String canonizeCorpusName(String corpusName) {
		String canonical = corpusName;
		if (canonical != null) {
			canonical = canonical.toLowerCase();
		}
		return canonical;
	}

	public String canonicalName() {
		return canonizeCorpusName(indexName);
	}

	public static Pair<String, Order> parseSortOrderDescr(String critStr) throws CompiledCorpusException {
		String field = critStr;
		Sort.Order order = Order.asc;
		String[] parts = critStr.split("\\:");
		String errMess = "Invalid sorting criterion string: " + critStr;
		if (parts.length > 2) {
			throw new CompiledCorpusException(
			errMess +
			"\n  Should not have contained more than one occurence of ':'");
		} else if (parts.length == 2) {
			field = parts[0];
			try {
				order = Order.valueOf(parts[1].toLowerCase());
			} catch (Exception e) {
				throw new CompiledCorpusException(
				errMess +
				"\n  invalid sort order '" + parts[1] + "'");
			}
		}
		return Pair.of(field, order);
	}

	protected String[] replaceCaretAndDollar(String[] ngramArr) {
		String[] ngramArrRepl = Arrays.copyOfRange(ngramArr, 0, ngramArr.length);
		if (ngramArrRepl[0].equals("^")) {
			ngramArrRepl[0] = "BEGIN";
		}
		int last = ngramArrRepl.length - 1;
		if (ngramArrRepl[last].equals("$")) {
			ngramArrRepl[last] = "END";
		}
		return ngramArrRepl;
	}

	protected String replaceCaretAndDollar(String ngram) {
		ngram = ngram.replaceAll("^\\^", "BEGIN ");
		ngram = ngram.replaceAll("\\$$", " END");
		return ngram;
	}

	public void changeLastUpdatedHistory() throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus.changeLastUpdatedHistory");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("indexName=" + indexName + ";Unpon entry, last loaded date = " + lastLoadedDate());
		}
		Long now = System.currentTimeMillis();
		changeLastUpdatedHistory(now);
		return;
	}
}
