package org.iutools.corpus;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.iutools.linguisticdata.Morpheme;
import org.iutools.morph.Decomposition;
import ca.nrc.debug.Debug;
import ca.nrc.dtrc.elasticsearch.*;
import ca.nrc.dtrc.elasticsearch.request.*;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.datastructure.trie.StringSegmenter;
import org.iutools.datastructure.trie.StringSegmenterException;
import org.iutools.datastructure.trie.StringSegmenter_Char;
import org.iutools.datastructure.trie.Trie;
import org.iutools.text.ngrams.NgramCompiler;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import static ca.nrc.dtrc.elasticsearch.StreamlinedClient.ESOptions.CREATE_IF_NOT_EXISTS;
import static ca.nrc.dtrc.elasticsearch.StreamlinedClient.ESOptions.UPDATES_WAIT_FOR_REFRESH;

/**
 * This class stores stats about Inuktut words seen in a corpus, such as:
 * 
 * - frequency of words and ngrams
 * - word morphological decompositions
 *
 * For details on how to use this class, look at the synopsis test: @see org.iutools.corpus.CompiledCorpusTest#test__CompiledCorpus__Synopsis()
 * 
 * @author desilets
 *
 */
public class CompiledCorpus {

	public static enum SearchOption {EXCL_MISSPELLED, WORD_ONLY};

	private String indexName = null;
	public String getIndexName() {return indexName;}
	StreamlinedClient _esClient = null;
	public final String WORD_INFO_TYPE = "WordInfo_ES";
	public final WordInfo winfoPrototype = new WordInfo("");

	public int searchBatchSize = 100;

	static Pattern pattSavePath = Pattern.compile(".*?(^|[^/\\\\.]*)\\.ES\\.json$");
	private boolean esClientVerbose = true;

	protected String segmenterClassName = StringSegmenter_Char.class.getName();
	protected transient StringSegmenter segmenter = null;

	private int decompsSampleSize = 10;

	// If boolean is set to true, then this will contain the 'address' of the
	// SpellChecker.
	private Integer address = null;

	@JsonIgnore
	public transient String name;

	protected transient NgramCompiler charsNgramCompiler = null;
	protected transient NgramCompiler morphsNgramCompiler = null;

	private static Boolean debug = null;

	public CompiledCorpus(String _indexName) throws CompiledCorpusException {
		init_CompiledCorpus(_indexName);
	}

	public void init_CompiledCorpus(String _indexName) throws CompiledCorpusException {
		this.indexName = StreamlinedClient.canonicalIndexName(_indexName);
		if (debugMode()) {
			this.address = System.identityHashCode(this);
		}
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

	private void addWordOccurence(String word, String[][] sampleDecomps,
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
	protected StringSegmenter getSegmenter() throws CompiledCorpusException {
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
		} catch (TimeoutException | StringSegmenterException | LinguisticDataException | CompiledCorpusException e) {
			throw new CompiledCorpusException(e);
		}
		return segments;
	}
	
	public void disactivateSegmenterTimeout() throws CompiledCorpusException {
        getSegmenter().disactivateTimeout();
	}
	
	public boolean containsCharNgram(String ngram) throws CompiledCorpusException {
		boolean answer = wordsContainingNgram(ngram).hasNext();
		return answer;
	}
	
	@JsonIgnore
	protected NgramCompiler getCharsNgramCompiler() {
		if (charsNgramCompiler == null) {
			charsNgramCompiler = new NgramCompiler(3, 6,true);
		}
		return charsNgramCompiler;
	}	
	
	@JsonIgnore
	protected NgramCompiler getMorphsNgramCompiler() {
		if (morphsNgramCompiler == null) {
			morphsNgramCompiler = new NgramCompiler(0, 3,true);
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

	public CompiledCorpus setIndexName(String _name) {
		this.indexName = _name;
		_esClient = null;
		return this;
	}

	public CompiledCorpus setESClientVerbose(boolean verbose) {
		if (verbose) {
			esClientVerbose = verbose;
		}
		return this;
	}

	protected StreamlinedClient esClient() throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.esClient");
		if (_esClient == null) {
			try {
				_esClient =
					new StreamlinedClient(indexName, CREATE_IF_NOT_EXISTS, UPDATES_WAIT_FOR_REFRESH)
						.setSleepSecs(0.0);
			} catch (ElasticSearchException e) {
				throw new CompiledCorpusException(e);
			}

			if (debugMode()) {
				tLogger.trace("Attaching observer to the ES index");
				_esClient.attachObserver(new ObsEnsureAllRecordsAreWordInfo());
			}
		}

		if (!esClientVerbose) {
			_esClient.setUserIO(null);
		} else  {
			_esClient.setUserIO(new UserIO(UserIO.Verbosity.Level1));
		}

		if (debugMode()) {
			_esClient.attachObserver(new ObsEnsureAllRecordsAreWordInfo());
		}
		return _esClient;
	}

	private boolean debugMode() throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.CompiledCorpus.debugMode");
		if (debug == null) {
			List<String> loggerNames = new ArrayList<String>();
			for (String reqType : new String[]{"POST", "PUT", "DELETE", "GET"}) {
				for (String when : new String[]{"before", "after"}) {
					loggerNames.add(when + reqType);
				}
			}
			for (String aName: loggerNames) {
				if (Logger.getLogger(aName).isTraceEnabled()) {
					debug = true;
					break;
				}
			}
			if (debug == null) {
				debug = false;
			}
		}
		tLogger.trace("returning debug="+debug);
		return debug;
	}

	public void loadFromFile(File jsonFile, Boolean verbose) throws CompiledCorpusException {
		loadFromFile(jsonFile, verbose, null, null);
	}

	public  void loadFromFile(File jsonFile, Boolean verbose, Boolean overwrite) throws CompiledCorpusException {
		loadFromFile(jsonFile, verbose, overwrite, (String)null);
	}

	public  void loadFromFile(File jsonFile, Boolean verbose,
		Boolean overwrite, String indexName) throws CompiledCorpusException {
		if (verbose == null) {
			verbose = true;
		}
		if (indexName == null) {
			if (this.indexName != null) {
				indexName = this.indexName;
			} else {
				indexName = corpusName4File(jsonFile);
			}
		}
		setIndexName(indexName);
		setESClientVerbose(verbose);

		boolean okToLoad = possiblyClearESIndex(overwrite, verbose);
		if (okToLoad) {
			try {
				esClient().bulkIndex(
						jsonFile.toString(), WORD_INFO_TYPE, 100,
						verbose, overwrite);
			} catch (ElasticSearchException e) {
				throw new CompiledCorpusException(e);
			}
		}

		changeLastUpdatedHistory();

		return;
	}

	private void changeLastUpdatedHistory() throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.CompiledCorpus.changeLastUpdatedHistory");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("indexName="+indexName+";Unpon entry, last loaded date = "+lastLoadedDate());
		}

		LastLoadedDate lastLoadedRecord = new LastLoadedDate();
		lastLoadedRecord.timestamp = System.currentTimeMillis();
		try {
			esClient().putDocument(LastLoadedDate.esTypeName, lastLoadedRecord);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("indexName="+indexName+";Upon exit, last loaded date = "+lastLoadedDate());
		}

		return;

	}

	protected boolean possiblyClearESIndex(Boolean clear, boolean verbose)
			throws CompiledCorpusException {
		boolean okToLoad = true;
		UserIO userIO = new UserIO().setVerbosity(UserIO.Verbosity.Level0);
		try {
			Index index = esClient().getIndex();
			if (index.exists() && !index.isEmpty()) {
				if (clear == null) {
					clear =
						userIO.prompt_yes_or_no(
								"Corpus " + esClient().getIndexName() + " already exists." +
								"\nWould you like to overwrite it?\n");
				}
				if (clear) {
					if (verbose) {
						System.out.println("Clearing index "+indexName);
					}
					esClearIndex();
				} else {
					okToLoad = false;
				}
			}
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return okToLoad;
	}

	public long totalWords() throws CompiledCorpusException {
		long total = 0;
		try {
			SearchResults<WordInfo> results =
					esClient().listAll(WORD_INFO_TYPE, winfoPrototype);
			total = results.getTotalHits();
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return total;
	}

	public long totalOccurencesOf(String word) throws CompiledCorpusException {
		long frequency = 0;
		WordInfo winfo =null;
		try {
			winfo =
				(WordInfo) esClient()
					.getDocumentWithID(word, WordInfo.class, WORD_INFO_TYPE);
			if (winfo != null) {
				frequency = winfo.frequency;;
			}
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}
		return frequency;
	}

	public List<WordWithMorpheme> wordsContainingMorpheme(String morpheme) throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.CompiledCorpus.wordsContainingMorpheme");

		tLogger.trace("Invoked with morpheme="+morpheme);

		List<WordWithMorpheme> words = new ArrayList<WordWithMorpheme>();

		String query = morphNgramQuery(morpheme);

		SearchResults<WordInfo> results = esWinfoSearch(query);
		Iterator<Hit<WordInfo>> iter = results.iterator();
		tLogger.trace("# words found "+results.getTotalHits());

		Pattern morphPatt = Pattern.compile("(^|\\s)([^\\s]*"+morpheme+"[^\\s]*)(\\s|$)");
		while (iter.hasNext()) {
			WordInfo winfo = iter.next().getDocument();
			tLogger.trace("Looking at word "+winfo.word);

			String morphId = null;
			Matcher morphMatcher =morphPatt.matcher(winfo.morphemesSpaceConcatenated);

			if (morphMatcher.find()) {
				morphId = morphMatcher.group(2);
			}

			String topDecomp =
					Decomposition.formatDecompStr(
							winfo.topDecompositionStr,
							Morpheme.MorphFormat.WITH_BRACES);

			WordWithMorpheme aWord =
					new WordWithMorpheme(
							winfo.word, morphId, topDecomp,
							winfo.frequency, winfo.decompositionsSample);
			words.add(aWord);
		}

		tLogger.trace("Returning");

		return words;
	}

	private String morphNgramQuery(String[] morphemes) {
		morphemes = replaceCaretAndDollar(morphemes);
		String query =
				"morphemesSpaceConcatenated:\""+
					WordInfo.insertSpaces(morphemes)+
					"\"";
		return query;
	}

	private String morphNgramQuery(String morpheme) {
		String[] morphemes = new String[] {morpheme};
		return morphNgramQuery(morphemes);
	}

	
	public long morphemeNgramFrequency(String[] ngram) throws CompiledCorpusException {
		String query =
			"morphemesSpaceConcatenated:\""+
				WordInfo.insertSpaces(ngram)+
				"\"";

		SearchResults<WordInfo> results = esWinfoSearch(query);
		Iterator<Hit<WordInfo>> iter = results.iterator();
		long freq = 0;
		while (iter.hasNext()) {
			freq += iter.next().getDocument().frequency;
		}

		return freq;
	}

	
	public Iterator<String> allWords() throws CompiledCorpusException {
		SearchResults<WordInfo> allWinfo = esListall();
		Iterator<String> wordsIter = allWinfo.docIDIterator();

		return wordsIter;
	}

	
	public WordInfo info4word(String word) throws CompiledCorpusException {
		WordInfo winfo = esGetDocumentWithID(word);

		return winfo;
	}

	
	public void updateDecompositionsIndex(WordInfo winfo) throws CompiledCorpusException {

	}

	
	public void regenerateMorphNgramsIndex() throws CompiledCorpusException {

	}

	public Iterator<WordInfo> winfosContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException {
		Set<String> winfoFields = new HashSet<String>();
		winfoFields.add("frequency");
		winfoFields.add("word");
		winfoFields.add("id");
		return searchWordsContainingNgram(ngram, winfoFields, options).docIterator();
	}


	public Iterator<String> wordsContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException {
		return searchWordsContainingNgram(ngram, options).docIDIterator();
	}

	public SearchResults<WordInfo> searchWordsContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException {
		return searchWordsContainingNgram(ngram, (Set<String>)null, options);
	}

	public SearchResults<WordInfo> searchWordsContainingNgram(String ngram,
		Set<String> winfoFields, SearchOption... options) throws CompiledCorpusException {
		String[] ngramArr = ngram.split("");
		ngramArr = replaceCaretAndDollar(ngramArr);
		String query =
			"+wordCharsSpaceConcatenated:\"" +
				WordInfo.insertSpaces(ngramArr) +
				"\"";

		RequestBodyElement[] additionalRequestElts = new RequestBodyElement[0];
		if (winfoFields != null) {
			additionalRequestElts =
				new RequestBodyElement[] {
					new _Source(winfoFields.toArray(new String[0]))
				};
		}
		options = (SearchOption[]) ArrayUtils.add(options, SearchOption.WORD_ONLY);
		SearchResults<WordInfo> results =
				esWinfoSearch(query, options, false, additionalRequestElts);

		return results;
	}

	private String[] replaceCaretAndDollar(String[] ngramArr) {
		String[] ngramArrRepl = Arrays.copyOfRange(ngramArr, 0, ngramArr.length);
		if (ngramArrRepl[0].equals("^")) {
			ngramArrRepl[0] = "BEGIN";
		}
		int last = ngramArrRepl.length-1;
		if (ngramArrRepl[last].equals("$")) {
			ngramArrRepl[last] = "END";
		}
		return ngramArrRepl;
	}

	
	public boolean containsWord(String word) throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.CompiledCorpus.containsWord");
		String traceLabel = this.traceLabel("word="+word);
		tLogger.trace(traceLabel+"invoked");
		WordInfo winfo = null;
		try {
			winfo =
				(WordInfo) esClient().getDocumentWithID(
					word, WordInfo.class, WORD_INFO_TYPE);
		} catch (RuntimeException | ElasticSearchException e) {
			tLogger.trace(traceLabel+"raised exception e="+e);
			tLogger.trace(traceLabel+"call stack was:"+Debug.printCallStack(e));

			throw new CompiledCorpusException(e);
		}

		boolean answer = (winfo != null);

		tLogger.trace(traceLabel+"exited");

		return answer;
	}

	private String traceLabel(String label) throws CompiledCorpusException {
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
	
	public Iterator<String> wordsContainingMorphNgram(String[] morphemes) throws CompiledCorpusException {
		Set<String> words = new HashSet<String>();
		String query = morphNgramQuery(morphemes);
		SearchResults<WordInfo> hits = esWinfoSearch(query);

		return hits.docIDIterator();
	}

	
	public long totalOccurences() throws CompiledCorpusException {
		Query queryBody = new Query();
		queryBody
				.openAttr("match_all")
				.setOpenedAttr(new HashMap<String,String>());
		;
		Aggs aggsElt = new Aggs()
				.aggregate("totalOccurences", "sum", "frequency");
		SearchResults<WordInfo> results =
				esWinfoSearch(queryBody, aggsElt);
		Double totalDbl = (Double) results.aggrResult("totalOccurences");
		long total = Math.round(totalDbl);

		return total;
	}

	
	public long totalWordsWithNoDecomp() throws CompiledCorpusException {
		String query = "totalDecompositions:0";
		SearchResults<WordInfo> results = esWinfoSearch(query);

		return results.getTotalHits();
	}

	
	public long totalWordsWithDecomps() throws CompiledCorpusException {
		Query query = new Query();
		query
				.openAttr("bool")
				.openAttr("must")
				.openAttr("exists")
				.openAttr("field")
				.setOpenedAttr("topDecompositionStr")
		;
		SearchResults<WordInfo> results = esWinfoSearch(query);

		return results.getTotalHits();
	}

	
	public long totalOccurencesWithNoDecomp() throws CompiledCorpusException {
		Query query = new Query();
		query
				.openAttr("bool")
				.openAttr("must_not")
				.openAttr("exists")
				.openAttr( "field")
				.setOpenedAttr("topDecompositionStr")
		;

		Aggs aggs = new Aggs();
		aggs.aggregate("totalOccurences", "sum", "frequency");

		SearchResults<WordInfo> results = esWinfoSearch(query, aggs);
		Double totalDbl =
				(Double) results.aggrResult("totalOccurences");
		long total = Math.round(totalDbl);

		return total;
	}

	
	public Long totalOccurencesWithDecomps() throws CompiledCorpusException {
		Query query = new Query();
		query
				.openAttr("bool")
				.openAttr("must")
				.openAttr("exists")
				.openAttr( "field")
				.setOpenedAttr("topDecompositionStr")
		;

		Aggs aggs = new Aggs();
		aggs
				.openAttr("totalOccurences")
				.openAttr("sum")
				.openAttr("field")
				.setOpenedAttr("frequency")
		;

		SearchResults<WordInfo> results = esWinfoSearch(query, aggs);
		Double totalDbl =
				(Double) results.aggrResult("totalOccurences");
		long total = Math.round(totalDbl);

		return total;
	}

	
	public Iterator<String> wordsWithNoDecomposition() throws CompiledCorpusException {
		List<String> words = new ArrayList<String>();
		String query = "totalDecompositions:0";
		SearchResults<WordInfo> hits = esWinfoSearch(query);

		Iterator<String> wordsIter = hits.docIDIterator();
		return wordsIter;
	}

	
	public String[] bestDecomposition(String word) throws CompiledCorpusException {
		return new String[0];
	}

	
	public WordInfo[] mostFrequentWordsExtending(String[] morphemes, Integer N) throws CompiledCorpusException {
		return new WordInfo[0];
	}

	public void addWordOccurence(
			String word, String[][] sampleDecomps, Integer totalDecomps,
			long freqIncr) throws CompiledCorpusException {

		Logger tLogger = Logger.getLogger("org.iutools.corpus.CompiledCorpus.addWordOccurences");
		tLogger.trace("invoked, word="+word);

		WordInfo winfo = null;
		try {
			winfo = (WordInfo) esClient().getDocumentWithID(
					word, WordInfo.class, WORD_INFO_TYPE);
		} catch (ElasticSearchException e) {
			// If this is a "no such index" exception, then don't worry.
			// It just means that the index is currently empty.
			if (!e.isNoSuchIndex()) {
				throw new CompiledCorpusException("Could not retrieve ElasticSearch info for word " + word, e);
			}
		}

		if (winfo == null) {
			// This word has yet to be added to the ES index
			winfo = new WordInfo(word);
		}

		winfo.frequency += freqIncr;
		winfo.setDecompositions(sampleDecomps, totalDecomps);
		try {
			tLogger.trace("putting the updated winfo");
			esClient().putDocument(WORD_INFO_TYPE, winfo);
			tLogger.trace("DONE putting the updated winfo");
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException("Error putting ES info for word "+word, e);
		}

		tLogger.trace("Exiting for word="+word);

		return;
	}

	
	public void deleteWord(String word) throws CompiledCorpusException {
		try {
			esClient().deleteDocumentWithID(word, WORD_INFO_TYPE);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}
	}


	
	public Trie getMorphNgramsTrie() throws CompiledCorpusException {
		return null;
	}

	
	public long totalWordsWithCharNgram(String ngram, SearchOption... options)
			throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.CompiledCorpus.charNgramFrequency");
		tLogger.trace("invoked with ngram="+ngram);
		String query =
				"+wordCharsSpaceConcatenated:\""+
						WordInfo.insertSpaces(ngram)+
						"\"";

		boolean statsOnly = true;
		SearchResults<WordInfo> results =
				esWinfoSearch(query, options, statsOnly, (RequestBodyElement[]) null);

		long freq = results.getTotalHits();
		tLogger.trace("Returning freq="+freq);

		return freq;
	}

	private static String insertSpaces(String orig) {
		String withSpaces = orig.replaceAll("(.)", "$1 ");
		return withSpaces;
	}

	private SearchResults<WordInfo> esWinfoSearch(String query, SearchOption[] options) throws CompiledCorpusException {
		return esWinfoSearch(query, options, (Boolean) null, new RequestBodyElement[0]);
	}

	private SearchResults<WordInfo> esWinfoSearch(String query) throws CompiledCorpusException {
		return esWinfoSearch(query, new SearchOption[0], (Boolean) null, new RequestBodyElement[0]);
	}

	private SearchResults<WordInfo> esWinfoSearch(
			String query, SearchOption[] options, Boolean statsOnly,
			RequestBodyElement... additionalReqBodies)
			throws CompiledCorpusException {
		SearchResults<WordInfo> results = null;

		Pair<String,RequestBodyElement[]> augmentedRequest =
			augmentRequestWithOptions(query, additionalReqBodies, options, statsOnly);
		query = augmentedRequest.getLeft();
		additionalReqBodies = augmentedRequest.getRight();

		try {
			results =
					esClient().search(
							query, WORD_INFO_TYPE, new WordInfo(),
							additionalReqBodies);

		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return results;
	}

	private SearchResults<WordInfo> esWinfoSearch(
			Query query, RequestBodyElement... additionalReqBodies) throws CompiledCorpusException {

		return esWinfoSearch(query, new SearchOption[0], (Boolean)null, additionalReqBodies);
	}


	private SearchResults<WordInfo> esWinfoSearch(
			Query query, SearchOption[] options,
			Boolean statsOnly,
			RequestBodyElement... additionalReqBodies) throws CompiledCorpusException {

		Set<SearchOption> optionsSet = new HashSet<SearchOption>();
		Collections.addAll(optionsSet, options);
		SearchResults<WordInfo> results = null;

		if (statsOnly == null) {
			statsOnly = false;
		}

		query =
				augmentRequestWithOptions(
					query, additionalReqBodies, options, statsOnly);
		try {
			results =
					esClient().search(
							query, WORD_INFO_TYPE, new WordInfo(), additionalReqBodies);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return results;
	}

	private Query augmentRequestWithOptions(
			Query query, RequestBodyElement[] additionalReqBodies,
			SearchOption[] options, Boolean statsOnly)
			throws CompiledCorpusException {

		if (statsOnly == null) {
			statsOnly = false;
		}

		if (ArrayUtils.contains(options, SearchOption.EXCL_MISSPELLED)) {
			throw new CompiledCorpusException(
					"Option "+SearchOption.EXCL_MISSPELLED+
							" is currently not supported by this method");
		}

		Size size = new Size(searchBatchSize);
		if (statsOnly) {
			size = new Size(1);
		}
		additionalReqBodies =
				(RequestBodyElement[])
						ArrayUtils.add(additionalReqBodies, size);

		return query;
	}

	private Pair<String,RequestBodyElement[]> augmentRequestWithOptions(String query,
		RequestBodyElement[] additionalReqBodies, SearchOption[] options,
  		Boolean statsOnly) {

		if (statsOnly == null) {
			statsOnly = false;
		}

		if (ArrayUtils.contains(options, SearchOption.EXCL_MISSPELLED)) {
			query = augmentQueryToExcludeMisspelled(query);
		}

		if (ArrayUtils.contains(options, SearchOption.WORD_ONLY)) {
			additionalReqBodies =
				(RequestBodyElement[]) ArrayUtils.add(additionalReqBodies,
						new _Source("id"));
		}

		Size size = new Size(searchBatchSize);
		if (statsOnly) {
			size = new Size(1);
		}
		additionalReqBodies =
			(RequestBodyElement[])
				ArrayUtils.add(additionalReqBodies, size);

		return Pair.of(query,additionalReqBodies);
	}

	private String augmentQueryToExcludeMisspelled(String query) {
		query += " +totalDecompositions:>0";
		return query;
	}


	private WordInfo esGetDocumentWithID(String word) throws CompiledCorpusException {
		WordInfo winfo = null;
		try {
			winfo =
				(WordInfo) esClient()
					.getDocumentWithID(word, WordInfo.class, WORD_INFO_TYPE);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return winfo;
	}

	private SearchResults<WordInfo> esListall() throws CompiledCorpusException {
		SearchResults<WordInfo> allWinfos = null;
		Sort sort = new Sort();
		sort.sortBy("_uid", Sort.Order.asc);
		try {
			allWinfos =
					esClient().listAll(WORD_INFO_TYPE, winfoPrototype, sort);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return allWinfos;
	}

	private void esClearIndex() throws CompiledCorpusException {
		try {
			esClient().clearIndex();
		} catch (ElasticSearchException | IOException | InterruptedException e) {
			throw new CompiledCorpusException(e);
		}
	}

	public void deleteAll() throws CompiledCorpusException {
		deleteAll(null);
	}

	public void deleteAll(Boolean force) throws CompiledCorpusException {
		if (force == null) {
			force = false;
		}

		boolean delete = true;
		if (!force) {
			delete =
					new UserIO().prompt_yes_or_no(
							"Delete all content of the ElasticSearch corpus " +
									indexName);
		}
		if (delete) {
			try {
				esClearIndex();
			} catch (CompiledCorpusException e) {
				throw new CompiledCorpusException(e);
			}
		}
	}

	public static String corpusName4File(File savePath) {
		String corpusName = null;
		Matcher matcher = pattSavePath.matcher(savePath.toString());
		if (matcher.matches()) {
			corpusName = matcher.group(1);
		}
		return corpusName;
	}

	
	public long totalWordsWithNgram(String ngram) throws CompiledCorpusException {
		return searchWordsContainingNgram(ngram).getTotalHits();
	}

	public boolean isUpToDateWithFile(File corpusFile)
			throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.CompiledCorpus.isUpToDateWithFile");
		boolean uptodate = false;
		try {
			long lastLoaded = lastLoadedDate();
			long fileLastChanged = corpusFile.lastModified();
			tLogger.trace("lastLoaded=" + lastLoaded + ", fileLastChanged=" + fileLastChanged);
			if (lastLoaded > fileLastChanged) {
				uptodate = true;
			}
		} catch (Exception e) {
			tLogger.trace("** Raised an exception: "+e.getMessage()+"\nCall stack was:"+Debug.printCallStack(e));
			throw e;
		}

		tLogger.trace("returning uptodate="+uptodate);
		return uptodate;
	}

	public long lastLoadedDate() throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("org.iutools.corpus.CompiledCorpus.lastLoadedDate");

		long date = 0;
		LastLoadedDate lastLoadedRecord = null;
		try {
			SearchResults<LastLoadedDate> results =
					esClient()
							.listAll(LastLoadedDate.esTypeName, new LastLoadedDate());
			tLogger.trace(
					"indexName="+indexName+"; number of records in load history = "+
							results.getTotalHits());
			Iterator<Hit<LastLoadedDate>> iter = results.iterator();
			while (iter.hasNext()) {
				Hit<LastLoadedDate> aHit = iter.next();
				long hitDate = aHit.getDocument().timestamp;
				if (hitDate > date) {
					date = hitDate;
				}
			}
//            lastLoadedRecord = (LastLoadedDate) esClient()
//                .getDocumentWithID(
//                "lastloaded", LastLoadedDate.class, LastLoadedDate.esTypeName);
		} catch (ElasticSearchException e) {
			tLogger.trace(
				"Caught exception e="+e+"\nCall stack: "+Debug.printCallStack(e));
			throw new CompiledCorpusException(e);
		}
		tLogger.trace(
				"indexName="+indexName+"; last loaded date = "+
						((lastLoadedRecord == null) ? null: lastLoadedRecord.timestamp));
		if (lastLoadedRecord != null) {
			date = lastLoadedRecord.timestamp;
		}

		return date;
	}

	public static class LastLoadedDate extends Document {
		public static final String esTypeName = "LastLoadedDate";

		public Long timestamp = null;

		public LastLoadedDate() {
			// There should ever be only one record of type LastLoadedDate
			// and its ID should be the following:
			this.id = "lastload";
		}
	}
}
