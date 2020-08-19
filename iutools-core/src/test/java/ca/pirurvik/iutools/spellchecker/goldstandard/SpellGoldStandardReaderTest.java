package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.nrc.file.ResourceGetter;
import ca.nrc.testing.AssertObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

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

    @Test @Ignore
    public void test__read__HappyPath() throws Exception {
        SpellGoldStandard gotGS = SpellGoldStandardReader.read(gsDir);
        String[] expGood = new String[] {
            "good", "hello", "morning", "universe"
        };
        new AssertSpellGoldStandard(gotGS, "")
            .wordsAre(new String[] {"good", "greetinhs", "hello", "morning",
                "universe", "wrld"})
            .correctlySpelledWordsAre(
                new String[] {"good", "hello", "morning", "universe"})
            .misspelledWordsAre(new String[] {"greetinhs", "wrld"})
            .wordsWithMultipleCorrectionsAre(
                new String[][]{
                    new String[] {"greetinhs", "greetings", "greetinhs"}
                })
            .missedRevisionsAre(Triple.of("BLAH","BLAH", "BLAH"))
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
