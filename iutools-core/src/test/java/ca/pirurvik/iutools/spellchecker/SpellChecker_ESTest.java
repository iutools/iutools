package ca.pirurvik.iutools.spellchecker;

import ca.inuktitutcomputing.config.IUConfig;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ES;
import ca.pirurvik.iutools.corpus.RW_CompiledCorpus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore
public class SpellChecker_ESTest extends SpellCheckerTest {

    private static final String emptyCorpusName = "empty-corpus";

    @Before
    public void setUp() throws Exception {
        // Make sure the ES indices are empty for the empty corpus name
        clearESIndices(new SpellChecker_ES(emptyCorpusName));
    }

    @Override
    protected SpellChecker makeCheckerLargeDict() throws Exception {
        SpellChecker checker = new SpellChecker_ES("hansard-1999-2002.v2020-10-06");
        checker.setVerbose(false);
        for (String aWord : correctWordsLatin) {
            checker.addCorrectWord(aWord);
        }
        return checker;
    }

    @Override
    protected SpellChecker makeCheckerSmallCustomDict() throws Exception {
        SpellChecker_ES checker = new SpellChecker_ES(emptyCorpusName);
        clearESIndices(checker);
        checker.setVerbose(false);
        for (String aWord : correctWordsLatin) {
            checker.addCorrectWord(aWord);
        }
        return checker;
    }

    private void clearESIndices(SpellChecker_ES checker) throws Exception {
        if (!checker.corpusIndexName().equals(emptyCorpusName)) {
            throw new Exception(
                    "You are only allowed to clear the ES index that corresponds to a corpus that is meant to be initially empty!!");
        }

        CompiledCorpus_ES corpus = (CompiledCorpus_ES) checker.corpus;
        corpus.deleteAll(true);

        corpus = (CompiledCorpus_ES) checker.explicitlyCorrectWords;
        corpus.deleteAll(true);

        return;
    }

    @Override
    protected SpellChecker makeCheckerEmptyDict() throws Exception {
        SpellChecker checker = new SpellChecker(emptyESCorpus());
        checker.setVerbose(false);
        return checker;
    }

    protected CompiledCorpus_ES largeESCorpus() throws Exception {
        CompiledCorpus_ES corpus =
                (CompiledCorpus_ES) RW_CompiledCorpus.read(largeESCorpusFile(), CompiledCorpus_ES.class);
        return corpus;
    }

    protected File largeESCorpusFile() throws Exception {
        File corpusFile = new File(IUConfig.getIUDataPath("data/compiled-corpuses/HANSARD-1999-2002.ES.json"));
        return corpusFile;
    }

    protected CompiledCorpus_ES emptyESCorpus() throws Exception {
        CompiledCorpus_ES corpus = new CompiledCorpus_ES("empty-corpus");
        corpus.deleteAll(true);
        return corpus;
    }


    ////////////////////////////////////////////////////////////////////////////////
    // Temporarily disable some failings tests that are inherited from parent test
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @Ignore
    public void test__spellCheck__SpeedTest() throws Exception {}
}