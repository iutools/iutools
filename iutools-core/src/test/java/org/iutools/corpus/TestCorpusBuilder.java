package org.iutools.corpus;

import org.iutools.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestCorpusBuilder {

    private static Map<String, Set<String>> addedWords4Corpus =
        new HashMap<String,Set<String>>();

    private static final String emptyCorpusName = "empty-corpus";

    public static CompiledCorpus makeEmptyCorpus()
        throws Exception {
        new StreamlinedClient(emptyCorpusName).deleteIndex();
        CompiledCorpus corpus =
            new CompiledCorpus(emptyCorpusName)
            .setSegmenterClassName(StringSegmenter_IUMorpheme.class);
        ;
        return corpus;
    }

    public static void addWords(CompiledCorpus corpus, String[] words) throws CompiledCorpusException {
        String corpusName = corpus.getIndexName();
        if (!addedWords4Corpus.containsKey(corpusName)) {
            addedWords4Corpus.put(corpusName, new HashSet<String>());
        }
        Set<String> addedWords = addedWords4Corpus.get(corpusName);
        for (String aWord: words) {
            if (!addedWords.contains(aWord)) {
                addedWords.add(aWord);
                try {
                    corpus.addWordOccurence(aWord);
                } catch (CompiledCorpusException e) {
                    throw new CompiledCorpusException(e);
                }
            }
        }
    }

    public static void clear(CompiledCorpus corpus) throws CompiledCorpusException {
        String corpusName = corpus.getIndexName();
        addedWords4Corpus.put(corpusName, new HashSet<String>());
        try {
            corpus.deleteAll(true);
        } catch (CompiledCorpusException e) {
            throw new CompiledCorpusException(e);
        }
    }
}
