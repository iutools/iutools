package ca.pirurvik.iutools.spellchecker;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ES;
import ca.pirurvik.iutools.corpus.RW_CompiledCorpus;
import org.junit.Ignore;

import java.io.File;

@Ignore
public class SpellChecker_ESTest extends SpellCheckerTest {

    @Override
    protected SpellChecker makeCheckerLargeDict() throws Exception {
        SpellChecker checker = new SpellChecker(largeESCorpusFile());
        checker.setVerbose(false);
        for (String aWord: correctWordsLatin) {
            checker.addCorrectWord(aWord);
        }
        return checker;
    }

    @Override
    protected SpellChecker makeCheckerSmallCustomDict() throws Exception {
        SpellChecker checker = new SpellChecker(emptyESCorpus());
        checker.setVerbose(false);
        for (String aWord: correctWordsLatin) {
            checker.addCorrectWord(aWord);
        }
        return checker;
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