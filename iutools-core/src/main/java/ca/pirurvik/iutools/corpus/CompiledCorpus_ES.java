package ca.pirurvik.iutools.corpus;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.dtrc.elasticsearch.*;
import ca.nrc.dtrc.elasticsearch.request.*;
import ca.nrc.dtrc.elasticsearch.request._Source;
import ca.nrc.ui.commandline.UserIO;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.nrc.dtrc.elasticsearch.StreamlinedClient.ESOptions.CREATE_IF_NOT_EXISTS;
import static ca.nrc.dtrc.elasticsearch.StreamlinedClient.ESOptions.UPDATES_WAIT_FOR_REFRESH;

/**
 * CompiledCorpus that uses an ElasticSearch index to store information about
 * the words.
 */
public class CompiledCorpus_ES extends CompiledCorpus {

    String indexName = null;
    StreamlinedClient _esClient = null;
    public final String WORD_INFO_TYPE = "WordInfo_ES";
    public final WordInfo_ES winfoPrototype = new WordInfo_ES("");

    public int searchBatchSize = 100;

    static Pattern pattSavePath = Pattern.compile(".*?(^|[^/\\\\.]*)\\.ES\\.json$");
    private boolean esClientVerbose = true;

    public CompiledCorpus_ES(String _indexName) throws CompiledCorpusException {
        indexName = _indexName;
    }

    public CompiledCorpus_ES setIndexName(String _name) {
        this.indexName = _name;
        _esClient = null;
        return this;
    }

    public CompiledCorpus_ES setESClientVerbose(boolean verbose) {
        if (verbose) {
            esClientVerbose = verbose;
        }
        return this;
    }

    protected StreamlinedClient esClient() throws CompiledCorpusException {
        if (_esClient == null) {
            try {
                _esClient =
                        new StreamlinedClient(indexName, CREATE_IF_NOT_EXISTS, UPDATES_WAIT_FOR_REFRESH)
                            .setSleepSecs(0.0);
            } catch (ElasticSearchException e) {
                throw new CompiledCorpusException(e);
            }
        }
        if (!esClientVerbose) {
            _esClient.setUserIO(null);
        } else  {
            _esClient.setUserIO(new UserIO(UserIO.Verbosity.Level1));
        }
        return _esClient;
    }

    public  void loadFromFile(File jsonFile, Boolean verbose) throws CompiledCorpusException {
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
        if (overwrite == null) {
            overwrite = false;
        }

        if (indexName == null) {
            indexName = corpusName4File(jsonFile);
        }
        setIndexName(indexName);
        setESClientVerbose(verbose);

        if (esClient().indexExists()) {
            if (overwrite) {
                if (verbose) {
                    System.out.println("Clearing index "+indexName);
                }
                esClearIndex();
            }
        }

        if (!esClient().indexExists() || overwrite) {
            boolean forceIndexCreation = true;
            try {
                esClient().bulkIndex(jsonFile.toString(), WORD_INFO_TYPE, 100, verbose, forceIndexCreation);
            } catch (ElasticSearchException e) {
                throw new CompiledCorpusException(e);
            }
        }

        return;
    }

    @Override
    public long totalWords() throws CompiledCorpusException {
        long total = 0;
        try {
            SearchResults<WordInfo_ES> results =
                esClient().listAll(WORD_INFO_TYPE, winfoPrototype);
            total = results.getTotalHits();
        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException(e);
        }

        return total;
    }

    @Override
    public long totalOccurencesOf(String word) throws CompiledCorpusException {
        WordInfo_ES winfo =null;
        try {
            winfo =
                (WordInfo_ES) esClient()
                    .getDocumentWithID(word, WordInfo_ES.class, WORD_INFO_TYPE);
        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException(e);
        }
        return winfo.frequency;
    }

    @Override
    public List<WordWithMorpheme> wordsContainingMorpheme(String morpheme) throws CompiledCorpusException {
        Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus_ES.wordsContainingMorpheme");

        tLogger.trace("Invoked with morpheme="+morpheme);

        List<WordWithMorpheme> words = new ArrayList<WordWithMorpheme>();

        String query = morphNgramQuery(morpheme);

        SearchResults<WordInfo_ES> results = esWinfoSearch(query);
        Iterator<Hit<WordInfo_ES>> iter = results.iterator();
        Pattern morphPatt = Pattern.compile("(^|\\s)([^\\s]*"+morpheme+"[^\\s]*)(\\s|$)");
        while (iter.hasNext()) {
            WordInfo_ES winfo = iter.next().getDocument();

            String morphId = null;
            Matcher morphMatcher =morphPatt.matcher(winfo.topDecompositionStr);
            if (morphMatcher.find()) {
                morphId = morphMatcher.group(2);
            }

            String topDecomp =
                winfo.topDecompositionStr.replaceAll("\\s+", "");

            WordWithMorpheme aWord =
                new WordWithMorpheme(
                    winfo.word, morphId, topDecomp,
                    winfo.frequency);
            words.add(aWord);
        }

        tLogger.trace("Returning");

        return words;
    }

    private String morphNgramQuery(String[] morphemes) {
        String query =
            "morphemesSpaceConcatenated:\""+
            WordInfo_ES.insertSpaces(morphemes)+
            "\"";
        return query;
    }

    private String morphNgramQuery(String morpheme) {
        String[] morphemes = new String[] {morpheme};
        return morphNgramQuery(morphemes);
    }

    @Override
    public long morphemeNgramFrequency(String[] ngram) throws CompiledCorpusException {
        String query =
                "morphemesSpaceConcatenated:\""+
                        WordInfo_ES.insertSpaces(ngram)+
                        "\"";

        SearchResults<WordInfo_ES> results = esWinfoSearch(query);
        Iterator<Hit<WordInfo_ES>> iter = results.iterator();
        long freq = 0;
        while (iter.hasNext()) {
            freq += iter.next().getDocument().frequency;
        }

        return freq;
    }

    @Override
    public Iterator<String> allWords() throws CompiledCorpusException {
        SearchResults<WordInfo_ES> allWinfo = esListall();
        Iterator<String> wordsIter = allWinfo.docIDIterator();

        return wordsIter;
    }

    @Override
    public WordInfo info4word(String word) throws CompiledCorpusException {
        WordInfo_ES winfo = esGetDocumentWithID(word);

        return winfo;
    }

    @Override
    public void updateDecompositionsIndex(WordInfo winfo) throws CompiledCorpusException {

    }

    @Override
    public void regenerateMorphNgramsIndex() throws CompiledCorpusException {

    }

    @Override
    public Iterator<String> wordsContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException {
        return searchWordsContainingNgram(ngram, options).docIDIterator();
    }

    public SearchResults<WordInfo_ES> searchWordsContainingNgram(String ngram, SearchOption... options) throws CompiledCorpusException {
        String[] ngramArr = ngram.split("");
        ngramArr = replaceCaretAndDollar(ngramArr);
        String query =
            "+wordCharsSpaceConcatenated:\"" +
            WordInfo_ES.insertSpaces(ngramArr) +
            "\"";

        options = (SearchOption[]) ArrayUtils.add(options, SearchOption.WORD_ONLY);
        SearchResults<WordInfo_ES> results =
            esWinfoSearch(query, options);

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

    @Override
    public boolean containsWord(String word) throws CompiledCorpusException {
        WordInfo_ES winfo = null;
        try {
            winfo = (WordInfo_ES) esClient().getDocumentWithID(
                    word, WordInfo_ES.class, WORD_INFO_TYPE);
        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException(e);
        }

        boolean answer = (winfo != null);

        return answer;
    }

    @Override
    public Iterator<String> wordsContainingMorphNgram(String[] morphemes) throws CompiledCorpusException {
        Set<String> words = new HashSet<String>();
        String query = morphNgramQuery(morphemes);
        SearchResults<WordInfo_ES> hits = esWinfoSearch(query);

        return hits.docIDIterator();
    }

    @Override
    public long totalOccurences() throws CompiledCorpusException {
        SearchResults<WordInfo_ES> results = null;
        long total = 0;
        try {
            results = esClient().listAll(WORD_INFO_TYPE, winfoPrototype);
            Iterator<Hit<WordInfo_ES>> iter = results.iterator();
            while (iter.hasNext()) {
                Hit<WordInfo_ES> hit = iter.next();
                total += hit.getDocument().frequency;
            }
        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException(e);
        }
        return total;
    }

    @Override
    public long totalWordsWithNoDecomp() throws CompiledCorpusException {
        String query = "totalDecompositions:0";
        SearchResults<WordInfo_ES> results = esWinfoSearch(query);

        return results.getTotalHits();
    }

    @Override
    public long totalWordsWithDecomps() throws CompiledCorpusException {
        Query query = new Query();
        query
            .openAttr("bool")
                .openAttr("must")
                    .openAttr("exists")
                        .openAttr("field")
                        .setOpenedAttr("topDecompositionStr")
            ;
        SearchResults<WordInfo_ES> results = esWinfoSearch(query);

        return results.getTotalHits();
    }

    @Override
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

        SearchResults<WordInfo_ES> results = esWinfoSearch(query, aggs);
        Double totalDbl =
                (Double) results.aggrResult("totalOccurences");
        long total = Math.round(totalDbl);

        return total;
    }

    @Override
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

        SearchResults<WordInfo_ES> results = esWinfoSearch(query, aggs);
        Double totalDbl =
            (Double) results.aggrResult("totalOccurences");
        long total = Math.round(totalDbl);

        return total;
    }

    @Override
    public Iterator<String> wordsWithNoDecomposition() throws CompiledCorpusException {
        List<String> words = new ArrayList<String>();
        String query = "totalDecompositions:0";
        SearchResults<WordInfo_ES> hits = esWinfoSearch(query);

        Iterator<String> wordsIter = hits.docIDIterator();
        return wordsIter;
    }

    @Override
    public String[] bestDecomposition(String word) throws CompiledCorpusException {
        return new String[0];
    }

    @Override
    public WordInfo[] mostFrequentWordsExtending(String[] morphemes, Integer N) throws CompiledCorpusException {
        return new WordInfo[0];
    }

    public void addWordOccurence(
        String word, String[][] sampleDecomps, Integer totalDecomps,
        long freqIncr) throws CompiledCorpusException {

        Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus_ES.addWordOccurences");
        tLogger.trace("invoked, word="+word);

        WordInfo_ES winfo = null;
        try {
            winfo = (WordInfo_ES) esClient().getDocumentWithID(
                word, WordInfo_ES.class, WORD_INFO_TYPE);
        } catch (ElasticSearchException e) {
            // If this is a "no such index" exception, then don't worry.
            // It just means that the index is currently empty.
            if (!e.isNoSuchIndex()) {
                throw new CompiledCorpusException("Could not retrieve ElasticSearch info for word " + word, e);
            }
        }

        if (winfo == null) {
            // This word has yet to be added to the ES index
            winfo = new WordInfo_ES(word);
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

    @Override
    public Trie getMorphNgramsTrie() throws CompiledCorpusException {
        return null;
    }

    @Override
    public long totalWordsWithCharNgram(String ngram, SearchOption... options)
        throws CompiledCorpusException {
        Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus_ES.charNgramFrequency");
        tLogger.trace("invoked with ngram="+ngram);
        String query =
            "+wordCharsSpaceConcatenated:\""+
            WordInfo_ES.insertSpaces(ngram)+
            "\"";

        boolean statsOnly = true;
        SearchResults<WordInfo_ES> results =
            esWinfoSearch(query, options, statsOnly, (RequestBodyElement[]) null);

        long freq = results.getTotalHits();
        tLogger.trace("Returning freq="+freq);

        return freq;
    }

    private static String insertSpaces(String orig) {
        String withSpaces = orig.replaceAll("(.)", "$1 ");
        return withSpaces;
    }

    private SearchResults<WordInfo_ES> esWinfoSearch(String query, SearchOption[] options) throws CompiledCorpusException {
        return esWinfoSearch(query, options, (Boolean) null, new RequestBodyElement[0]);
    }

    private SearchResults<WordInfo_ES> esWinfoSearch(String query) throws CompiledCorpusException {
        return esWinfoSearch(query, new SearchOption[0], (Boolean) null, new RequestBodyElement[0]);
    }

    private SearchResults<WordInfo_ES> esWinfoSearch(
        String query, SearchOption[] options, Boolean statsOnly,
        RequestBodyElement... additionalReqBodies)
        throws CompiledCorpusException {
        SearchResults<WordInfo_ES> results = null;

        Pair<String,RequestBodyElement[]> augmentedRequest =
            augmentRequestWithOptions(query, additionalReqBodies, options, statsOnly);
        query = augmentedRequest.getLeft();
        additionalReqBodies = augmentedRequest.getRight();

        try {
            results =
                esClient().search(
                    query, WORD_INFO_TYPE, new WordInfo_ES(),
                    additionalReqBodies);

        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException(e);
        }

        return results;
    }

    private SearchResults<WordInfo_ES> esWinfoSearch(
            Query query, RequestBodyElement... additionalReqBodies) throws CompiledCorpusException {

        return esWinfoSearch(query, new SearchOption[0], (Boolean)null, additionalReqBodies);
    }


    private SearchResults<WordInfo_ES> esWinfoSearch(
        Query query, SearchOption[] options,
        Boolean statsOnly,
        RequestBodyElement... additionalReqBodies) throws CompiledCorpusException {

        Set<SearchOption> optionsSet = new HashSet<SearchOption>();
        Collections.addAll(optionsSet, options);
        SearchResults<WordInfo_ES> results = null;

        if (statsOnly == null) {
            statsOnly = false;
        }

        query =
            augmentRequestWithOptions(
                query, additionalReqBodies, options, statsOnly);
        try {
            results =
                esClient().search(
                    query, WORD_INFO_TYPE, new WordInfo_ES(), additionalReqBodies);
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
        RequestBodyElement[] additionalReqBodies, SearchOption[] options, Boolean statsOnly) {

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


    private WordInfo_ES esGetDocumentWithID(String word) throws CompiledCorpusException {
        WordInfo_ES winfo = null;
        try {
            winfo = (WordInfo_ES) esClient().getDocumentWithID(word, WordInfo_ES.class, WORD_INFO_TYPE);
        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException(e);
        }

        return winfo;
    }

    private SearchResults<WordInfo_ES> esListall() throws CompiledCorpusException {
        SearchResults<WordInfo_ES> allWinfos = null;
        Sort sort = new Sort();
        sort.sortBy("word", Sort.Order.asc);
        try {
            allWinfos =
                esClient().listAll(WORD_INFO_TYPE, winfoPrototype);
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

    protected static String corpusName4File(File savePath) {
        String corpusName = null;
        Matcher matcher = pattSavePath.matcher(savePath.toString());
        if (matcher.matches()) {
            corpusName = matcher.group(1);
        }
        return corpusName;
    }

    @Override
    public long totalWordsWithNgram(String ngram) throws CompiledCorpusException {
        return searchWordsContainingNgram(ngram).getTotalHits();
    }
}

