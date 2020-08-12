package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.nrc.file.ResourceGetter;
import ca.nrc.testing.AssertObject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class SpellGoldStandardReaderTest {

    File gsDir = null;

    @Before
    public void setUp() throws Exception {
        gsDir = ResourceGetter.copyResourceToTempLocation("ca/pirurvik/iutools/spellchecker/goldstandard");
    }

    ////////////////////////////////////
    // DOCUMENTATION TESTS
    ////////////////////////////////////

    @Test
    public void test__SpellGoldStandardReader__Synopsis() throws Exception {
        // Use this class to read a Spell Checker Gold Standard from
        // a bunch of files produced by human evaluators.
        //
        SpellGoldStandard gotGS = SpellGoldStandardReader.read(gsDir);
    }

    ////////////////////////////////////
    // VERIFICATION TESTS
    ////////////////////////////////////

    @Test
    public void test__read__HappyPath() throws Exception {
        SpellGoldStandard gotGS = SpellGoldStandardReader.read(gsDir);
        new AssertSpellGoldStandard(gotGS, "")
            .correctlySpelledWordsAre("BLAH")
            .wordsWithMultipleCorrectionsAre(new String[][]{})
            ;
    }

    @Test
    public void test__idAndRevisorForFile__HappyPath() throws Exception {
        File csvFile = new File("hello_world.joe.csv");
        SpellGoldStandardReader.CSVConsumer consumer = new SpellGoldStandardReader.CSVConsumer(new SpellGoldStandard());
        Pair<String,String> gotInfo = consumer.idAndRevisorForFile(csvFile.toPath());
        AssertObject.assertDeepEquals(
            "",
             Pair.of("hello_world", "joe"), gotInfo);
    }
}
