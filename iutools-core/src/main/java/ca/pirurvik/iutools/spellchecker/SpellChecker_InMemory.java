package ca.pirurvik.iutools.spellchecker;

import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import org.junit.Ignore;

import java.io.FileNotFoundException;

@Ignore
public class SpellChecker_InMemory extends SpellChecker {
    public SpellChecker_InMemory()
        throws StringSegmenterException, SpellCheckerException {
        super();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

//    @Override
//    protected void __processCorpus() throws ConfigException, FileNotFoundException {
//        super.__processCorpus();
//        this.allWords = ((CompiledCorpus_InMemory)corpus).decomposedWordsSuite;
//    }
}
