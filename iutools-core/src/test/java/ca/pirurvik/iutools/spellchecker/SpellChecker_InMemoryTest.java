package ca.pirurvik.iutools.spellchecker;

import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;

public class SpellChecker_InMemoryTest extends SpellCheckerTest {

    @Override
    public SpellChecker largeDictChecker() throws Exception {
        SpellChecker checker = new SpellChecker_InMemory();
        return checker;
    }

    @Override
    protected SpellChecker smallDictChecker() throws Exception {
        SpellChecker checker = new SpellChecker(CompiledCorpusRegistry.emptyCorpusName);
        return checker;
    }

}
