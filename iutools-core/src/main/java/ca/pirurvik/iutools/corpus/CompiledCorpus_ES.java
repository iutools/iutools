package ca.pirurvik.iutools.corpus;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.dtrc.elasticsearch.*;
import ca.nrc.dtrc.elasticsearch.request.Query;
import ca.nrc.dtrc.elasticsearch.request.RequestBuilder;

import java.io.File;
import java.util.*;

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

    public CompiledCorpus_ES(String _indexName) throws CompiledCorpusException {
        indexName = _indexName;
    }

    protected StreamlinedClient esClient() throws CompiledCorpusException {
        if (_esClient == null) {
            try {
                _esClient =
                        new StreamlinedClient(indexName, CREATE_IF_NOT_EXISTS, UPDATES_WAIT_FOR_REFRESH)
                            .setSleepSecs(0.5);
            } catch (ElasticSearchException e) {
                throw new CompiledCorpusException(e);
            }

        }
        return _esClient;
    }


    public  void loadFromFile(File jsonFile, Boolean verbose) throws CompiledCorpusException {
        if (verbose == null) {
            verbose = true;
        }
        boolean forceIndexCreation = true;
        try {
            esClient().bulkIndex(jsonFile.toString(), WORD_INFO_TYPE, 100, verbose, forceIndexCreation);
        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException(e);
        }
    }

    // TODO-Sept2020: Modify listaLL method so you can pass it an aggregeation
    //  sepecification (Map<String,Object>). We can then pass an aggregation
    //  spec that asks for the sum of the frequencies.
    @Override
    public long totalWords() throws CompiledCorpusException {
        long total = 0;
        try {
            SearchResults<WordInfo_ES> results = esClient().listAll(WORD_INFO_TYPE, winfoPrototype);
            total = results.getTotalHits();
        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException(e);
        }

        return total;
    }

    @Override
    public long totalOccurencesOf(String word) throws CompiledCorpusException {
        return 0;
    }

    @Override
    public List<WordWithMorpheme> wordsContainingMorpheme(String morpheme) throws CompiledCorpusException {
        return null;
    }

    @Override
    public long morphemeNgramFrequency(String[] ngram) throws CompiledCorpusException {
        return 0;
    }

    @Override
    public Iterator<String> allWords() throws CompiledCorpusException {
        return null;
    }

    @Override
    public WordInfo info4word(String word) throws CompiledCorpusException {
        return null;
    }

    @Override
    public void updateDecompositionsIndex(WordInfo winfo) throws CompiledCorpusException {

    }

    @Override
    public void regenerateMorphNgramsIndex() throws CompiledCorpusException {

    }

    @Override
    public Set<String> wordsContainingNgram(String ngram) throws CompiledCorpusException {
        Set<String> matchingWords = new HashSet<String>();

        String[] ngramArr = ngram.split("");
        String query =
            "wordCharsSpaceConcatenated:\"" +
            WordInfo_ES.insertSpaces(ngram) +
            "\"";
        SearchResults<WordInfo_ES> results = searchES_freeform(query);
        Iterator<Hit<WordInfo_ES>> iter = results.iterator();
        while (iter.hasNext()) {
            Hit<WordInfo_ES> hit = iter.next();
            matchingWords.add(hit.document.word);
        }

        return matchingWords;
    }

    @Override
    public boolean containsWord(String word) throws CompiledCorpusException {
        WordInfo_ES winfo = null;
        try {
            winfo = (WordInfo_ES) esClient().getDocumentWithID(
                    word, WordInfo_ES.class, WORD_INFO_TYPE);
        } catch (ElasticSearchException e) {
//            if (!e.isNoSuchIndex()) {
            throw new CompiledCorpusException(e);
//            }
        }

        boolean answer = (winfo != null);

        return answer;
    }

    @Override
    protected Set<String> wordsContainingMorphNgram(String[] morphemes) throws CompiledCorpusException {
        return null;
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
        Query query = new Query();
        new RequestBuilder(query)
            .addObject("query")
                .addObject("bool")
                    .addObject("must_not")
                        .addObject("exists")
                            .addObject("field", "topDecompositionStr")
            .build();
        SearchResults<WordInfo_ES> results = searchES_queryMap(query);

        return results.getTotalHits();
    }

    @Override
    public long totalWordsWithDecomps() throws CompiledCorpusException {
        Query query = new Query();
            new RequestBuilder(query)
                .addObject("query")
                    .addObject("bool")
                        .addObject("must")
                            .addObject("exists")
                                .addObject("field", "topDecompositionStr")
                .build();
        SearchResults<WordInfo_ES> results = searchES_queryMap(query);

        return results.getTotalHits();
    }

    // TODO-Sept2020: Seperate the 'aggs' part from the 'query' part
    @Override
    public long totalOccurencesWithNoDecomp() throws CompiledCorpusException {
        Query query = new Query();
        new RequestBuilder(query)
            .addObject("query")
                .addObject("bool")
                    .addObject("must_not")
                        .addObject("exists")
                            .addObject( "field", "topDecompositionStr")
                            .closeObject()
                        .closeObject()
                    .closeObject()
                .closeObject()
            .closeObject()

            .addObject("aggs")
                .addObject("totalOccurences")
                    .addObject("sum")
                        .addObject("field", "frequency")

            .build()
            ;

        SearchResults<WordInfo_ES> results = searchES_queryMap(query);
        Double totalDbl =
                (Double) results.aggrResult("totalOccurences");
        long total = Math.round(totalDbl);

        return total;
    }

    // TODO-Sept2020: Seperate the 'aggs' part from the 'query' part
    @Override
    public Long totalOccurencesWithDecomps() throws CompiledCorpusException {
        Query query = new Query();
        new RequestBuilder(query)
            .addObject("query")
                .addObject("bool")
                    .addObject("must")
                        .addObject("exists")
                            .addObject( "field", "topDecompositionStr")
                            .closeObject()
                        .closeObject()
                    .closeObject()
                .closeObject()
            .closeObject()

            .addObject("aggs")
                .addObject("totalOccurences")
                    .addObject("sum")
                        .addObject("field", "frequency")

        .build()
        ;

        SearchResults<WordInfo_ES> results = searchES_queryMap(query);
        Double totalDbl =
            (Double) results.aggrResult("totalOccurences");
        long total = Math.round(totalDbl);

        return total;
    }

    @Override
    public Iterator<String> wordsWithNoDecomposition() throws CompiledCorpusException {
        return null;
    }

    @Override
    public String[] bestDecomposition(String word) throws CompiledCorpusException {
        return new String[0];
    }

    @Override
    public WordInfo[] mostFrequentWordsExtending(String[] morphemes, Integer N) throws CompiledCorpusException {
        return new WordInfo[0];
    }

    protected  void addWordOccurence(
        String word, String[][] sampleDecomps, Integer totalDecomps,
        long freqIncr) throws CompiledCorpusException {

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
            esClient().putDocument(WORD_INFO_TYPE, winfo);
        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException("Error putting ES info for word "+word, e);
        }

        return;
    }

    @Override
    public Trie getMorphNgramsTrie() throws CompiledCorpusException {
        return null;
    }

    @Override
    public long charNgramFrequency(String ngram) throws CompiledCorpusException {
        String query =
            "wordCharsSpaceConcatenated:\""+
            WordInfo_ES.insertSpaces(ngram)+
            "\"";

        SearchResults<WordInfo_ES> results = searchES_freeform(query);
        Iterator<Hit<WordInfo_ES>> iter = results.iterator();
        long freq = 0;
        while (iter.hasNext()) {
            freq += iter.next().getDocument().frequency;
        }

        return freq;
    }

    private static String insertSpaces(String orig) {
        String withSpaces = orig.replaceAll("(.)", "$1 ");
        return withSpaces;
    }

    private SearchResults<WordInfo_ES> searchES_freeform(String query) throws CompiledCorpusException {
        SearchResults<WordInfo_ES> results = null;
        try {
            results =
                esClient().search(
                    query, WORD_INFO_TYPE, new WordInfo_ES());
        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException(e);
        }

        return results;
    }

    private SearchResults<WordInfo_ES> searchES_queryMap(Query query) throws CompiledCorpusException {
        SearchResults<WordInfo_ES> results = null;
        try {
            results =
                    esClient().search(
                            query, WORD_INFO_TYPE, new WordInfo_ES());
        } catch (ElasticSearchException e) {
            throw new CompiledCorpusException(e);
        }

        return results;
    }
}

