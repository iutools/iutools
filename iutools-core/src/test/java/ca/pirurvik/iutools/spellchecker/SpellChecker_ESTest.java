package ca.pirurvik.iutools.spellchecker;

import ca.inuktitutcomputing.config.IUConfig;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ES;
import ca.pirurvik.iutools.corpus.RW_CompiledCorpus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class SpellChecker_ESTest extends SpellCheckerTest {

    private static final String emptyCorpusName = "empty-corpus";

    @Before
    public void setUp() throws Exception {
        // Make sure the ES indices are empty for the empty corpus name
        clearESIndices(new SpellChecker_ES(emptyCorpusName));
        removeCorrectWordsLatin("hansard-1999-2002.v2020-10-06");
    }

    private void removeCorrectWordsLatin(String corpusName) throws Exception {
        SpellChecker checker = new SpellChecker_ES(corpusName);
        for (String word: correctWordsLatin) {
//            checker.removeExplicitlyCorrectWord(word);
        }
    }


    @Override
    protected SpellChecker largeDictChecker() throws Exception {
        SpellChecker checker = new SpellChecker_ES("hansard-1999-2002.v2020-10-06");
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

        CompiledCorpus_ES corpus = (CompiledCorpus_ES) checker.corpus;
        corpus.deleteAll(true);

        corpus = (CompiledCorpus_ES) checker.explicitlyCorrectWords;
        corpus.deleteAll(true);

        Thread.sleep(100);

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
}