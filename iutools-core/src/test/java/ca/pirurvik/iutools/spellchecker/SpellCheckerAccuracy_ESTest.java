package ca.pirurvik.iutools.spellchecker;

import ca.nrc.dtrc.elasticsearch.StreamlinedClient;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ES;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ESTest;
import org.junit.Before;
import org.junit.Ignore;

public class SpellCheckerAccuracy_ESTest extends SpellCheckerAccuracyTest {

    @Override
    protected SpellChecker makeLargeDictChecker() throws Exception {
        SpellChecker_ES checker = new SpellChecker_ES("hansard-1999-2002.v2020-10-06");
        return checker;
    }

    @Override
    protected SpellChecker makeEmptyDictChecker() throws Exception {
        String indexName = CompiledCorpus_ESTest.testIndex;
        new StreamlinedClient(indexName).deleteIndex();
        CompiledCorpus_ES corpus = new CompiledCorpus_ES(indexName);
        SpellChecker_ES checker = new SpellChecker_ES(indexName);

        return checker;
    }

    @Override
    protected String usingCorpus() {
        return "HANSARD-1999-2002.v2020-10-06.ES";
    }

}
