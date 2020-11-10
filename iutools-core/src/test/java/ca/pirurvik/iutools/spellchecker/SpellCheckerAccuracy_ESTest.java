package ca.pirurvik.iutools.spellchecker;

import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusTest;
import org.junit.Ignore;

@Ignore
public class SpellCheckerAccuracy_ESTest extends SpellCheckerAccuracyTest {

    @Override
    protected SpellChecker makeLargeDictChecker() throws Exception {
        SpellChecker_ES checker = new SpellChecker_ES();
        return checker;
    }

    @Override
    protected SpellChecker makeEmptyDictChecker() throws Exception {
        String indexName = CompiledCorpusTest.testIndex;
        new StreamlinedClient(indexName).deleteIndex();
        CompiledCorpus corpus = new CompiledCorpus(indexName);
        SpellChecker_ES checker = new SpellChecker_ES(indexName);

        return checker;
    }
}
