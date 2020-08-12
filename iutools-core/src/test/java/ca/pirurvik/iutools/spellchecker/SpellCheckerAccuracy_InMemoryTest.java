package ca.pirurvik.iutools.spellchecker;

import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;

public class SpellCheckerAccuracy_InMemoryTest extends SpellCheckerAccuracyTest {
    @Override
    protected String usingCorpus() {
		return "Hansard1999-2002";
    }
}
