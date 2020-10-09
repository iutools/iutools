package ca.pirurvik.iutools.spellchecker;

import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import org.junit.Ignore;

public class SpellChecker_InMemoryTest extends SpellCheckerTest {

    @Override
    public SpellChecker largeDictChecker() throws Exception {
        SpellChecker checker = new SpellChecker();
        return checker;
    }

    @Override
    protected SpellChecker smallDictChecker() throws Exception {
        SpellChecker checker = new SpellChecker(CompiledCorpusRegistry.emptyCorpusName);
        return checker;
    }

}
