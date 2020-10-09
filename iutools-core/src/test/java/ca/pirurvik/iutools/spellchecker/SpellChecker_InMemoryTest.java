package ca.pirurvik.iutools.spellchecker;

import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import org.junit.Ignore;

// TODO-June2020: Activate this test and make it use an InMemory corpus
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
