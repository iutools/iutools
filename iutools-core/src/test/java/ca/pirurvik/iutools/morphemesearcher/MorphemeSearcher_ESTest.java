package ca.pirurvik.iutools.morphemesearcher;

import ca.nrc.datastructure.trie.MockStringSegmenter_IUMorpheme;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusException;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ES;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ESTest;

public class MorphemeSearcher_ESTest extends MorphemeSearcherTest {

    public static final String emptyCorpusName = "empty-corpus";

    @Override
    protected CompiledCorpus makeCorpus() throws Exception {
        String indexName = CompiledCorpus_ESTest.testIndex;
        new StreamlinedClient(indexName).deleteIndex();
        CompiledCorpus corpus = new CompiledCorpus_ES(indexName);
        corpus.setSegmenterClassName(MockStringSegmenter_IUMorpheme.class.getName());
        return corpus;
    }
}
