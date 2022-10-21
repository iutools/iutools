package org.iutools.corpus.elasticsearch;


import ca.nrc.debug.Debug;
import ca.nrc.dtrc.elasticsearch.*;
import static ca.nrc.dtrc.elasticsearch.ESFactory.ESOptions;
import static ca.nrc.dtrc.elasticsearch.request.Sort.Order;

import ca.nrc.dtrc.elasticsearch.index.IndexAPI;
import ca.nrc.dtrc.elasticsearch.index.IndexDef;
import ca.nrc.dtrc.elasticsearch.request.*;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.UserIO;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.corpus.*;
import org.iutools.datastructure.CloseableIteratorWrapper;
import org.iutools.elasticsearch.ES;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.sql.CloseableIterator;
import org.json.JSONObject;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/** This implementation of CompiledCorpus uses ElasticSearch as its data store. */
public class CompiledCorpus_ES extends CompiledCorpus {

	public CompiledCorpus_ES(String _indexName) throws CompiledCorpusException {
		super(_indexName);
		init_CompiledCorpus_ES(_indexName, (Boolean)null);
	}

	public CompiledCorpus_ES(String _indexName, Boolean createIfNotExists) throws CompiledCorpusException {
		super(_indexName, createIfNotExists);
		init_CompiledCorpus_ES(_indexName, createIfNotExists);
	}

	public void init_CompiledCorpus_ES(String _indexName, Boolean createIfNotExists) throws CompiledCorpusException {
		if (debugMode()) {
			this.address = System.identityHashCode(this);
		}
		ensureESIndexIsDefined();
		return;
	}

	public CompiledCorpus setIndexName(String _name) {
		this.indexName = _name;
		_esFactory = null;
		return this;
	}

	public CompiledCorpus setESClientVerbose(boolean verbose) {
		if (verbose) {
			esClientVerbose = verbose;
		}
		return this;
	}

	public ESFactory esFactory() throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.esFactory");
		if (_esFactory == null) {
			try {
				_esFactory =
					ES.makeFactory(indexName, ESOptions.CREATE_IF_NOT_EXISTS, ESOptions.UPDATES_WAIT_FOR_REFRESH)
						.setSleepSecs(0.0)
						.setErrorPolicy(ErrorHandlingPolicy.LENIENT);
			} catch (ElasticSearchException e) {
				throw new CompiledCorpusException(e);
			}
			;
			// 2021-01-10: Setting this to false should speed things up, but it may corrupt
			// the ES index.
			_esFactory.synchedHttpCalls = false;
//			_esFactory.synchedHttpCalls = true;

			if (debugMode()) {
				logger.trace("Attaching observer to the ES index");
				try {
					_esFactory.attachObserver(
						new ObsEnsureAllRecordsAreWordInfo(esFactory()));
				} catch (ElasticSearchException e) {
					throw new CompiledCorpusException(e);
				}
			}
		}

		if (!esClientVerbose) {
			_esFactory.setUserIO(null);
		} else  {
			_esFactory.setUserIO(
				new UserIO(UserIO.Verbosity.Level1));
		}

		if (debugMode()) {
			try {
				_esFactory.attachObserver(
					new ObsEnsureAllRecordsAreWordInfo(esFactory()));
			} catch (ElasticSearchException e) {
				throw new CompiledCorpusException(e);
			}
		}

		return _esFactory;
	}

	protected void ensureESIndexIsDefined() throws CompiledCorpusException {
		try {
			if (!esFactory().indexAPI().exists()) {
				IndexDef idef = new IndexDef();
				idef.getTypeDef("*").getFieldDef("word").type = FieldDef.Types.keyword;
				idef.getTypeDef("*").getFieldDef("frequency").type = FieldDef.Types.integer;
				idef.getTypeDef("*").getFieldDef("totalDecompositions").type = FieldDef.Types.integer;
				esFactory().indexAPI().define(idef, true);
			}
		} catch (Exception e) {
			throw new CompiledCorpusException(e);
		}
	}

	@Override
	public boolean exists() throws CompiledCorpusException {
		boolean answer = false;
		try {
			answer = esFactory().indexAPI().exists();
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}
		return answer;
	}

	@Override
	public  void loadJsonFile(File jsonFile, Boolean verbose,
									  Boolean overwrite, String indexName) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus.loadFromFile");
		if (verbose == null) {
			verbose = true;
		}
		if (overwrite == null) {
			overwrite = false;
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

		List<ESOptions> options = new ArrayList<ESOptions>();
		if (verbose) {
			options.add(ESOptions.VERBOSE);
		}
		if (!overwrite) {
			options.add(ESOptions.APPEND);
		}

		ESFactory esFactory = esFactory();
		logger.trace("Using esFactory="+esFactory);
		boolean okToLoad = possiblyClearESIndex(overwrite, verbose);
		if (okToLoad) {
			try {
				possiblyDefineIndex();
				esFactory.indexAPI().bulkIndex(
					jsonFile.toString(), WORD_INFO_TYPE, 100,
					options.toArray(new ESOptions[0]));
			} catch (ElasticSearchException e) {
				throw new CompiledCorpusException(e);
			}
		}

		changeLastUpdatedHistory();

		try {
			esFactory().indexAPI().cacheIndexExists(true);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return;
	}

	protected void possiblyDefineIndex() throws CompiledCorpusException {
		try {
			if (!esFactory().indexAPI().exists()) {
				IndexDef iDef = new IndexDef();
				esFactory().indexAPI().define(iDef, true);
			}
		} catch (Exception e) {
			throw new CompiledCorpusException(e);
		}
		return;
	}

	@Override
	public void changeLastUpdatedHistory(Long timestamp) throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus.changeLastUpdatedHistory");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("indexName="+indexName+";Unpon entry, last loaded date = "+lastLoadedDate());
		}

		LastLoadedDate lastLoadedRecord = new LastLoadedDate();
		lastLoadedRecord.timestamp = timestamp;
		try {
			esFactory().crudAPI().putDocument(LastLoadedDate.esTypeName, lastLoadedRecord);
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
			IndexAPI indexAPI = esFactory().indexAPI();
			if (indexAPI.exists() && !indexAPI.isEmpty()) {
				if (clear == null) {
					clear =
						userIO.prompt_yes_or_no(
								"Corpus " + esFactory().indexName + " already exists." +
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

	@Override
	public long totalWords() throws CompiledCorpusException {
		long total = 0;
		try {
			SearchResults<WordInfo> results =
			esFactory().indexAPI().listAll(WORD_INFO_TYPE, winfoPrototype);
			total = results.getTotalHits();
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return total;
	}

	@Override
	public long totalOccurencesOf(String word) throws CompiledCorpusException {
		long frequency = 0;
		WordInfo winfo =null;
		try {
			winfo =
				(WordInfo) esFactory().crudAPI()
					.getDocumentWithID(word, WordInfo.class, WORD_INFO_TYPE);
			if (winfo != null) {
				frequency = winfo.frequency;;
			}
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}
		return frequency;
	}

	@Override
	public boolean containsWord(String word) throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus.containsWord");
		String traceLabel = this.traceLabel("word="+word);
		tLogger.trace(traceLabel+"invoked");
		WordInfo winfo = null;
		try {
			winfo =
				(WordInfo) esFactory().crudAPI().getDocumentWithID(
					word, WordInfo.class, WORD_INFO_TYPE, false);
		} catch (RuntimeException | ElasticSearchException e) {
			tLogger.trace(traceLabel + "raised exception e=" + e);
			tLogger.trace(traceLabel + "call stack was:" + Debug.printCallStack(e));
			throw new CompiledCorpusException(e);
		}

		boolean answer = (winfo != null);

		tLogger.trace(traceLabel+"exited");

		return answer;
	}

	@Override
	public void addWordOccurence(
		String word, String[][] sampleDecomps, Integer totalDecomps,
		long freqIncr) throws CompiledCorpusException {

		Logger tLogger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus.addWordOccurences");
		tLogger.trace("invoked, word="+word);

		ensureCorpusIndexIsDefined();

		WordInfo winfo = null;
		try {
			winfo = (WordInfo) esFactory().crudAPI().getDocumentWithID(
					word, WordInfo.class, WORD_INFO_TYPE);
		} catch (ElasticSearchException e) {
			// If this is a "no such index" exception, then don't worry.
			// It just means that the index is currently empty.
			if (!e.isNoSuchIndex()) {
				throw new CompiledCorpusException(
					"Could not retrieve ElasticSearch info for word " + word, e);
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
			esFactory().crudAPI().putDocument(WORD_INFO_TYPE, winfo);
			tLogger.trace("DONE putting the updated winfo");
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(
				"Error putting ES info for word "+word, e);
		}

		tLogger.trace("Exiting for word="+word);

		return;
	}

	protected void ensureCorpusIndexIsDefined() throws CompiledCorpusException {
		try {
			if (!esFactory().indexAPI().exists()) {
				esFactory().indexAPI().define(true);
			}
		} catch (Exception e) {
			throw new CompiledCorpusException(e);
		}
	}

	@Override
	public void deleteWord(String word) throws CompiledCorpusException {
		try {
			esFactory().crudAPI().deleteDocumentWithID(word, WORD_INFO_TYPE);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}
	}

	protected SearchResults<WordInfo> esWinfoSearch(String query, SearchOption[] options) throws CompiledCorpusException {
		return esWinfoSearch(query, options, (Boolean) null, new RequestBodyElement[0]);
	}

	protected SearchResults<WordInfo> esWinfoSearch(String query, RequestBodyElement... additionalReqBodies) throws CompiledCorpusException {
		return esWinfoSearch(query, new SearchOption[0], (Boolean) null, additionalReqBodies);
	}

	protected SearchResults<WordInfo> esWinfoSearch(String query) throws CompiledCorpusException {
		return esWinfoSearch(query, new SearchOption[0], (Boolean) null, new RequestBodyElement[0]);
	}

	protected SearchResults<WordInfo> esWinfoSearch(
		String query, SearchOption[] options, Boolean statsOnly,
		RequestBodyElement... additionalReqBodies)
		throws CompiledCorpusException, NoSuchCorpusException {
		return esWinfoSearch(query, options, statsOnly, (Integer)null,
			additionalReqBodies);
	}

	protected SearchResults<WordInfo> esWinfoSearch(
		String query, SearchOption[] options, Boolean statsOnly,
		Integer batchSize, RequestBodyElement... additionalReqBodies)
		throws CompiledCorpusException, NoSuchCorpusException {
		SearchResults<WordInfo> results = null;

		Pair<String,RequestBodyElement[]> augmentedRequest =
			augmentRequestWithOptions(query, additionalReqBodies, options, statsOnly);
		query = augmentedRequest.getLeft();
		additionalReqBodies = augmentedRequest.getRight();

		try {
			results =
				esFactory().searchAPI().search(
					query, WORD_INFO_TYPE, new WordInfo(), batchSize,
					additionalReqBodies);
		} catch (NoSuchIndexException exc) {
			throw new NoSuchCorpusException(exc);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return results;
	}

	protected SearchResults<WordInfo> esWinfoSearch(
			Query query, RequestBodyElement... additionalReqBodies) throws CompiledCorpusException {

		return esWinfoSearch(query, new SearchOption[0], (Boolean)null, additionalReqBodies);
	}

	protected SearchResults<WordInfo> esWinfoSearch(
		Query query, SearchOption[] options,
		Boolean statsOnly,
		RequestBodyElement... additionalReqBodies) throws CompiledCorpusException {
		return esWinfoSearch(query, options, statsOnly, (Integer)null, additionalReqBodies);
	}

	protected SearchResults<WordInfo> esWinfoSearch(
		Query query, SearchOption[] options,
		Boolean statsOnly, Integer batchSize,
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
			esFactory().searchAPI().search(
				query, WORD_INFO_TYPE, new WordInfo(), batchSize, additionalReqBodies);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return results;
	}

	protected Query augmentRequestWithOptions(
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
						ArrayUtils.add(additionalReqBodies, (RequestBodyElement)size);

		return query;
	}

	protected Pair<String,RequestBodyElement[]> augmentRequestWithOptions(String query,
		RequestBodyElement[] additionalReqBodies, SearchOption[] options,
  		Boolean statsOnly) {

		if (additionalReqBodies == null) {
			additionalReqBodies = new RequestBodyElement[0];
		}

		if (statsOnly == null) {
			statsOnly = false;
		}

		if (ArrayUtils.contains(options, SearchOption.EXCL_MISSPELLED)) {
			query = augmentQueryToExcludeMisspelled(query);
		}

		if (ArrayUtils.contains(options, SearchOption.WORD_ONLY)) {
			additionalReqBodies =
				(RequestBodyElement[]) ArrayUtils.add(additionalReqBodies,
				(RequestBodyElement)new _Source("id"));
		}

		Size size = new Size(searchBatchSize);
		if (statsOnly) {
			size = new Size(1);
		}
		additionalReqBodies =
			(RequestBodyElement[])
				ArrayUtils.add((RequestBodyElement[])additionalReqBodies, (RequestBodyElement)size);

		return Pair.of(query,additionalReqBodies);
	}

	protected String augmentQueryToExcludeMisspelled(String query) {
		query += " +totalDecompositions:>0";
		return query;
	}


	protected WordInfo esGetDocumentWithID(String word) throws CompiledCorpusException {
		WordInfo winfo = null;
		try {
			winfo =
				(WordInfo) esFactory().crudAPI()
					.getDocumentWithID(word, WordInfo.class, WORD_INFO_TYPE);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return winfo;
	}

	protected SearchResults<WordInfo> esListall() throws CompiledCorpusException {
		SearchResults<WordInfo> allWinfos = null;
		Sort sort = new Sort();
		sort.sortBy("_uid", Order.asc);
		try {
			allWinfos = esFactory().indexAPI()
				.listAll(WORD_INFO_TYPE, winfoPrototype, sort);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}

		return allWinfos;
	}

	protected void esClearIndex() throws CompiledCorpusException {
		try {
			esFactory().indexAPI().clear();
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}
	}

	@Override
	public CloseableIterator<String> allWords() throws CompiledCorpusException {
		SearchResults<WordInfo> allWinfo = esListall();
		CloseableIterator<String> wordsIter =
			new CloseableIteratorWrapper<String>(allWinfo.docIDIterator());

		return wordsIter;
	}

	@Override
	public WordInfo info4word(String word) throws CompiledCorpusException {
		WordInfo winfo = esGetDocumentWithID(word);

		return winfo;
	}

	@Override
	public Iterator<WordInfo> winfosContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException {
		Set<String> winfoFields = new HashSet<String>();
		winfoFields.add("frequency");
		winfoFields.add("word");
		winfoFields.add("id");
		return searchWordsContainingNgram(ngram, winfoFields, options).docIterator();
	}

	@Override
	public CloseableIterator<String> wordsContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException {
		Iterator<String> iter = searchWordsContainingNgram(ngram, options).docIDIterator(true);
		CloseableIterator<String> wrappedIter = new CloseableIteratorWrapper<String>(iter);
		return wrappedIter;
	}

	@Override
	public DocIterator<WordInfo> wordInfosContainingNgram(String ngram, Set<String> fields) throws CompiledCorpusException {
		SearchResults<WordInfo> results = searchWordsContainingNgram(ngram, fields);
		DocIterator<WordInfo> iter = results.docIterator();
		return iter;
	}

	public SearchResults<WordInfo> searchWordsContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException {
		return searchWordsContainingNgram(ngram, (Set<String>)null, options);
	}

	public SearchResults<WordInfo> searchWordsContainingNgram(String ngram,
		Set<String> winfoFields, SearchOption... options) throws CompiledCorpusException {

		TransCoder.Script queryScript = TransCoder.textScript(ngram);
		try {
			ngram = TransCoder.ensureScript(TransCoder.Script.ROMAN, ngram);
		} catch (TransCoderException e) {
			throw new CompiledCorpusException(e);
		}

		String[] ngramArr = ngram.split("");
		ngramArr = replaceCaretAndDollar(ngramArr);
		String query =
			"+wordCharsSpaceConcatenated:\"" +
				WordInfo.insertSpaces(ngramArr) +
				"\"";

		List<RequestBodyElement> additionalRequestEltsList = new ArrayList<RequestBodyElement>();
		additionalRequestEltsList.add(
			new Sort().sortBy("frequency", Order.desc));
		RequestBodyElement[] additionalRequestElts = new RequestBodyElement[0];
		if (winfoFields == null) {
			// If no fields are provided, just include the doc id
			additionalRequestEltsList.add(new _Source("id"));
		} else {
			// Only return the requested fields
			additionalRequestEltsList.add(
				new _Source(winfoFields.toArray(new String[0])));
		}
		Integer batchSize = new Integer(1000);
		SearchResults<WordInfo> results =
				esWinfoSearch(query, options, false, batchSize,
					additionalRequestEltsList.toArray(new RequestBodyElement[0]));

		return results;
	}

	protected String replaceCaretAndDollar(String ngram) {
		String[] ngramArr = ngram.split("");
		ngramArr = replaceCaretAndDollar(ngramArr);
		String modifiedNgram = String.join(" ", ngramArr);
		modifiedNgram  = modifiedNgram.replaceAll(" +", " ");
		return modifiedNgram;
	}

	protected String morphNgramQuery(String[] morphemes) {
		morphemes = replaceCaretAndDollar(morphemes);
		String query =
				"morphemesSpaceConcatenated:\""+
					WordInfo.insertSpaces(morphemes)+
					"\"";
		return query;
	}

	protected String morphNgramQuery(String morpheme) {
		String[] morphemes = new String[] {morpheme};
		return morphNgramQuery(morphemes);
	}

	@Override
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

	@Override
	public List<WordInfo> wordsContainingMorpheme(String morpheme,
		Integer maxWords, String... sortCriteria) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus_ES.wordsContainingMorpheme");

		logger.trace("Invoked with morpheme="+morpheme);
		List<WordInfo> winfos = new ArrayList<WordInfo>();

		if (logger.isErrorEnabled() && morpheme == null) {
				logger.error("morpheme is null");
		}
		if (morpheme != null) {

			String query = morphNgramQuery(morpheme);
			RequestBodyElement[] esSortCriteria = esSortCriteria(sortCriteria);

			SearchResults<WordInfo> results = esWinfoSearch(query, esSortCriteria);
			Iterator<Hit<WordInfo>> iter = results.iterator();
			if (logger.isErrorEnabled() && iter == null) {
				logger.error("*** morpheme="+morpheme+", iter is null");
			}

			logger.trace("# words found " + results.getTotalHits());

			Pattern morphPatt = Pattern.compile("(^|\\s)([^\\s]*" + morpheme + "[^\\s]*)(\\s|$)");
			while (iter.hasNext()) {
				WordInfo winfo = iter.next().getDocument();
				if (logger.isErrorEnabled()) {
					if (winfo == null) {
						logger.error("** winfo = null");
					} else if (winfo.word == null){
						logger.error("** winfo.word = null; winfo="+ PrettyPrinter.print(winfo));
					}
				}
				winfos.add(winfo);
				logger.trace("Looking at word " + winfo.word);
			}
		}

		logger.trace("Returning");

		return winfos;
	}

	private RequestBodyElement[] esSortCriteria(String[] sortCriteria) throws CompiledCorpusException {
		List<RequestBodyElement> esCriteria = new ArrayList<RequestBodyElement>();
		for (String critStr: sortCriteria) {
			Pair<String,Order> fieldAndOrder = parseSortOrderDescr(critStr);
			Sort esCrit =
				new Sort().sortBy(fieldAndOrder.getLeft(), fieldAndOrder.getRight());
			esCriteria.add(esCrit);
		}
		return esCriteria.toArray(new RequestBodyElement[0]);
	}

	@Override
	public Iterator<String> wordsContainingMorphNgram(String[] morphemes) throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus.wordsContainingMorphNgram");
		Set<String> words = new HashSet<String>();
		String query = morphNgramQuery(morphemes);
		SearchResults<WordInfo> hits = esWinfoSearch(query);

		if (logger.isTraceEnabled()) {
			logger.trace(
				"For morphemes="+ StringUtils.join(morphemes, ",")+
				", returning total of "+hits.getTotalHits()+" hits.");
		}
		return hits.docIDIterator(true);
	}

	@Override
	public long totalOccurences() throws CompiledCorpusException {
		Query queryBody = new Query(
			new JSONObject()
				.put("bool", new JSONObject()
					.put("must", new JSONObject()
						.put("match", new JSONObject()
							.put("type", WORD_INFO_TYPE)
						)
					)
				)
		);

		Aggs aggsElt = new Aggs()
				.aggregate("totalOccurences", "sum", "frequency");

		SearchResults<WordInfo> results =
				esWinfoSearch(queryBody, aggsElt);
		Double totalDbl = null;
		try {
			totalDbl = (Double) results.aggrResult("totalOccurences", Double.class);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}
		long total = Math.round(totalDbl);

		return total;
	}

	@Override
	public long totalWordsWithDecomps() throws CompiledCorpusException {
		Query query = new Query(
			new JSONObject()
			.put("bool", new JSONObject()
				.put("must", new JSONObject()
					.put("exists", new JSONObject()
						.put("field", "topDecompositionStr")
					)
				)
			)
		);

		SearchResults<WordInfo> results = esWinfoSearch(query);

		return results.getTotalHits();
	}

	@Override
	public long totalOccurencesWithNoDecomp() throws CompiledCorpusException {
		Query query = new Query(
			new JSONObject()
			.put("bool", new JSONObject()
				.put("must_not", new JSONObject()
					.put("exists", new JSONObject()
						.put("field", "topDecompositionStr")
					)
				)
			)
		);

		Aggs aggs = new Aggs();
		aggs.aggregate("totalOccurences", "sum", "frequency");

		SearchResults<WordInfo> results = esWinfoSearch(query, aggs);
		Double totalDbl =
		null;
		try {
			totalDbl = (Double) results.aggrResult("totalOccurences", Double.class);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}
		long total = Math.round(totalDbl);

		return total;
	}

	@Override
	public Long totalOccurencesWithDecomps() throws CompiledCorpusException {
		Query query = new Query(
			new JSONObject()
			.put("bool", new JSONObject()
				.put("must", new JSONObject()
					.put("exists", new JSONObject()
						.put( "field", "topDecompositionStr")
					)
				)
			)
		);

		Aggs aggs =
			new Aggs().aggregate("totalOccurences", "sum", "frequency");

		SearchResults<WordInfo> results = esWinfoSearch(query, aggs);
		Double totalDbl =
		null;
		try {
			totalDbl = (Double) results.aggrResult("totalOccurences", Double.class);
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}
		long total = Math.round(totalDbl);

		return total;
	}

	@Override
	public long totalWordsWithCharNgram(String ngram, SearchOption... options)
			throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus.totalWordsWithCharNgram");
		tLogger.trace("invoked with ngram="+ngram);
		try {
			ngram = TransCoder.ensureScript(TransCoder.Script.ROMAN, ngram);
		} catch (TransCoderException e) {
			throw new CompiledCorpusException(e);
		}
		ngram = WordInfo.insertSpaces(ngram);
		ngram = replaceCaretAndDollar(ngram);
		String query =
				"+wordCharsSpaceConcatenated:\""+
						ngram+
						"\"";

		boolean statsOnly = true;
		SearchResults<WordInfo> results =
				esWinfoSearch(query, options, statsOnly, (RequestBodyElement[]) null);

		long freq = results.getTotalHits();
		tLogger.trace("Returning freq="+freq);

		return freq;
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

	@Override
	public long totalWordsWithNgram(String ngram) throws CompiledCorpusException {
		if (1-1 == 0) throw new RuntimeException("Is this method redundant with totalWordsWithCharNgram?");
		return searchWordsContainingNgram(ngram).getTotalHits();
	}

	@Override
	public long lastLoadedDate() throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.CompiledCorpus.lastLoadedDate");

		tLogger.trace("invoked");
		Long date = null;
		IndexAPI indexAPI = null;
		try {
			indexAPI = esFactory().indexAPI();
			if (!indexAPI.exists()) {
				date = new Long(0);
			}
			if (date == null) {
				// Index exists. Check if its last loaded date
				// is greater than the last modified date for the json file.
				SearchResults<LastLoadedDate> results =
					esFactory().indexAPI()
						.listAll(LastLoadedDate.esTypeName, new LastLoadedDate());
				tLogger.trace(
					"indexName=" + indexName + "; number of records in load history = " +
					results.getTotalHits());
				Iterator<Hit<LastLoadedDate>> iter = results.iterator();
				while (iter.hasNext()) {
					Hit<LastLoadedDate> aHit = iter.next();
					long hitDate = aHit.getDocument().timestamp;
					if (date == null || hitDate > date) {
						date = hitDate;
					}
				}
			}
		} catch (NoSuchIndexException e) {
			// If index does not exist, leave date at 0
		} catch (ElasticSearchException e) {
			tLogger.trace(
				"Caught exception e=" + e + "\nCall stack: " + Debug.printCallStack(e));
			throw new CompiledCorpusException(e);
		}

		if (date == null) {
			date = new Long(0);
		}

		return date;
	}

	@Override
	public long totalWordsWithNoDecomp() throws CompiledCorpusException {
		String query = "totalDecompositions:0";
		SearchResults<WordInfo> results = esWinfoSearch(query);

		return results.getTotalHits();
	}

	@Override
	public Iterator<String> wordsWithNoDecomposition() throws CompiledCorpusException {
		List<String> words = new ArrayList<String>();
		String query = "totalDecompositions:0";
		SearchResults<WordInfo> hits = esWinfoSearch(query);

		Iterator<String> wordsIter = hits.docIDIterator(true);
		return wordsIter;
	}
}
