package ca.pirurvik.iutools.spellchecker;

import ca.inuktitutcomputing.config.IUConfig;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import ca.pirurvik.iutools.corpus.RW_CompiledCorpus;
import org.junit.Before;

import java.io.File;

public class SpellChecker_ESTest extends SpellCheckerTest {

    private static final String emptyCorpusName = "empty-corpus";

    @Before
    public void setUp() throws Exception {
        // Make sure the ES indices are empty for the empty corpus name
        clearESIndices(new SpellChecker_ES(emptyCorpusName));
    }


    @Override
    protected SpellChecker largeDictChecker() throws Exception {
        SpellChecker checker = new SpellChecker_ES(CompiledCorpusRegistry.defaultCorpusName);
        return checker;
    }

    @Override
    protected SpellChecker smallDictChecker() throws Exception {
        SpellChecker checker = new SpellChecker_ES(emptyCorpusName);
        return checker;
    }

    private void clearESIndices(SpellChecker_ES checker) throws Exception {
        if (!checker.corpusIndexName().equals(emptyCorpusName)) {
            throw new Exception(
                    "You are only allowed to clear the ES index that corresponds to a corpus that is meant to be initially empty!!");
        }

        CompiledCorpus corpus = checker.corpus;
        corpus.deleteAll(true);

        corpus = checker.explicitlyCorrectWords;
        corpus.deleteAll(true);

        Thread.sleep(100);

        return;
    }

    @Override
    protected SpellChecker makeCheckerEmptyDict() throws Exception {
        SpellChecker checker = new SpellChecker_ES(emptyESCorpus().getIndexName());
        checker.setVerbose(false);
        return checker;
    }

    protected CompiledCorpus largeESCorpus() throws Exception {
        CompiledCorpus corpus =
            RW_CompiledCorpus.read(largeESCorpusFile());
        return corpus;
    }

    protected File largeESCorpusFile() throws Exception {
        File corpusFile = new File(IUConfig.getIUDataPath("data/compiled-corpuses/HANSARD-1999-2002.ES.json"));
        return corpusFile;
    }

    protected CompiledCorpus emptyESCorpus() throws Exception {
        CompiledCorpus corpus = new CompiledCorpus("empty-corpus");
        corpus.deleteAll(true);
        return corpus;
    }
}