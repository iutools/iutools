package ca.pirurvik.iutools.spellchecker;

import org.junit.Ignore;

//TODO-June2020: Activate this test and make it use an InFileSystem corpus
@Ignore
public class SpellChecker_InFileSystemTest extends SpellCheckerTest {

    @Override
    protected SpellChecker largeDictChecker() throws Exception {
        return null;
    }

    @Override
    protected SpellChecker smallDictChecker() throws Exception {
        return null;
    }
}
