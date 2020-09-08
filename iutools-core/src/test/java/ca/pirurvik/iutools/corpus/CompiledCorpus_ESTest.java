package ca.pirurvik.iutools.corpus;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class CompiledCorpus_ESTest extends CompiledCorpusTest {

    private Set<String> generatedIdices = new HashSet<String>();

    final String testIndex = "iutools_corpus_test";

    @After
    public void setUp() throws Exception {
        new StreamlinedClient(testIndex).deleteIndex();
    }

    @After
    public void tearDown() throws Exception {
        new StreamlinedClient(testIndex).deleteIndex();
    }

    @Override
    protected CompiledCorpus makeCorpusWithDefaultSegmenter() throws Exception {
        CompiledCorpus_ES corpus = new CompiledCorpus_ES(testIndex);
        return corpus;
    }

    ///////////////////////////////////////////
    // We Ovrerride and @Ignore all tests that are currently
    // not passing for the ES subclass
    ///////////////////////////////////////////

//    @Test
//    @Ignore
//    public void test__CompiledCorpus__Synopsis() throws Exception {
//    }
}