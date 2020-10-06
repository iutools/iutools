package ca.pirurvik.iutools.corpus;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.nrc.testing.AssertString;
import org.apache.commons.collections.iterators.IteratorChain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class CompiledCorpus_ESTest extends CompiledCorpusTest {

    private Set<String> generatedIdices = new HashSet<String>();

    public final static String testIndex = "iutools_corpus_test";

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

    @Test
    public void test__corpusName4File__HappyPath() {
        File jsonFile = new File("/some/path/some-corpus.ES.json");
        String gotName = CompiledCorpus_ES.corpusName4File(jsonFile);
        AssertString.assertStringEquals(
                "Corpus name was not as expected for file: "+jsonFile,
                "some-corpus", gotName);
    }
}