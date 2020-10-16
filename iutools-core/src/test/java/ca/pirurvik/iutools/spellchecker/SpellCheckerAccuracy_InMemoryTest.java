package ca.pirurvik.iutools.spellchecker;

import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import org.junit.Ignore;

@Ignore
public class SpellCheckerAccuracy_InMemoryTest extends SpellCheckerAccuracyTest {

    @Override
    protected SpellChecker makeLargeDictChecker() throws Exception {
        if (checkerLargeDict == null) {
            checkerLargeDict = new SpellChecker(usingCorpus());
        }
        return checkerLargeDict;
    }

    @Override
    protected SpellChecker makeEmptyDictChecker() throws Exception {
        SpellChecker checker = new SpellChecker(CompiledCorpusRegistry.emptyCorpusName);
        return checker;
    }

    @Override
    protected String usingCorpus() {
        return "Hansard1999-2002";
    }
}
